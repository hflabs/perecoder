package ru.hflabs.rcd.web.controller.document;

import com.google.common.base.Function;
import com.google.common.collect.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import ru.hflabs.rcd.model.ModelUtils;
import ru.hflabs.rcd.model.criteria.FilterCriteria;
import ru.hflabs.rcd.model.criteria.FilterCriteriaValue;
import ru.hflabs.rcd.model.criteria.FilterResult;
import ru.hflabs.rcd.model.definition.ModelDefinition;
import ru.hflabs.rcd.model.definition.ModelFieldDefinition;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.document.MetaFieldType;
import ru.hflabs.rcd.model.document.Record;
import ru.hflabs.rcd.service.IMergeService;
import ru.hflabs.rcd.service.IPagingService;
import ru.hflabs.rcd.service.document.IMetaFieldService;
import ru.hflabs.rcd.service.document.IRecordService;
import ru.hflabs.rcd.web.controller.ControllerTemplate;
import ru.hflabs.rcd.web.model.PageRequestBean;
import ru.hflabs.rcd.web.model.PageResponseBean;
import ru.hflabs.util.core.FormatUtil;

import javax.annotation.Resource;
import javax.swing.*;
import javax.validation.Valid;
import java.util.*;

import static ru.hflabs.rcd.accessor.Accessors.injectId;
import static ru.hflabs.rcd.web.PagingUtils.findPageByCriteria;
import static ru.hflabs.rcd.web.controller.document.DictionaryController.createTransferModel;

/**
 * Класс <class>RecordController</class> реализует контроллер управления записями справочника
 *
 * @see Record
 */
@Controller(RecordController.MAPPING_URI + RecordController.NAME_POSTFIX)
@RequestMapping(RecordController.MAPPING_URI + RecordController.DATA_URI)
public class RecordController extends ControllerTemplate {

    public static final String MAPPING_URI = "records";

    /** Сервис конвертации записи в плоскую карту значений */
    private static final Function<Record, Map<String, String>> RECORD_TO_ENTITY_FUNCTION = new Function<Record, Map<String, String>>() {
        @Override
        public Map<String, String> apply(Record input) {
            Map<String, String> result = Maps.newLinkedHashMap();
            // Устанавливаем идентификаторы
            result.put(Record.PRIMARY_KEY, input.getId());
            result.put(Record.DICTIONARY_ID, input.getDictionaryId());
            // Устанавливаем контент
            Map<String, Field> fields = input.getFields();
            if (!CollectionUtils.isEmpty(fields)) {
                for (Field field : fields.values()) {
                    result.put(field.getMetaFieldId(), field.getValue());
                }
            }
            // Возвращаем сформированную запись
            return result;
        }
    };
    /** Сервис конвертации плоской карты значений в запись */
    private static final IMergeService<Map<String, String>, Record, Record> ENTITY_TO_RECORD_FUNCTION = new RecordMergeService();

    /** Сервис работы с МЕТА-полями справочника */
    @Resource(name = "metaFieldService")
    private IMetaFieldService metaFieldService;
    /** Сервис работы с записями справочника */
    @Resource(name = "recordService")
    private IRecordService recordService;

    @RequestMapping(value = "/model", method = RequestMethod.GET)
    @ResponseBody
    public Collection<ModelDefinition> createModel() {
        // Получаем модель МЕТА-поля
        ModelDefinition metaFieldDefinition = modelDefinitionFactory.retrieveService(MetaField.class);
        // Добавляем флаги
        ModelFieldDefinition flagDefinition = new ModelFieldDefinition();
        {
            flagDefinition.setType(ModelFieldDefinition.FieldType.BOOLEAN);
            flagDefinition.setRequired(true);
        }
        metaFieldDefinition.getFields().put(MetaField.FLAG_PRIMARY_NAME, flagDefinition);
        metaFieldDefinition.getFields().put(MetaField.FLAG_UNIQUE_NAME, flagDefinition);
        metaFieldDefinition.getFields().put(MetaField.FLAG_HIDDEN_NAME, flagDefinition);
        // Добавляем доступные типы
        metaFieldDefinition.setAvailableValues(
                ImmutableMap.<String, Object>builder()
                        .put(MetaField.TYPE, EnumSet.allOf(MetaFieldType.class))
                        .build()
        );
        metaFieldDefinition.setDefaultParameters(
                ImmutableMap.<String, Object>builder()
                        .put(MetaField.TYPE, MetaFieldType.STRING)
                        .build()
        );
        // Возвращаем сформированные модели
        return ImmutableList.<ModelDefinition>builder()
                .add(metaFieldDefinition)
                .addAll(createTransferModel(modelDefinitionFactory))
                .build();
    }

