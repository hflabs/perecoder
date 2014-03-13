package ru.hflabs.rcd.task.performer.synchronization;

import com.google.common.collect.*;
import lombok.AccessLevel;
import lombok.Setter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.Constants;
import ru.hflabs.rcd.exception.constraint.CollisionDataException;
import ru.hflabs.rcd.model.Essence;
import ru.hflabs.rcd.model.Historical;
import ru.hflabs.rcd.model.ModelUtils;
import ru.hflabs.rcd.model.change.*;
import ru.hflabs.rcd.model.criteria.FilterCriteria;
import ru.hflabs.rcd.model.criteria.FilterCriteriaValue;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.path.FieldNamedPath;
import ru.hflabs.rcd.model.rule.RecodeRule;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;
import ru.hflabs.rcd.service.IHistoryService;
import ru.hflabs.rcd.service.ServiceUtils;
import ru.hflabs.rcd.service.document.IDictionaryService;
import ru.hflabs.rcd.service.document.IFieldService;
import ru.hflabs.rcd.service.document.IGroupService;
import ru.hflabs.rcd.service.document.IMetaFieldService;
import ru.hflabs.rcd.service.document.recodeRule.RecodeRuleActualizeService;
import ru.hflabs.rcd.service.document.recodeRuleSet.RecodeRuleSetActualizeService;
import ru.hflabs.rcd.service.rule.IRecodeRuleService;
import ru.hflabs.rcd.service.rule.IRecodeRuleSetService;
import ru.hflabs.rcd.task.performer.TaskPerformerTemplate;
import ru.hflabs.rcd.task.performer.TaskProgressHolder;
import ru.hflabs.rcd.term.Condition;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static ru.hflabs.rcd.accessor.Accessors.FROM_RULE_INJECTOR;
import static ru.hflabs.rcd.accessor.Accessors.TO_RULE_INJECTOR;
import static ru.hflabs.rcd.model.CriteriaUtils.createCriteriaByDocumentIDs;
import static ru.hflabs.rcd.model.ModelUtils.*;
import static ru.hflabs.rcd.service.ServiceUtils.publishChangeEvent;

/**
 * Класс <class>SynchronizationTaskPerformer</class> реализует шаблон задачи синхронизации с внешними источниками
 *
 * @author Nazin Alexander
 */
