package ru.hflabs.rcd.service.document.record;

import com.google.common.base.Function;
import com.google.common.collect.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.comparator.CompoundComparator;
import org.springframework.util.comparator.InvertibleComparator;
import ru.hflabs.rcd.accessor.Accessors;
import ru.hflabs.rcd.exception.constraint.IllegalPrimaryKeyException;
import ru.hflabs.rcd.model.Comparators;
import ru.hflabs.rcd.model.change.ChangeMode;
import ru.hflabs.rcd.model.change.ChangeSet;
import ru.hflabs.rcd.model.change.ChangeType;
import ru.hflabs.rcd.model.criteria.FilterCriteria;
import ru.hflabs.rcd.model.criteria.FilterCriteriaValue;
import ru.hflabs.rcd.model.criteria.FilterResult;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.document.Record;
import ru.hflabs.rcd.service.IValidateService;
import ru.hflabs.rcd.service.document.IFieldService;
import ru.hflabs.rcd.service.document.IMetaFieldService;
import ru.hflabs.rcd.service.document.IRecordService;
import ru.hflabs.rcd.term.Condition;
import ru.hflabs.util.core.collection.CollectionUtil;
import ru.hflabs.util.spring.Assert;

import javax.swing.*;
import java.util.*;

import static ru.hflabs.rcd.model.CriteriaUtils.createCriteriaByIDs;
import static ru.hflabs.rcd.model.ModelUtils.*;
import static ru.hflabs.rcd.service.ServiceUtils.*;

/**
 * Класс <class>RecordService</class> реализует сервис работы с записями справочников
 *
 * @author Nazin Alexander
 */
public class RecordService implements IRecordService, ApplicationEventPublisherAware {

    /** Сервис публикации событий */
    private ApplicationEventPublisher eventPublisher;
    /** Сервис работы с МЕТА-полями справочников */
    private IMetaFieldService metaFieldService;
    /** Сервис работы с значениями полей записи */
    private IFieldService fieldService;
    /** Сервис валидации модификации записей */
    private IValidateService<Record> changeValidator;
    /** Валидатор закрытия записей */
    private IValidateService<Record> closeValidator;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    @Override
    public Class<Record> retrieveTargetClass() {
        return Record.class;
    }

    public void setMetaFieldService(IMetaFieldService metaFieldService) {
        this.metaFieldService = metaFieldService;
    }

    public void setFieldService(IFieldService fieldService) {
        this.fieldService = fieldService;
    }

    public void setChangeValidator(IValidateService<Record> changeValidator) {
        this.changeValidator = changeValidator;
    }

    public void setCloseValidator(IValidateService<Record> closeValidator) {
        this.closeValidator = closeValidator;
    }

