package ru.hflabs.rcd.service.document.recodeRuleSet;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.event.modify.ChangeEvent;
import ru.hflabs.rcd.exception.constraint.rule.AttachedRecodeRuleException;
import ru.hflabs.rcd.exception.search.rule.UnknownRecodeRuleSetException;
import ru.hflabs.rcd.exception.search.rule.UnknownRuleSetNameException;
import ru.hflabs.rcd.model.Essence;
import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.change.ChangeType;
import ru.hflabs.rcd.model.criteria.FilterCriteria;
import ru.hflabs.rcd.model.criteria.FilterCriteriaValue;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.path.DirectionNamedPath;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;
import ru.hflabs.rcd.model.rule.RecodeRule;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;
import ru.hflabs.rcd.service.document.DocumentServiceTemplate;
import ru.hflabs.rcd.service.document.IDictionaryService;
import ru.hflabs.rcd.service.document.IFieldService;
import ru.hflabs.rcd.service.document.IMetaFieldService;
import ru.hflabs.rcd.service.rule.IRecodeRuleService;
import ru.hflabs.rcd.service.rule.IRecodeRuleSetService;
import ru.hflabs.rcd.term.Condition;
import ru.hflabs.util.spring.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static ru.hflabs.rcd.accessor.Accessors.FROM_SET_INJECTOR;
import static ru.hflabs.rcd.accessor.Accessors.TO_SET_INJECTOR;
import static ru.hflabs.rcd.model.CriteriaUtils.createCriteriaByDocumentIDs;
import static ru.hflabs.rcd.model.CriteriaUtils.createCriteriaByIDs;
import static ru.hflabs.rcd.model.ModelUtils.validateDictionaryNamedPath;
import static ru.hflabs.rcd.model.change.Predicates.CHANGE_NAME_PREDICATE;
import static ru.hflabs.rcd.service.ServiceUtils.*;

/**
 * Класс <class>RecodeRuleSetService</class> реализует сервис работы с наборами правил перекодирования
 *
 * @author Nazin Alexander
 */
public class RecodeRuleSetService extends DocumentServiceTemplate<RecodeRuleSet> implements IRecodeRuleSetService {

    /** Сервис работы со справочниками */
    private IDictionaryService dictionaryService;
    /** Сервис работы с МЕТА-полями */
    private IMetaFieldService metaFieldService;
    /** Сервис работы со значениями полей */
    private IFieldService fieldService;
    /** Сервис работы с правилами перекодирования */
    private IRecodeRuleService recodeRuleService;

    public RecodeRuleSetService() {
        super(RecodeRuleSet.class);
    }

