package ru.hflabs.rcd.web.controller.rule;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import ru.hflabs.rcd.exception.constraint.IllegalPrimaryKeyException;
import ru.hflabs.rcd.model.criteria.FilterCriteria;
import ru.hflabs.rcd.model.criteria.FilterCriteriaValue;
import ru.hflabs.rcd.model.definition.ModelDefinition;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;
import ru.hflabs.rcd.service.document.IFieldService;
import ru.hflabs.rcd.service.document.IMetaFieldService;
import ru.hflabs.rcd.service.rule.IRecodeRuleSetService;
import ru.hflabs.rcd.web.controller.ControllerTemplate;
import ru.hflabs.rcd.web.model.rule.RecodeRuleDirection;
import ru.hflabs.rcd.web.model.rule.RecodeRuleSetRequestBean;
import ru.hflabs.rcd.web.model.rule.RecodeRuleSetResponseBean;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collection;

import static ru.hflabs.rcd.accessor.Accessors.*;
import static ru.hflabs.rcd.model.ModelUtils.ID_FUNCTION;
import static ru.hflabs.rcd.model.change.Predicates.*;

/**
 * Класс <class>RecodeRuleSetController</class> реализует контроллер управления наборами правил перекодирования
 *
 * @see RecodeRuleSet
 */
@Controller(RecodeRuleSetController.MAPPING_URI + RecodeRuleSetController.NAME_POSTFIX)
@RequestMapping(RecodeRuleSetController.MAPPING_URI + RecodeRuleSetController.DATA_URI)
public class RecodeRuleSetController extends ControllerTemplate {

    public static final String MAPPING_URI = "rrs";

    /** Сервис работы с МЕТА-полями справочника */
    @Resource(name = "metaFieldService")
    private IMetaFieldService metaFieldService;
    /** Сервис работы со значениями полей справочника */
    @Resource(name = "fieldService")
    private IFieldService fieldService;
    /** Сервис работы с наборами правил перекодирования */
    @Resource(name = "recodeRuleSetService")
    private IRecodeRuleSetService recodeRuleSetService;