@Setter
public abstract class SynchronizationTaskPerformer<P extends SynchronizationParameters>
        extends TaskPerformerTemplate<P, SynchronizationResult>
        implements ApplicationEventPublisherAware {

    /** Основной контекст приложения */
    @Setter(AccessLevel.NONE)
    protected ApplicationEventPublisher eventPublisher;

    /** Сервис работы с историей документов */
    protected IHistoryService historyService;

    /** Сервис работы с группами справочников */
    protected IGroupService groupService;
    /** Сервис работы со справочниками */
    protected IDictionaryService dictionaryService;
    /** Сервис работы с МЕТА-полями справочника */
    protected IMetaFieldService metaFieldService;
    /** Сервис работы со значениями полей */
    protected IFieldService fieldService;

    /** Сервис работы с наборами правил перекодирования */
    protected IRecodeRuleSetService recodeRuleSetService;
    /** Сервис работы с правилами перекодирования */
    protected IRecodeRuleService recodeRuleService;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    @Override
    public Class<SynchronizationResult> retrieveResultClass() {
        return SynchronizationResult.class;
    }

    /**
     * Выполняет публикацию событий изменения сущностей
     *
     * @param changeSets наборы изменений
     */
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    protected void doPublishChanges(TaskProgressHolder context, Collection<HistoryBuilder<? extends Historical>> changeSets) {
        TaskProgressHolder innerProgress = new TaskProgressHolder(changeSets.size(), context);
        for (HistoryBuilder<? extends Historical> changeSet : changeSets) {
            changeProgress(innerProgress.nextStep(), "Saving {0}", "save.essence", changeSet.getTargetClass().getSimpleName());
            publishChangeEvent(eventPublisher, this, changeSet);
        }
    }

    /**
     * Выполняет сохранение изменений
     *
     * @param context контекст синхронизации
     * @param documentsHistory дескриптор изменения документов
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class, timeout = 3600)
    protected void doSaveChanges(TaskProgressHolder context, DocumentsHistory documentsHistory) {
        // Публикуем события об изолированном изменении документов
        doPublishChanges(
                context,
                ImmutableList.<HistoryBuilder<? extends Historical>>of(
                        documentsHistory.groups,
                        documentsHistory.dictionaries,
                        documentsHistory.metaFields,
                        documentsHistory.fields
                )
        );
        changeProgress(context, "Actualize recode rules", "rules");
        // Выполняем сихронизацию наборов правил
        Collection<RecodeRuleSet> ruleSets = doSynchronizeRecodeRuleSets(documentsHistory);
        // Выполняем синхронизацию правил
        doSynchronizeRecodeRules(ruleSets, documentsHistory);
    }

    /**
     * Выполняет актуализацию правил перекодирования
     *
     * @param documentsHistory дескриптор изменения документов
     * @return Возвращает модифицированные правила
     */
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    protected Collection<RecodeRule> doSynchronizeRecodeRules(Collection<RecodeRuleSet> ruleSets, DocumentsHistory documentsHistory) {
        ImmutableList.Builder<RecodeRule> result = ImmutableList.builder();
        // Получаем актуальные наборы правил
        Collection<RecodeRuleSet> actualRuleSets = Collections2.filter(ruleSets, Predicates.changeTypes(ChangeType.ACTUAL_SET));
        // Получаем существующие правила перекодирования
        if (!CollectionUtils.isEmpty(actualRuleSets)) {
            Collection<RecodeRule> currentRules = recodeRuleService.findAllByCriteria(
                    createCriteriaByDocumentIDs(RecodeRule.RECODE_RULE_SET_ID, ruleSets), true
            );
            // Выполняем актуализацию
            Collection<RecodeRule> updatedRules = ServiceUtils.updateRulesByDependencies(
                    RecodeRuleActualizeService.BY_FIELD,
                    documentsHistory.fields.getEssences(ChangeType.ACTUAL_SET),
                    currentRules
            );

            ImmutableList.Builder<RecodeRule> toUpdate = ImmutableList.builder();
            ImmutableList.Builder<RecodeRule> toClose = ImmutableList.builder();
            // Получаем закрытые поля
            Collection<String> closedFieldIDs = Collections2.transform(documentsHistory.fields.getEssences(ChangeType.CLOSED_SET), ID_FUNCTION);
            // Выполняем построение карты именованного пути к правилам
            Map<FieldNamedPath, Collection<RecodeRule>> fromNamedPath2rules = Multimaps.index(
                    updatedRules,
                    FROM_RULE_INJECTOR.getNamedPathFunction()
            ).asMap();
            // Выполняем сортировку актуализированных на обновление и закрытие
            for (Collection<RecodeRule> toCheck : fromNamedPath2rules.values()) {
                // Если правила конфликтуют, то выполняем их закрытие
                if (toCheck.size() > 1 && Sets.newHashSet(Collections2.transform(toCheck, TO_RULE_INJECTOR.getNamedPathFunction())).size() != 1) {
                    toClose.addAll(toCheck);
                } else if (!CollectionUtils.isEmpty(closedFieldIDs)) {
                    // Проверяем, что среди актуализированных правил нет закрытых полей
                    for (RecodeRule rule : toCheck) {
                        if (closedFieldIDs.contains(FROM_RULE_FIELD_ID.apply(rule)) || closedFieldIDs.contains(TO_RULE_FIELD_ID.apply(rule))) {
                            toClose.add(rule);
                        } else {
                            toUpdate.add(rule);
                        }
                    }
                } else {
                    // Правила актуальны
                    toUpdate.addAll(toCheck);
                }
            }
            // Выполняем обновление
            result.addAll(recodeRuleService.update(toUpdate.build(), currentRules, false));
            // Выполняем закрытие
            result.addAll(recodeRuleService.close(toClose.build(), false));
        }
        // Возвращаем модифицированные правила
        return result.build();
    }

    /**
     * Выполняет актуализацию наборов правил перекодирования
     *
     * @param documentsHistory дескриптор изменения документов
     * @return Возвращает модифицированные наборы правил
     */
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    protected Collection<RecodeRuleSet> doSynchronizeRecodeRuleSets(DocumentsHistory documentsHistory) {
        ImmutableList.Builder<RecodeRuleSet> result = ImmutableList.builder();

        // Получаем наборы правил для актуальных МЕТА-полей
        Collection<MetaField> actualMetaFields = documentsHistory.metaFields.getEssences(ChangeType.ACTUAL_SET);
        if (!CollectionUtils.isEmpty(actualMetaFields)) {
            Collection<RecodeRuleSet> existedRuleSets = recodeRuleSetService.findAllByCriteria(
                    createCriteriaByDocumentIDs(RecodeRuleSet.FIELD_ID, actualMetaFields), true
            );
            // Выполняем актуализацию наборов правил
            Collection<RecodeRuleSet> updatedRuleSets = ServiceUtils.updateRulesByDependencies(
                    RecodeRuleSetActualizeService.BY_META_FIELD,
                    actualMetaFields,
                    existedRuleSets
            );
            // Выполняем актуализацию поле перекодирования по умолчанию
            updatedRuleSets = ServiceUtils.updateRulesByDependencies(
                    RecodeRuleSetActualizeService.BY_DEFAULT_FIELD,
                    documentsHistory.fields.getEssences(ChangeType.ACTUAL_SET),
                    updatedRuleSets
            );
            // Выполняем обновление наборов правил
            result.addAll(recodeRuleSetService.update(updatedRuleSets, existedRuleSets, false));
        }

        // Выполняем закрытие наборов правил
        Collection<MetaField> closedMetaFields = documentsHistory.metaFields.getEssences(ChangeType.CLOSED_SET);
        if (!CollectionUtils.isEmpty(closedMetaFields)) {
            result.addAll(recodeRuleSetService.closeByCriteria(
                    createCriteriaByDocumentIDs(RecodeRuleSet.FIELD_ID, closedMetaFields)
            ));
        }
        // Возвращаем модифицированные раборы правил
        return result.build();
    }

    /**
     * Выполняет синхронизацию объектов
     *
     * @param context контекст синхронизации
     * @param callback провайдер доступа к объектами
     * @return Возвращает результат синхронизации
     */
    protected <T extends Essence & Historical> HistoryBuilder<T> doSynchronize(TaskProgressHolder context, Collection<T> existed, SynchronizeCallback<T> callback) {
        // Получаем актуальные значения
        Collection<T> targetValues = callback.getTarget();
        Map<String, T> newEssences = CollectionUtils.isEmpty(targetValues) ?
                Collections.<String, T>emptyMap() :
                Maps.uniqueIndex(targetValues, callback.getUniqueFunction());

        // Получаем существующие значения
        Map<String, T> existedEssences = CollectionUtils.isEmpty(existed) ?
                Collections.<String, T>emptyMap() :
                Maps.newHashMap(Maps.uniqueIndex(existed, callback.getUniqueFunction()));

        // Формируем дескриптор изменения
        return historyService.createChangeSet(
                callback.retrieveTargetClass(),
                context.date,
                context.author,
                existedEssences,
                newEssences,
                callback.getMergeFunction()
        );
    }

    /**
     * Выполняет сихронизацию записей справочника
     *
     * @param context контекст синхронизации
     * @param documentsHistory дескриптор изменений документов
     * @param dictionary целевой справочник
     * @param callback провайдер доступа к актуальным значениям записей
     * @return Возвращает модифицированный дескриптор изменений документов
     */
    protected DocumentsHistory doSynchronizeRecords(TaskProgressHolder context, DocumentsHistory documentsHistory, Dictionary dictionary, SynchronizeRecordCallback callback) {
        // Получаем текущие значения МЕТА-полей
        Collection<MetaField> existedMetaFields = metaFieldService.findAllByRelativeId(dictionary.getId(), null, false);
        // Выполняем синхронизацию МЕТА-полей
        Map<ChangeType, ChangeSet<MetaField>> metaFieldChangeSets = doSynchronize(
                context,
                existedMetaFields,
                callback.getMetaFields(dictionary)
        ).getChangeSets();
        documentsHistory.metaFields.addChangeSets(metaFieldChangeSets);

        // Для каждого МЕТА-полей выполняем синхронизацию его значений
        for (ChangeSet<MetaField> changeSet : metaFieldChangeSets.values()) {
            for (MetaField metaField : changeSet.getChanged()) {
                // Получаем текущие значения полей
                Collection<Field> existedFields = fieldService.findAllByRelativeId(metaField.getId(), null, false);
                // Выполняем синхронизацию значений полей
                documentsHistory.fields.addChangeSets(doSynchronize(
                        context,
                        existedFields,
                        callback.getFields(metaField)
                ).getChangeSets());
            }
        }
        // Возвращаем модифицированный дескриптор изменений
        return documentsHistory;
    }

    /**
     * Проверяет, что не существует групп, которые не относятся к ЦНСИ с таким же названием
     *
     * @param groups коллекция групп для проверки
     * @throws CollisionDataException Исключительная ситуация при проверке коллизий
     */
    protected void checkGroupsCollision(Collection<Group> groups, Condition condition) throws CollisionDataException {
        if (!CollectionUtils.isEmpty(groups)) {
            FilterCriteria collisionCriteria = new FilterCriteria()
                    .injectFilters(
                            ImmutableMap.<String, FilterCriteriaValue<?>>of(
                                    Group.NAME, new FilterCriteriaValue.StringsValue(Collections2.transform(groups, NAME_FUNCTION)),
                                    Group.OWNER, new FilterCriteriaValue.StringValue(Constants.ESNSI_OWNER_NAME).injectCondition(condition)
                            )

                    );
            // Выполняем поиск существующих групп
            Collection<Group> existedGroups = groupService.findAllByCriteria(collisionCriteria, false);
            // Проверяем результат
            if (!CollectionUtils.isEmpty(existedGroups)) {
                String groupNames = StringUtils.collectionToCommaDelimitedString(Collections2.transform(existedGroups, NAME_FUNCTION));
                throw new CollisionDataException(
                        String.format("Synchronization forcibly aborted. Found conflicting with CNSI groups: %s", groupNames)
                );
            }
        }
    }

    /**
     * Класс <class>DocumentsHistory</class> содержит набор дескрипторов документов
     *
     * @author Nazin Alexander
     */
    protected static class DocumentsHistory {
        /** Изменения групп справочников */
        public final HistoryBuilder<Group> groups;
        /** Изменения справочников */
        public final HistoryBuilder<Dictionary> dictionaries;
        /** Изменения МЕТА-полей */
        public final HistoryBuilder<MetaField> metaFields;
        /** Изменения значений полей */
        public final HistoryBuilder<Field> fields;

        public DocumentsHistory() {
            this.groups = new HistoryBuilder<>(Group.class, ChangeMode.ISOLATED);
            this.dictionaries = new HistoryBuilder<>(Dictionary.class, ChangeMode.ISOLATED);
            this.metaFields = new HistoryBuilder<>(MetaField.class, ChangeMode.ISOLATED);
            this.fields = new HistoryBuilder<>(Field.class, ChangeMode.ISOLATED);
        }
    }

    /**
     * Интерфейс <class>SynchronizeRecordCallback</class> декларирует методы для получения информации о синхронизируемых записях справочника
     *
     * @author Nazin Alexander
     */
    public interface SynchronizeRecordCallback {

        /**
         * Возвращает провайдера синхронизации МЕТА-полей
         *
         * @param dictionary целевой справочник
         */
        SynchronizeCallback<MetaField> getMetaFields(Dictionary dictionary);

        /**
         * Возвращает провайдера синхронизации значений полей
         *
         * @param metaField целевое МЕТА-поле
         */
        SynchronizeCallback<Field> getFields(MetaField metaField);
    }

    /**
     * Класс <class>SynchronizeEmptyRecordCallback</class> реализует провайдер доступа к пустым записям
     *
     * @author Nazin Alexander
     */
    public final class SynchronizeEmptyRecordCallback implements SynchronizeRecordCallback {

        @Override
        public SynchronizeCallback<MetaField> getMetaFields(Dictionary dictionary) {
            return new SynchronizeCallback.Empty<>(MetaField.class, ModelUtils.<MetaField>idFunction());
        }

        @Override
        public SynchronizeCallback<Field> getFields(MetaField metaField) {
            return new SynchronizeCallback.Empty<>(Field.class, ModelUtils.<Field>idFunction());
        }
    }

}