    /**
     * Выполняет фильтрацию записей справочника
     *
     * @param metaFields коллекция МЕТА-полей справочника
     * @param recordCriteria критерий поиска
     * @param fillTransitive флаг необходимости заполнения транзитивных зависимостей
     * @return Возвращает результат фильтрации
     */
    private FilterResult<Record> doFindRecordsByCriteria(Collection<MetaField> metaFields, FilterCriteria recordCriteria, boolean fillTransitive) {
        Assert.notNull(recordCriteria, "Filter criteria must not be NULL");
        // Проверяем, что МЕТА-поля справочника существуют
        if (CollectionUtils.isEmpty(metaFields)) {
            return new FilterResult<>(Collections.<Record>emptyList(), 0, 0);
        }
        final MetaField primaryMetaField = retrievePrimaryMetaField(metaFields);
        // Запоминаем количество МЕТА-полей, для рассчета количества результирующих записей
        final int metaFieldsCount = metaFields.size();
        // Формируем карту фильтров
        ImmutableMap.Builder<String, FilterCriteriaValue<?>> resultCriteriaFilter = ImmutableMap.<String, FilterCriteriaValue<?>>builder()
                .put(Field.META_FIELD_ID, new FilterCriteriaValue.StringsValue(Collections2.transform(metaFields, ID_FUNCTION)));
        // Получаем общее количество записей для справочника
        final int totalRecordsCount = fieldService.countByCriteria(new FilterCriteria().injectFilters(resultCriteriaFilter.build())) / metaFieldsCount;

        // Если исходный критерий заполнен, то выполняем поиск полей, которые ему удовлетворяют
        if (StringUtils.hasText(recordCriteria.getSearch()) || !CollectionUtils.isEmpty(recordCriteria.getFilters())) {
            Collection<Field> fields = fieldService.findByCriteria(
                    new FilterCriteria()
                            .injectSearch(recordCriteria.getSearch())
                            .injectFilters(
                                    recordCriteria.getFilters() != null ?
                                            ImmutableMap.<String, FilterCriteriaValue<?>>builder()
                                                    .putAll(resultCriteriaFilter.build())
                                                    .putAll(recordCriteria.getFilters())
                                                    .build() :
                                            resultCriteriaFilter.build()
                            ),
                    false).getResult();
            // Если по заданным критериям найдены значения полей, то добавляем идентификаторы их первичных полей в карту фильтрации
            if (!fields.isEmpty()) {
                Set<String> fieldNames = Sets.newHashSet(Collections2.transform(fields, NAME_FUNCTION));
                resultCriteriaFilter.put(Field.NAME, new FilterCriteriaValue.StringsValue(fieldNames, Condition.OR));
            } else {
                return new FilterResult<>(Collections.<Record>emptyList(), 0, totalRecordsCount);
            }
        }

        // Модифицируем запрос, опираясь на то, что количество полей кратно количеству МЕТА-полей
        FilterCriteria resultCriteria = new FilterCriteria()
                .injectFilters(resultCriteriaFilter.build());
        // Получаем целевые значения полей
        Collection<Field> fields = fieldService.findAllByCriteria(resultCriteria, fillTransitive);
        // Формируем записи
        List<Record> records = createRecords(metaFields, fields);
        // Выполняем сортировку записей
        if (StringUtils.hasText(recordCriteria.getSortOrderKey()) && !SortOrder.UNSORTED.equals(recordCriteria.getSortOrderValue())) {
            Comparator<Record> recordComparator = new CompoundComparator<>(
                    new InvertibleComparator<>(new Comparators.RecordComparator(recordCriteria.getSortOrderKey()), SortOrder.ASCENDING.equals(recordCriteria.getSortOrderValue())),
                    Comparators.IDENTIFYING_COMPARATOR);
            Collections.sort(records, recordComparator);
        } else {
            Comparator<Record> recordComparator = new CompoundComparator<>(
                    new Comparators.RecordComparator(primaryMetaField.getName()),
                    Comparators.IDENTIFYING_COMPARATOR);
            Collections.sort(records, recordComparator);
        }
        // Выполняем выделение страницы
        List<Record> result = CollectionUtil.extractPage(records, recordCriteria.getOffset(), recordCriteria.getCount());
        // Выполняем заполнение справочника при необходимости
        if (fillTransitive) {
            result = Lists.newArrayList(Collections2.transform(result, new Function<Record, Record>() {
                @Override
                public Record apply(Record input) {
                    return input.injectDictionary(primaryMetaField.getRelative());
                }
            }));
        }
        // Формируем результирующий ответ
        return new FilterResult<>(
                result, records.size(), totalRecordsCount
        );
    }

    @Override
    public Record findByID(String dictionaryId, String id, boolean fillTransitive, boolean quietly) {
        Assert.isTrue(StringUtils.hasText(id), "ID must not be NULL or EMPTY");
        return extractSingleDocument(findByIDs(dictionaryId, ImmutableSet.of(id), fillTransitive, quietly), null);
    }

    @Override
    public Collection<Record> findByIDs(String dictionaryId, Set<String> ids, boolean fillTransitive, boolean quietly) {
        Assert.isTrue(StringUtils.hasText(dictionaryId), "Dictionary ID must not be NULL or EMPTY");
        Assert.isTrue(!CollectionUtils.isEmpty(ids), "IDs must not be NULL or EMPTY");
        // Получаем МЕТА-поля по идентификатору справочника
        Collection<MetaField> metaFields = metaFieldService.findAllByRelativeId(dictionaryId, null, fillTransitive);
        // Проверяем, что МЕТА-поля найдены
        if (CollectionUtils.isEmpty(metaFields)) {
            if (quietly) {
                return Collections.emptyList();
            } else {
                throw new IllegalPrimaryKeyException(String.format("Can't find record with IDs '%s'", StringUtils.collectionToCommaDelimitedString(ids)));
            }
        }
        // Формируем записи
        Collection<Record> result = doFindRecordsByCriteria(metaFields, createCriteriaByIDs(Field.NAME, ids), fillTransitive).getResult();
        // Проверяем количество найденных записей и возвращаем результат
        return checkFoundDocuments(Record.class, ids, result, quietly);
    }