    @RequestMapping(value = "/model", method = RequestMethod.GET)
    @ResponseBody
    public ModelDefinition createModel() {
        return modelDefinitionFactory.retrieveService(RecodeRuleSetRequestBean.class);
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public Collection<RecodeRuleSetResponseBean> getRecodeRuleSets(
            @RequestParam(value = MetaField.DICTIONARY_ID, required = false) String dictionaryId,
            @RequestParam(value = "direction", required = false) RecodeRuleDirection direction) {
        FilterCriteria filterCriteria = new FilterCriteria();
        // Устанавливаем идентификатор справочника, если он задан
        if (StringUtils.hasText(dictionaryId)) {
            final String targetFieldName;
            if (RecodeRuleDirection.FROM.equals(direction)) {
                targetFieldName = RecodeRuleSet.FROM_DICTIONARY_ID;
            } else if (RecodeRuleDirection.TO.equals(direction)) {
                targetFieldName = RecodeRuleSet.TO_DICTIONARY_ID;
            } else {
                targetFieldName = MetaField.DICTIONARY_ID;
            }
            filterCriteria.injectFilters(
                    ImmutableMap.<String, FilterCriteriaValue<?>>of(
                            targetFieldName, new FilterCriteriaValue.StringValue(dictionaryId)
                    )
            );
        }
        // Выполняем поиск
        return Collections2.transform(
                recodeRuleSetService.findAllByCriteria(filterCriteria, true),
                RecodeRuleSetResponseBean.CONVERT
        );
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public RecodeRuleSetResponseBean getRecodeRuleSet(@PathVariable String id) {
        return RecodeRuleSetResponseBean.CONVERT.apply(
                recodeRuleSetService.findByID(id, true, false)
        );
    }

    private static MetaField detectRecodeMetaField(FluentIterable<MetaField> metaFields, Predicate<MetaField> predicate) {
        return metaFields.firstMatch(predicate).orNull();
    }

    /**
     * Определяет МЕТА-поле перекодирования по следующему принципу:</br>
     * <ul>
     * <li>{@link MetaField#FLAG_PRIMARY Первичное} поле, если оно {@link MetaField#FLAG_HIDDEN не скрытое}</li>
     * <li>Первое {@link MetaField#FLAG_UNIQUE уникальное} и не скрытое поле</li>
     * <li>Первое не скрытое поле, из коллекции, отсортированной по {@link MetaField#ordinal порядку}</li>
     * </ul>
     *
     * @param metaFieldId идентификатор предпочитаемого поля или <code>NULL</code>
     * @param dictionaryId идентификатор справочника
     * @return Возвращает МЕТА-поле
     */
    private MetaField detectRecodeMetaField(String metaFieldId, String dictionaryId) {
        if (StringUtils.hasText(metaFieldId)) {
            return metaFieldService.findByID(metaFieldId, true, false);
        }
        // Получаем все МЕТА-поля справочника
        FluentIterable<MetaField> metaFields = FluentIterable.from(metaFieldService.findAllByRelativeId(dictionaryId, null, true));
        // Пытаемся получить первичное МЕТА-поле
        MetaField result = detectRecodeMetaField(metaFields, Predicates.and(PRIMARY_META_FIELD_PREDICATE, NOT_HIDDEN_META_FIELD_PREDICATE));
        if (result != null) {
            return result;
        }
        // Пытаемся получить уникальное МЕТА-поле
        result = detectRecodeMetaField(metaFields, Predicates.and(UNIQUE_META_FIELD_PREDICATE, NOT_HIDDEN_META_FIELD_PREDICATE));
        if (result != null) {
            return result;
        }
        // Получаем первое не скрытое
        result = detectRecodeMetaField(metaFields, NOT_HIDDEN_META_FIELD_PREDICATE);
        if (result != null) {
            return result;
        }
        // Определить МЕТА-поле не удалось, ориентируемся на первичное
        result = detectRecodeMetaField(metaFields, PRIMARY_META_FIELD_PREDICATE);
        if (result != null) {
            return result;
        }
        // Определить целевое МЕТА-поле не удалось
        throw new IllegalPrimaryKeyException(
                String.format("Can't find meta fields for dictionary with ID '%s'", dictionaryId)
        );
    }

    /**
     * Выполняет формирование набора правил перекодирования из идентификаторов связанных сущностей
     *
     * @param id идентификатор правила
     * @param bean декоратор идентификаторов
     * @return Возвращает сфориированный набор правил перекодирования
     */
    private RecodeRuleSet convertRecodeRuleSet(String id, RecodeRuleSetRequestBean bean) {
        // Получаем исходное МЕТА-поле
        MetaField fromMetaField = detectRecodeMetaField(bean.getFromMetaFieldId(), bean.getFromDictionaryId());
        // Получаем целевое МЕТА-поле
        MetaField toMetaField = detectRecodeMetaField(bean.getToMetaFieldId(), bean.getToDictionaryId());
        // Формируем правило перекодирования
        RecodeRuleSet ruleSet = injectId(bean.getDelegate(), id);
        ruleSet = FROM_SET_INJECTOR.inject(ruleSet, fromMetaField);
        ruleSet = TO_SET_INJECTOR.inject(ruleSet, toMetaField);
        // Формируем идентификатор поля поумолчанию
        if (StringUtils.hasText(bean.getDefaultRecordId())) {
            ruleSet = ruleSet.injectDefaultField(
                    fieldService.findUniqueByRelativeId(ID_FUNCTION.apply(toMetaField), bean.getDefaultRecordId(), false, false)
            );
        }
        // Возвращаем сформированный набор правил
        return ruleSet;
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ResponseBody
    public RecodeRuleSetResponseBean createRecodeRuleSet(@Valid @RequestBody RecodeRuleSetRequestBean bean) {
        // Выполняем подготовку набора правил
        RecodeRuleSet ruleSet = convertRecodeRuleSet(null, bean);
        // Выполняем создание и возвращаем результат
        return RecodeRuleSetResponseBean.CONVERT.apply(
                createSingleDocument(recodeRuleSetService, ruleSet, true)
        );
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public RecodeRuleSetResponseBean updateRecodeRuleSet(@PathVariable String id, @Valid @RequestBody RecodeRuleSetRequestBean bean) {
        // Выполняем подготовку набора правил
        RecodeRuleSet ruleSet = convertRecodeRuleSet(id, bean);
        // Выполняем обновление и возвращаем результат
        return RecodeRuleSetResponseBean.CONVERT.apply(
                updateSingleDocument(recodeRuleSetService, ruleSet, true)
        );
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void closeRecodeRuleSet(@PathVariable String id) {
        recodeRuleSetService.closeByIDs(Sets.newHashSet(id));
    }
}
