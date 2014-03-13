package ru.hflabs.rcd.service.document.recodeRule;

import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.accessor.RuleFieldAccessor;
import ru.hflabs.rcd.event.modify.ChangeEvent;
import ru.hflabs.rcd.exception.constraint.rule.AttachedRecodeRuleException;
import ru.hflabs.rcd.exception.search.rule.UnknownRecodeRuleException;
import ru.hflabs.rcd.model.change.Diff;
import ru.hflabs.rcd.model.change.Predicates;
import ru.hflabs.rcd.model.criteria.FilterCriteriaValue;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.path.FieldNamedPath;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;
import ru.hflabs.rcd.model.rule.RecodeRule;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;
import ru.hflabs.rcd.service.IMergeService;
import ru.hflabs.rcd.service.ServiceUtils;
import ru.hflabs.rcd.service.document.DocumentServiceTemplate;
import ru.hflabs.rcd.service.document.IFieldService;
import ru.hflabs.rcd.service.rule.IRecodeRuleService;
import ru.hflabs.rcd.service.rule.IRecodeRuleSetService;
import ru.hflabs.util.core.EqualsUtil;
import ru.hflabs.util.spring.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static ru.hflabs.rcd.accessor.Accessors.*;
import static ru.hflabs.rcd.model.CriteriaUtils.*;
import static ru.hflabs.rcd.model.ModelUtils.*;
import static ru.hflabs.rcd.service.ServiceUtils.*;

/**
 * Класс <class>RecodeRuleService</class> реализует сервис работы с правилами перекодирования
 *
 * @author Nazin Alexander
 */
public class RecodeRuleService extends DocumentServiceTemplate<RecodeRule> implements IRecodeRuleService {

    /** Сервис работы с наборами правил перекодировани */
    private IRecodeRuleSetService recodeRuleSetService;
    /** Сервис работы со значениями полей */
    private IFieldService fieldService;

    /** Сервис обновления значений полей правил перекодирования */
    private IMergeService<RecodeRuleSet, Collection<RecodeRule>, Collection<RecodeRule>> metaFieldActualizeService;

    public RecodeRuleService() {
        super(RecodeRule.class);
        this.metaFieldActualizeService = new MetaFieldActualizeService();
    }

    public void setRecodeRuleSetService(IRecodeRuleSetService recodeRuleSetService) {
        this.recodeRuleSetService = recodeRuleSetService;
    }

    public void setFieldService(IFieldService fieldService) {
        this.fieldService = fieldService;
    }

    @Override
    protected Collection<RecodeRule> injectTransitiveDependencies(Collection<RecodeRule> objects) {
        return super.injectTransitiveDependencies(injectRelations(injectRuleRelations(objects, fieldService, FROM_RULE_INJECTOR, TO_RULE_INJECTOR), recodeRuleSetService));
    }

    @Override
    public RecodeRule findUniqueByRelativeId(String relativeId, String value, boolean fillTransitive, boolean quietly) {
        RecodeRule result = findUniqueDocumentBy(this, createCriteriaByRelative(RecodeRule.RECODE_RULE_SET_ID, relativeId, RecodeRule.FROM_FIELD_ID, value), fillTransitive);
        if (result == null && !quietly) {
            throw new UnknownRecodeRuleException(String.format("Rule from set '%s' and source field ID '%s' not found", relativeId, value));
        }
        return result;
    }

    @Override
    public Collection<RecodeRule> findAllByRelativeId(String relativeId, String searchQuery, boolean fillTransitive) {
        Assert.isTrue(StringUtils.hasText(relativeId), "ID must not be NULL or EMPTY");
        return findAllByCriteria(createCriteriaByIDs(RecodeRule.RECODE_RULE_SET_ID, relativeId).injectSearch(searchQuery), fillTransitive);
    }

    /**
     * Выполняет поиск правил привязанных к идентификаторам полей
     *
     * @param recodeRuleSetId идентификатор набора правил
     * @param fieldName направление поля поиска
     * @param fieldValues значение поля поиска
     * @param fillTransitive флаг заполнения транзитивных зависимостей
     * @return Возвращает коллекцию найденных правил
     */
    private Collection<RecodeRule> doFindAllByFields(String recodeRuleSetId, String fieldName, Collection<String> fieldValues, boolean fillTransitive) {
        return findAllByCriteria(
                createCriteriaByRelative(RecodeRule.RECODE_RULE_SET_ID, recodeRuleSetId, fieldName, fieldValues),
                fillTransitive
        );
    }