    @Override
    public Collection<Record> findAllRecords(String dictionaryId, boolean fillTransitive) {
        return findRecordsByCriteria(dictionaryId, new FilterCriteria().injectCount(FilterCriteria.COUNT_ALL), fillTransitive).getResult();
    }

    @Override
    public FilterResult<Record> findRecordsByCriteria(String dictionaryId, FilterCriteria criteria, boolean fillTransitive) {
        Assert.isTrue(StringUtils.hasText(dictionaryId), "Dictionary ID must not be NULL or EMPTY");
        Collection<MetaField> metaFields = metaFieldService.findAllByRelativeId(dictionaryId, null, true);
        return doFindRecordsByCriteria(metaFields, criteria, fillTransitive);
    }

    /**
     * Выполняет построение дескриптор изменений записей
     *
     * @param changeType тип изменений
     * @param records коллекция записей
     * @param validatorService сервис валидации или <code>NULL</code>, если валидацию необязательна
     * @return Возвращает построенный дескриптор изменений
     */
    private ChangeSet<Record> buildChangeSet(ChangeType changeType, Collection<Record> records, IValidateService<Record> validatorService) {
        ChangeSet<Record> changeSet = new ChangeSet<>(retrieveTargetClass(), changeType, ChangeMode.DEFAULT);
        for (Record record : records) {
            record = (validatorService != null) ? validatorService.validate(record) : record;
            changeSet.appendChange(record);
        }
        return changeSet;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public Collection<Record> create(Collection<Record> objects, boolean needValidation) {
        ChangeSet<Record> changeSet = buildChangeSet(ChangeType.CREATE, objects, needValidation ? changeValidator : null);
        publishChangeEvent(eventPublisher, this, changeSet);
        return changeSet.getChanged();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public Collection<Record> update(Collection<Record> objects, boolean needValidation) {
        ChangeSet<Record> changeSet = buildChangeSet(ChangeType.UPDATE, objects, needValidation ? changeValidator : null);
        publishChangeEvent(eventPublisher, this, changeSet);
        return changeSet.getChanged();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public Collection<Record> close(Collection<Record> objects, boolean needValidation) {
        Assert.isTrue(!CollectionUtils.isEmpty(objects), "Collection must not be NULL or EMPTY");
        // Формируем коллецию соответствий идентификатора справочника к записям
        Map<String, Collection<Record>> dictionaryId2records = Multimaps.index(objects, new Function<Record, String>() {
            @Override
            public String apply(Record input) {
                return input.getDictionaryId();
            }
        }).asMap();

        // Выполняем построение дескриптор закрытия
        ChangeSet<Record> changeSet = new ChangeSet<>(retrieveTargetClass(), ChangeType.CLOSE, ChangeMode.DEFAULT);
        for (Map.Entry<String, Collection<Record>> entry : dictionaryId2records.entrySet()) {
            Collection<Record> existedRecords = findByIDs(entry.getKey(), Sets.newHashSet(Collections2.transform(entry.getValue(), ID_FUNCTION)), true, true);
            ChangeSet<Record> validated = buildChangeSet(ChangeType.CLOSE, existedRecords, needValidation ? closeValidator : null);
            changeSet.appendChanges(validated.getChanged());
        }

        publishChangeEvent(eventPublisher, this, changeSet);
        return changeSet.getChanged();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public void closeByIDs(final String dictionaryId, Set<String> ids) {
        Assert.isTrue(StringUtils.hasText(dictionaryId), "Dictionary ID must not be NULL or EMPTY");
        Assert.isTrue(!CollectionUtils.isEmpty(ids), "Closed IDs must not be NULL or EMPTY");
        // Формируем фиктивную коллецию записей
        Collection<Record> records = Lists.newArrayList(Collections2.transform(ids, new Function<String, Record>() {
            @Override
            public Record apply(String input) {
                Assert.notNull(input, "Closed ID must not be NULL");
                final Record record = Accessors.injectId(new Record(), input);
                record.setDictionaryId(dictionaryId);
                return record;
            }
        }));
        // Выполняем закрытие записей
        close(records, true);
    }
}