    @RequestMapping(value = "/{dictionaryId}", method = RequestMethod.GET)
    @ResponseBody
    public PageResponseBean<Map<String, String>> getRecords(
            @PathVariable final String dictionaryId,
            @RequestParam(value = "recordIDs", required = false) final Set<String> recordIDs,
            @Valid @ModelAttribute final PageRequestBean page) {
        // Форматируем параметры
        final String targetDictionaryId = FormatUtil.parseString(dictionaryId);
        // Устаналиваем сортировку
        if (StringUtils.hasText(page.getSortOrderKey()) && !SortOrder.UNSORTED.equals(page.getSortOrderValue())) {
            page.setSortOrderKey(ModelUtils.NAME_FUNCTION.apply(metaFieldService.findByID(page.getSortOrderKey(), false, true)));
        }
        // Рассчитываем размер и целевую страницы в зависимости от переданной коллекции идентификаторов
        final Integer targetPageSize;
        final Integer targetPage;
        if (!CollectionUtils.isEmpty(recordIDs)) {
            targetPageSize = recordIDs.size();
            targetPage = PageRequestBean.DEFAULT_PAGE;
        } else {
            targetPageSize = page.getPageSize();
            targetPage = page.getPage();
        }
        // Выполняем поиск страницы записей
        return findPageByCriteria(targetPageSize, defaultPagingSize, targetPage, new IPagingService<Map<String, String>>() {
            @Override
            public FilterResult<Map<String, String>> findPage(int count, int offset) {
                // Формируем критерий
                FilterCriteria filterCriteria = page.createFilterCriteria()
                        .injectOffset(offset)
                        .injectCount(count);
                // Устанавливаем фильтры
                if (!CollectionUtils.isEmpty(recordIDs)) {
                    filterCriteria = filterCriteria
                            .injectFilters(ImmutableMap.<String, FilterCriteriaValue<?>>of(
                                    Field.NAME, new FilterCriteriaValue.StringsValue(recordIDs)
                            ));
                }
                // Выполняем поиск
                FilterResult<Record> records = recordService.findRecordsByCriteria(targetDictionaryId, filterCriteria, false);
                // Формируем декораторы
                Collection<Map<String, String>> result = Lists.newArrayList(
                        Collections2.transform(records.getResult(), RECORD_TO_ENTITY_FUNCTION)
                );
                // Возвращаем результат фильтрации
                return new FilterResult<>(result, records.getCountByFilter(), records.getTotalCount());
            }
        });
    }

    @RequestMapping(value = "/{dictionaryId}", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> createRecord(@PathVariable String dictionaryId, @RequestBody Map<String, String> bean) {
        // Формируем запись
        Record record = ENTITY_TO_RECORD_FUNCTION.merge(bean, null);
        {
            record.setId(null);
            record.setDictionaryId(dictionaryId);
        }
        // Выполняем создание запись
        Record result = createSingleDocument(recordService, injectId(record, null), true);
        // Возвращаем созданную запись
        return RECORD_TO_ENTITY_FUNCTION.apply(result);
    }

    @RequestMapping(value = "/{dictionaryId}/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, String> updateRecord(@PathVariable String dictionaryId, @PathVariable String id, @RequestBody Map<String, String> bean) {
        // Формируем запись
        Record record = ENTITY_TO_RECORD_FUNCTION.merge(bean, recordService.findByID(dictionaryId, id, false, false));
        {
            record.setId(id);
            record.setDictionaryId(dictionaryId);
        }
        // Выполняем обновление записи
        Record result = updateSingleDocument(recordService, record, true);
        // Возвращаем обновленную запись запись
        return RECORD_TO_ENTITY_FUNCTION.apply(result);
    }

    @RequestMapping(value = "/{dictionaryId}/{id}", method = RequestMethod.DELETE)
    public void deleteRecord(@PathVariable String dictionaryId, @PathVariable String id) {
        recordService.closeByIDs(dictionaryId, Sets.newHashSet(id));
    }

    /**
     * Класс <class>RecordMergeService</class> реализует сервис слияния старых и новых значений полей записи
     *
     * @author Nazin Alexander
     */
    private static final class RecordMergeService implements IMergeService<Map<String, String>, Record, Record> {

        @Override
        public Record merge(Map<String, String> newEssence, Record oldEssence) {
            final Record result = new Record();
            // Устанавливаем идентификаторы
            result.setId(newEssence.remove(Record.PRIMARY_KEY));
            result.setDictionaryId(newEssence.remove(Record.DICTIONARY_ID));
            // Остальные значения интерпретируем как поля записи
            Map<String, Field> oldFields = (oldEssence != null) ?
                    Maps.uniqueIndex(oldEssence.getFields().values(), ModelUtils.RELATIVE_ID_FUNCTION) :
                    Collections.<String, Field>emptyMap();
            Map<String, Field> newFields = new LinkedHashMap<>(newEssence.size());
            for (Map.Entry<String, String> entry : newEssence.entrySet()) {
                // Получаем предыдущее значение поля
                Field field = oldFields.containsKey(entry.getKey()) ?
                        oldFields.get(entry.getKey()) :
                        new Field();
                // Устанавливаем системные поля
                field.setName(result.getId());
                field.setMetaFieldId(entry.getKey());
                // Устанавливаем значение
                field.setValue(entry.getValue());
                // Сохраняем новое значение
                newFields.put(entry.getKey(), field);
            }
            // Возвращаем сформированную запись
            return result.injectFields(newFields);
        }
    }
}