    @Override
    public Collection<RecodeRule> findAllByFieldIDs(String recodeRuleSetId, Collection<String> fromFieldIDs, boolean fillTransitive) {
        return doFindAllByFields(recodeRuleSetId, RecodeRule.FROM_FIELD_ID, fromFieldIDs, fillTransitive);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public Collection<RecodeRule> modify(Collection<RecodeRule> toCreate, Collection<RecodeRule> toUpdate, Collection<RecodeRule> toClose, boolean needValidation) {
        return ImmutableList.<RecodeRule>builder()
                .addAll(create(toCreate, needValidation))
                .addAll(update(toUpdate, needValidation))
                .addAll(close(toClose, needValidation))
                .build();
    }

    /**
     * Выполняет создание правил перекодирования по созданным значениям полей
     *
     * @param fields коллекцию изменившихся значений полей
     */
    private Collection<RecodeRule> doCreateFromRulesByDependencies(Collection<Field> fields) {
        Map<String, Field> id2fields = Maps.uniqueIndex(fields, ID_FUNCTION);
        Map<String, Collection<Field>> metaFieldId2fields = Multimaps.index(
                fields, RELATIVE_ID_FUNCTION
        ).asMap();
        // Получаем все наборы перекодировок, в которых участвуют МЕТА-поля в качестве источника
        Collection<RecodeRuleSet> ruleSets = recodeRuleSetService.findAllByCriteria(
                createCriteriaByIDs(RecodeRuleSet.FROM_FIELD_ID, metaFieldId2fields.keySet()), false
        );
        // Для каждого набора выполняем поиск существующих правил с таким же исходным значением
        ImmutableSet.Builder<RecodeRule> toCreate = ImmutableSet.builder();
        for (RecodeRuleSet ruleSet : ruleSets) {
            Collection<Field> fromFields = metaFieldId2fields.get(FROM_RULE_INJECTOR.applyRelativeId(ruleSet));
            // Получаем коллекцию идентификаторов полей, для которых правила перекодирования настроены
            final Collection<String> existedFieldIDs = Collections2.transform(
                    findAllByFieldIDs(ruleSet.getId(), Collections2.transform(fromFields, ID_FUNCTION), false),
                    FROM_RULE_FIELD_ID
            );
            // Для каждого поля, для которого НЕ настроено правило перекодирования, определяем существующее правило перекодирование по исходному значению
            Collection<Field> fieldsToCheck = Collections2.filter(fromFields, new Predicate<Field>() {
                @Override
                public boolean apply(Field input) {
                    return !existedFieldIDs.contains(input.getId());
                }
            });
            for (Field fromField : fieldsToCheck) {
                RecodeRule existedRule = findOneDocumentBy(
                        this,
                        createCriteriaByRelative(RecodeRule.RECODE_RULE_SET_ID, ruleSet.getId(), RecodeRule.VALUE, fromField.getValue()),
                        true
                );
                // Если правило с таким же исходным значением существует, то выполняем создание нового правила для поля
                if (existedRule != null) {
                    String toFieldId = existedRule.getToFieldId();
                    // Получаем актуальное целевое значение
                    Field toField = id2fields.containsKey(toFieldId) ? id2fields.get(toFieldId) : existedRule.getTo();
                    // Формируем правило
                    RecodeRule newRule = new RecodeRule().injectRecodeRuleSet(ruleSet);
                    newRule = FROM_RULE_INJECTOR.inject(newRule, fromField);
                    newRule = TO_RULE_INJECTOR.inject(newRule, toField);
                    // Добавляем правило на создание
                    toCreate.add(newRule);
                }
            }
        }
        // Выполняем создание правил
        return create(toCreate.build(), false);
    }

    /**
     * Выполняем актуализацию правил, в которых изменился источник
     *
     * @param existedRules коллекция сущеуствующих правил
     * @param changedRules коллекция актуализированных правил
     * @return Возвращает обновленную коллекцию правил
     */
    private Collection<RecodeRule> doUpdateFromRulesByDependencies(Collection<Field> changedFields, Collection<RecodeRule> existedRules, Collection<RecodeRule> changedRules) {
        if (CollectionUtils.isEmpty(changedRules)) {
            return changedRules;
        }
        Map<String, Field> id2fields = Maps.uniqueIndex(changedFields, ID_FUNCTION);
        ImmutableList.Builder<RecodeRule> toUpdate = ImmutableList.builder();
        // Для каждого из актуализированных правил проверяем существование правила с таким же исходным значением
        for (Map.Entry<String, Collection<RecodeRule>> ruleSetId2rules : Multimaps.index(changedRules, RELATIVE_ID_FUNCTION).asMap().entrySet()) {
            final String ruleSetId = ruleSetId2rules.getKey();
            // Формируем карту принадлежиности правил к именованному пути
            Map<FieldNamedPath, Collection<RecodeRule>> namedPath2rules = Multimaps.index(
                    ruleSetId2rules.getValue(),
                    FROM_RULE_INJECTOR.getNamedPathFunction()
            ).asMap();
            // Выполняем проверку для каждого именованного пути
            for (Map.Entry<FieldNamedPath, Collection<RecodeRule>> toCheck : namedPath2rules.entrySet()) {
                // Пытаемся получить существующее правило по значению исходного пути
                RecodeRule existedRule = findOneDocumentBy(
                        this,
                        createCriteriaByRelative(RecodeRule.RECODE_RULE_SET_ID, ruleSetId, RecodeRule.VALUE, toCheck.getKey().getFieldValue()),
                        true
                );
                if (existedRule != null) {
                    // Правило найдено - переопределяем целевое поле
                    String toFieldId = existedRule.getToFieldId();
                    // Получаем актуальное целевое значение
                    Field toField = id2fields.containsKey(toFieldId) ? id2fields.get(toFieldId) : existedRule.getTo();
                    // Выполняем актуализацию назначения для проверяемых правил
                    for (RecodeRule rule : toCheck.getValue()) {
                        if (EqualsUtil.equals(rule.getToFieldId(), toFieldId)) {
                            toUpdate.add(TO_RULE_INJECTOR.inject(rule, toField));
                        } else {
                            toUpdate.add(TO_RULE_INJECTOR.inject(shallowClone(rule), toField));
                        }
                    }
                } else {
                    // Правило не найдено
                    toUpdate.addAll(toCheck.getValue());
                }
            }
        }
        // Выполняем обновление
        return update(toUpdate.build(), existedRules, false);
    }

    /**
     * Выполняем актуализацию правил, в которых изменилось назначение
     *
     * @param existedRules коллекция сущеуствующих правил
     * @param changedRules коллекция актуализированных правил
     * @return Возвращает обновленную коллекцию правил
     */
    private Collection<RecodeRule> doUpdateToRulesByDependencies(Collection<RecodeRule> existedRules, Collection<RecodeRule> changedRules) {
        return update(changedRules, existedRules, false);
    }

    /**
     * Выполняет обновление правил перекодирования по изменившимся значениям полей
     *
     * @param fields коллекцию изменившихся значений полей
     */
    private Collection<RecodeRule> doUpdateByDependencies(Collection<Field> fields) {
        final Set<String> fieldIDs = Sets.newHashSet(Collections2.transform(fields, ID_FUNCTION));
        // Получаем коллекцию существующих правил
        Collection<RecodeRule> existedRules = findAllByCriteria(createCriteriaByIDs(RecodeRule.FIELD_ID, fieldIDs), true);
        // Выполняем актуализацию правил
        Collection<RecodeRule> updatedRules = updateRulesByDependencies(RecodeRuleActualizeService.BY_FIELD, fields, existedRules);
        // Проверяем, что после актуализации есть изменения
        if (!CollectionUtils.isEmpty(updatedRules)) {
            ImmutableList.Builder<RecodeRule> result = ImmutableList.builder();
            // Отбираем и обновляем те правила, в которых изменился источник
            result.addAll(doUpdateFromRulesByDependencies(
                    fields,
                    existedRules,
                    Lists.newArrayList(Collections2.filter(updatedRules, new Predicate<RecodeRule>() {
                        @Override
                        public boolean apply(RecodeRule input) {
                            return fieldIDs.contains(input.getFromFieldId());
                        }
                    })))
            );
            // Отбираем и обновляем те правила, в которых изменилось назначение
            result.addAll(doUpdateToRulesByDependencies(
                    existedRules,
                    Lists.newArrayList(Collections2.filter(updatedRules, new Predicate<RecodeRule>() {
                        @Override
                        public boolean apply(RecodeRule input) {
                            return fieldIDs.contains(input.getToFieldId());
                        }
                    })))
            );
            // Возвращаем результат обновления
            return result.build();
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public <T> Collection<RecodeRule> modifyByDependencies(Class<T> dependencyClass, Collection<T> dependencies) {
        if (Field.class.equals(dependencyClass) && !CollectionUtils.isEmpty(dependencies)) {
            return ImmutableList.<RecodeRule>builder()
                    .addAll(doCreateFromRulesByDependencies((Collection<Field>) dependencies))
                    .addAll(doUpdateByDependencies((Collection<Field>) dependencies))
                    .build();
        }
        return Collections.emptyList();
    }

    /**
     * Возвращает количество правил перекодирования по коллекции полей
     *
     * @param fields коллекция полей
     * @return Возвращает количество найденных правил
     */
    private int countByFields(Collection<Field> fields) {
        return !CollectionUtils.isEmpty(fields) ? countByCriteria(createCriteriaByDocumentIDs(RecodeRule.FIELD_ID, fields)) : 0;
    }

    /**
     * Возвращает количество правил перекодирования по коллекции наборов правил
     *
     * @param ruleSets набор правил
     * @return Возвращает количество найденных правил
     */
    private int countByRuleSets(Collection<RecodeRuleSet> ruleSets) {
        return !CollectionUtils.isEmpty(ruleSets) ? countByCriteria(createCriteriaByDocumentIDs(RecodeRule.RECODE_RULE_SET_ID, ruleSets)) : 0;
    }

    /**
     * Выполняет обновление правил перекодирования по изменившимся наборам
     *
     * @param ruleSets коллекция изменившихся наборов
     */
    private void updateByRuleSets(Collection<RecodeRuleSet> ruleSets) {
        // Получаем количество существующих правил
        int count = countByRuleSets(ruleSets);
        // Проверяем, что правила найдены
        if (count > 0) {
            Map<String, RecodeRuleSet> id2ruleSet = Maps.uniqueIndex(ruleSets, ID_FUNCTION);
            // Получаем существующие правила
            Map<String, Collection<RecodeRule>> existed = Multimaps.index(
                    findByCriteria(createCriteriaByDocumentIDs(RecodeRule.RECODE_RULE_SET_ID, ruleSets).injectCount(count), true).getResult(),
                    RELATIVE_ID_FUNCTION
            ).asMap();
            // Для каждого изменившегося набора формируем коллекции на создание и закрытие, если правила конфликтуют
            for (Map.Entry<String, Collection<RecodeRule>> existedEntry : existed.entrySet()) {
                ImmutableSet.Builder<RecodeRule> toUpdate = ImmutableSet.builder();
                ImmutableSet.Builder<RecodeRule> toClose = ImmutableSet.builder();
                RecodeRuleSet ruleSet = id2ruleSet.get(existedEntry.getKey());
                Collection<RecodeRule> existedRules = existedEntry.getValue();
                // Выполняем актуализацию правил
                Collection<RecodeRule> updatedRules = metaFieldActualizeService.merge(ruleSet, existedRules);
                // Выполняем закрытие тех правил, для которых различаются
                Map<FieldNamedPath, Collection<RecodeRule>> fromNamedPath2rules = Multimaps.index(
                        updatedRules,
                        FROM_RULE_INJECTOR.getNamedPathFunction()
                ).asMap();
                for (Collection<RecodeRule> toCheck : fromNamedPath2rules.values()) {
                    if (toCheck.size() > 1 && Sets.newHashSet(Collections2.transform(toCheck, TO_RULE_INJECTOR.getNamedPathFunction())).size() != 1) {
                        toClose.addAll(toCheck);
                    } else {
                        toUpdate.addAll(toCheck);
                    }
                }
                // Выполняем обновление
                update(toUpdate.build(), existedRules, false);
                // Выполняем закрытие
                doClose(toClose.build(), null);
            }
        }
    }

    @Override
    protected void handleOtherCreateEvent(ChangeEvent event) {
        if (Field.class.equals(event.getChangedClass())) {
            doCreateFromRulesByDependencies(event.getChanged(Field.class));
        }
    }

    @Override
    protected void handleOtherUpdateEvent(ChangeEvent event) {
        if (Field.class.equals(event.getChangedClass())) {
            modifyByDependencies(Field.class, event.getChangedByPredicate(Field.class, Predicates.CHANGE_VALUE_PREDICATE));
        } else if (RecodeRuleSet.class.equals(event.getChangedClass())) {
            // Формируем правло отбора изменившихся наборов
            Predicate<Collection<Diff>> targetDiffs = new Predicate<Collection<Diff>>() {
                @Override
                public boolean apply(Collection<Diff> input) {
                    for (Diff diff : input) {
                        if (RecodeRuleSet.FROM_FIELD_ID.equals(diff.getField()) || RecodeRuleSet.TO_FIELD_ID.equals(diff.getField())) {
                            return true;
                        }
                    }
                    return false;
                }
            };
            // Выполняем обновление правил перекодирования по изменившимся наборам
            updateByRuleSets(event.getChangedByPredicate(RecodeRuleSet.class, targetDiffs));
        }
    }

    @Override
    protected void handleOtherCloseEvent(ChangeEvent event) {
        if (Field.class.equals(event.getChangedClass())) {
            // Определяем количество призязанных правил
            int existedRulesCount = countByFields(event.getChanged(Field.class));
            // Проверяем, что к закрываемым значениям полей не привязано правил
            Assert.isTrue(
                    existedRulesCount == 0,
                    String.format("Can't %s %s. Cause by: found %d attached recode rules",
                            event.getChangeType().name().toLowerCase(),
                            event.getChangedClass().getSimpleName(),
                            existedRulesCount
                    ),
                    AttachedRecodeRuleException.class
            );
        } else if (RecodeRuleSet.class.equals(event.getChangedClass())) {
            closeByCriteria(createCriteriaByDocumentIDs(RecodeRule.RECODE_RULE_SET_ID, event.getChanged(RecodeRuleSet.class)));
        }
    }

    /**
     * Класс <class>FieldInstanceToRecodeRuleMergeService</class> реализует сервис обновления правил перекодирования по изменившимся наборам перекодирования
     *
     * @author Nazin Alexander
     */
    private class MetaFieldActualizeService implements IMergeService<RecodeRuleSet, Collection<RecodeRule>, Collection<RecodeRule>> {

        /**
         * Проверяет необходимость и выполняет обновление значения поля перекодирования
         *
         * @param ruleSet набор правил перекодирования
         * @param ruleSetAccessor сервис доступа к МЕТА-полю
         * @param rule правило перекодирования
         * @param ruleAccessor сервис доступа к значению поля
         * @return Возвращает обновленное правило перекодирования
         */
        private RecodeRule doMerge(
                RecodeRuleSet ruleSet, RuleFieldAccessor<MetaFieldNamedPath, MetaField, RecodeRuleSet> ruleSetAccessor,
                RecodeRule rule, RuleFieldAccessor<FieldNamedPath, Field, RecodeRule> ruleAccessor) {
            // Получаем идентификатор МЕТА-поля
            String rrsMetaFieldId = ruleSetAccessor.applyRelativeId(ruleSet);
            // Получаем текущее значение поля перекодирования
            Field oldField = ruleAccessor.apply(rule);
            // Проверяем, что значение поля перекодирования изменилось
            if (!EqualsUtil.equals(rrsMetaFieldId, oldField.getMetaFieldId())) {
                // Получаем уникальное значение поля по идентификаторам МЕТА-поля и записи
                Field newField = ServiceUtils.findUniqueDocumentBy(
                        fieldService,
                        ImmutableMap.<String, FilterCriteriaValue<?>>of(
                                Field.META_FIELD_ID, new FilterCriteriaValue.StringValue(rrsMetaFieldId),
                                Field.NAME, new FilterCriteriaValue.StringValue(oldField.getName())
                        ), true
                );
                // Обновляем значение поля в правиле
                ruleAccessor.inject(rule, newField);
            }
            // Возвращаем обновленное правило
            return rule;
        }

        @Override
        public Collection<RecodeRule> merge(RecodeRuleSet ruleSet, Collection<RecodeRule> existed) {
            ImmutableList.Builder<RecodeRule> result = ImmutableList.builder();
            for (RecodeRule rule : existed) {
                RecodeRule updated = shallowClone(rule);
                // Актуализируем источник
                updated = doMerge(ruleSet, FROM_SET_INJECTOR, updated, FROM_RULE_INJECTOR);
                // Актуализируем назначение
                updated = doMerge(ruleSet, TO_SET_INJECTOR, updated, TO_RULE_INJECTOR);
                // Сохраняем обновленное значение
                result.add(updated);
            }
            // Возвращаем обновленное правило
            return result.build();
        }
    }
}