    public void setDictionaryService(IDictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setMetaFieldService(IMetaFieldService metaFieldService) {
        this.metaFieldService = metaFieldService;
    }

    public void setFieldService(IFieldService fieldService) {
        this.fieldService = fieldService;
    }

    public void setRecodeRuleService(IRecodeRuleService recodeRuleService) {
        this.recodeRuleService = recodeRuleService;
    }

    @Override
    protected Collection<RecodeRuleSet> injectTransitiveDependencies(Collection<RecodeRuleSet> objects) {
        return super.injectTransitiveDependencies(injectRuleRelations(injectRelations(objects, fieldService), metaFieldService, FROM_SET_INJECTOR, TO_SET_INJECTOR));
    }

    @Override
    public RecodeRuleSet findUniqueByNamedPath(String path, boolean quietly) {
        Assert.isTrue(StringUtils.hasText(path), "RecodeRuleSet name must not be NULL or EMPTY");
        RecodeRuleSet result = findUniqueDocumentBy(this, createCriteriaByIDs(RecodeRuleSet.NAME, path), true);
        if (result == null && !quietly) {
            throw new UnknownRuleSetNameException(path);
        }
        return result;
    }

    @Override
    public RecodeRuleSet findRecodeRuleSetByNamedPath(MetaFieldNamedPath fromPath, MetaFieldNamedPath toPath, boolean fillTransitive, boolean quietly) {
        Collection<RecodeRuleSet> ruleSets = findRecodeRuleSetByNamedPath(
                Arrays.asList(new DirectionNamedPath<>(fromPath, toPath)),
                fillTransitive);
        // Выполняем поиск набора правил
        RecodeRuleSet ruleSet = extractSingleDocument(ruleSets, null);
        // Проверяем, что набор правил найден
        if (ruleSet != null) {
            return ruleSet;
        } else if (!quietly) {
            // Пытаемся определить причину отсутствия набора правил, опираясь на то, что не найден один из справочников
            dictionaryService.findUniqueByNamedPath(fromPath, false);
            dictionaryService.findUniqueByNamedPath(toPath, false);
            // Справочники найдены, но для них не составлен набор правил
            throw new UnknownRecodeRuleSetException(fromPath, toPath);
        } else {
            return null;
        }
    }

    @Override
    public Collection<RecodeRuleSet> findRecodeRuleSetByNamedPath(Collection<DirectionNamedPath<MetaFieldNamedPath>> rulePaths, boolean fillTransitive) {
        ImmutableMap.Builder<String, FilterCriteriaValue<?>> builder = ImmutableMap.builder();
        // Формируем критерий для каждого именованного пути
        for (DirectionNamedPath<MetaFieldNamedPath> path : rulePaths) {
            validateDictionaryNamedPath(path.first);
            validateDictionaryNamedPath(path.second);
            ImmutableMap.Builder<String, FilterCriteriaValue<?>> clauseBuilder = ImmutableMap.<String, FilterCriteriaValue<?>>builder()
                    .put(RecodeRuleSet.FROM_GROUP_NAME, new FilterCriteriaValue.StringValue(path.first.getGroupName()))
                    .put(RecodeRuleSet.FROM_DICTIONARY_NAME, new FilterCriteriaValue.StringValue(path.first.getDictionaryName()))
                    .put(RecodeRuleSet.TO_GROUP_NAME, new FilterCriteriaValue.StringValue(path.second.getGroupName()))
                    .put(RecodeRuleSet.TO_DICTIONARY_NAME, new FilterCriteriaValue.StringValue(path.second.getDictionaryName()));
            // Добавляем фильтры по МЕТА-полям
            clauseBuilder = StringUtils.hasText(path.first.getFieldName()) ?
                    clauseBuilder.put(RecodeRuleSet.FROM_FIELD_NAME, new FilterCriteriaValue.StringValue(path.first.getFieldName())) :
                    clauseBuilder;
            clauseBuilder = StringUtils.hasText(path.second.getFieldName()) ?
                    clauseBuilder.put(RecodeRuleSet.TO_FIELD_NAME, new FilterCriteriaValue.StringValue(path.second.getFieldName())) :
                    clauseBuilder;
            builder.put(path.toString(), new FilterCriteriaValue.ClauseValue(clauseBuilder.build()).injectCondition(Condition.OR));
        }
        // Формируем критерий поиска
        FilterCriteria filterCriteria = new FilterCriteria()
                .injectCount(rulePaths.size() + 1)
                .injectFilters(builder.build());
        // Выполняем поиск наборов правил
        return findByCriteria(filterCriteria, fillTransitive).getResult();
    }

    /**
     * Проверяет, что для справочника заданы все перекодировки для каждого набора правил
     *
     * @param dictionary проверяемый справочник
     * @param recodeRuleSets проверяемые наборы перекодировок
     * @return Возвращает флаг проверки
     */
    private boolean isDictionaryUnmatched(Dictionary dictionary, Collection<RecodeRuleSet> recodeRuleSets) {
        // Получаем первичное МЕТА-поле
        MetaField primaryMetaField = metaFieldService.findPrimaryMetaField(dictionary.getId(), false, false);
        // Получаем количество записей справочника
        int recordsCount = fieldService.countByCriteria(
                createCriteriaByIDs(Field.META_FIELD_ID, primaryMetaField.getId())
        );
        // Для каждого набора проверяем, что количество перекодировок соответствует количеству записей справочника
        for (RecodeRuleSet ruleSet : recodeRuleSets) {
            // Если не задано правило перекодирования по умолчанию, то проверяем, что
            // количество записей справочника равно количеству перекодировок
            if (!StringUtils.hasText(ruleSet.getDefaultFieldId())) {
                int recodesCount = recodeRuleService.countByCriteria(
                        createCriteriaByIDs(RecodeRule.RECODE_RULE_SET_ID, ruleSet.getId())
                );
                if (recordsCount != recodesCount) {
                    return true;
                }
            }
        }
        // Для всех наборов заданы все правила
        return false;
    }

    @Override
    public Set<Dictionary> findUnmatchedDictionaries(String groupId, boolean fillTransitive) {
        Assert.isTrue(StringUtils.hasText(groupId), "Group id must not be NULL or EMPTY");
        final Collection<Dictionary> dictionaries = dictionaryService.findAllByRelativeId(groupId, null, fillTransitive);
        final Set<Dictionary> result = Sets.newHashSet();
        // Выполняет итерирование всех справочников для указанной группы
        for (Dictionary dictionary : dictionaries) {
            // Получаем наборы перекодировок для справочника, где он является источником и назначением
            Collection<RecodeRuleSet> fromRecodeRuleSets = findAllByCriteria(createCriteriaByIDs(RecodeRuleSet.FROM_DICTIONARY_ID, dictionary.getId()), false);
            Collection<RecodeRuleSet> toRecodeRuleSets = findAllByCriteria(createCriteriaByIDs(RecodeRuleSet.TO_DICTIONARY_ID, dictionary.getId()), false);
            // Проверяем, что справочник участвует в перекодировках
            if (CollectionUtils.isEmpty(fromRecodeRuleSets) && CollectionUtils.isEmpty(toRecodeRuleSets)) {
                result.add(dictionary);
            } else if (!CollectionUtils.isEmpty(fromRecodeRuleSets) && isDictionaryUnmatched(dictionary, fromRecodeRuleSets)) {
                result.add(dictionary);
            }
        }
        return result;
    }

    /**
     * Выполняет актуализацию наборов правил перекодирования
     *
     * @param service сервис актуализации
     * @param changed коллекция изменившихся зависимостей
     * @param fieldName названия поля для поиска существующих правил
     */
    private <T extends Essence> Collection<RecodeRuleSet> doUpdateByDependencies(RecodeRuleSetActualizeService<T> service, Collection<T> changed, String fieldName) {
        // Получаем существующие наборы
        Collection<RecodeRuleSet> existedRules = findAllByCriteria(createCriteriaByDocumentIDs(fieldName, changed), true);
        // Выполняем актуализацию
        Collection<RecodeRuleSet> updatedRules = updateRulesByDependencies(service, changed, existedRules);
        // Выполняем обновление
        return update(updatedRules, existedRules, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public <T> Collection<RecodeRuleSet> modifyByDependencies(Class<T> dependencyClass, Collection<T> dependencies) {
        if (!CollectionUtils.isEmpty(dependencies)) {
            if (Group.class.equals(dependencyClass)) {
                return doUpdateByDependencies(
                        RecodeRuleSetActualizeService.BY_GROUP,
                        (Collection<Group>) dependencies,
                        Dictionary.GROUP_ID
                );
            } else if (Dictionary.class.equals(dependencyClass)) {
                return doUpdateByDependencies(
                        RecodeRuleSetActualizeService.BY_DICTIONARY,
                        (Collection<Dictionary>) dependencies,
                        MetaField.DICTIONARY_ID
                );
            } else if (MetaField.class.equals(dependencyClass)) {
                return doUpdateByDependencies(
                        RecodeRuleSetActualizeService.BY_META_FIELD,
                        (Collection<MetaField>) dependencies,
                        RecodeRuleSet.FIELD_ID
                );
            }
        }
        return Collections.emptyList();
    }

    /**
     * Выполняет проверку привязанных документов к существующим правилам перекодирования
     *
     * @param changeType тип изменений
     * @param changeClass целевой класс документов
     * @param targetField проверяемое поля правил перекодирования
     * @param documents коллекция проверяемых документов
     * @throws AttachedRecodeRuleException Исключительная ситуация
     */
    private <T extends Identifying> void checkAttachedRules(ChangeType changeType, Class<T> changeClass, String targetField, Collection<T> documents) throws AttachedRecodeRuleException {
        if (!CollectionUtils.isEmpty(documents)) {
            int existedRulesCount = countByCriteria(createCriteriaByDocumentIDs(targetField, documents));
            Assert.isTrue(
                    existedRulesCount == 0,
                    String.format("Can't %s %s. Cause by: found %d attached recode rule sets",
                            changeType.name().toLowerCase(),
                            changeClass.getSimpleName(),
                            existedRulesCount
                    ),
                    AttachedRecodeRuleException.class
            );
        }
    }

    @Override
    protected void handleOtherUpdateEvent(ChangeEvent event) {
        if (Group.class.equals(event.getChangedClass())) {
            modifyByDependencies(Group.class, event.getChangedByPredicate(Group.class, CHANGE_NAME_PREDICATE));
        } else if (Dictionary.class.equals(event.getChangedClass())) {
            modifyByDependencies(Dictionary.class, event.getChangedByPredicate(Dictionary.class, CHANGE_NAME_PREDICATE));
        } else if (MetaField.class.equals(event.getChangedClass())) {
            modifyByDependencies(MetaField.class, event.getChangedByPredicate(MetaField.class, CHANGE_NAME_PREDICATE));
        }
    }

    @Override
    protected void handleOtherCloseEvent(ChangeEvent event) {
        if (MetaField.class.equals(event.getChangedClass())) {
            checkAttachedRules(event.getChangeType(), MetaField.class, RecodeRuleSet.FIELD_ID, event.getChanged(MetaField.class));
        } else if (Field.class.equals(event.getChangedClass())) {
            checkAttachedRules(event.getChangeType(), Field.class, RecodeRuleSet.DEFAULT_FIELD_ID, event.getChanged(Field.class));
        }
    }
}
