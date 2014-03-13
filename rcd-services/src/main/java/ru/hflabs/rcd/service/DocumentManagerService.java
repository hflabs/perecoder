package ru.hflabs.rcd.service;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.accessor.Accessors;
import ru.hflabs.rcd.event.ContextEvent;
import ru.hflabs.rcd.exception.constraint.ConstraintException;
import ru.hflabs.rcd.exception.constraint.DuplicateNameException;
import ru.hflabs.rcd.exception.constraint.IllegalPermissionsException;
import ru.hflabs.rcd.exception.constraint.document.IllegalMetaFieldException;
import ru.hflabs.rcd.exception.constraint.rule.IllegalRecodeRuleException;
import ru.hflabs.rcd.exception.search.document.UnknownDictionaryException;
import ru.hflabs.rcd.model.Essence;
import ru.hflabs.rcd.model.Historical;
import ru.hflabs.rcd.model.Named;
import ru.hflabs.rcd.model.change.ChangeType;
import ru.hflabs.rcd.model.change.HistoryBuilder;
import ru.hflabs.rcd.model.criteria.FilterCriteria;
import ru.hflabs.rcd.model.criteria.FilterCriteriaValue;
import ru.hflabs.rcd.model.document.*;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.path.DictionaryNamedPath;
import ru.hflabs.rcd.model.path.DirectionNamedPath;
import ru.hflabs.rcd.model.path.FieldNamedPath;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;
import ru.hflabs.rcd.model.rule.RecodeRule;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;
import ru.hflabs.rcd.model.rule.Rule;
import ru.hflabs.rcd.service.document.*;
import ru.hflabs.rcd.service.rule.IRecodeRuleService;
import ru.hflabs.rcd.service.rule.IRecodeRuleSetService;
import ru.hflabs.util.core.EqualsUtil;
import ru.hflabs.util.core.MD5;
import ru.hflabs.util.spring.Assert;

import java.util.*;

import static ru.hflabs.rcd.accessor.Accessors.injectName;
import static ru.hflabs.rcd.accessor.Accessors.linkRelative;
import static ru.hflabs.rcd.model.CriteriaUtils.createCriteriaByIDs;
import static ru.hflabs.rcd.model.ModelUtils.*;
import static ru.hflabs.rcd.service.ServiceUtils.injectRelative;
import static ru.hflabs.rcd.service.ServiceUtils.publishChangeEvent;

/**
 * Класс <class>DocumentManagerService</class> реализует сервис управления справочниками
 *
 * @author Nazin Alexander
 */
public class DocumentManagerService implements IManagerService, ApplicationEventPublisherAware {

    /** Функция расчета уникального идентификатора для правила */
    private static final Function<Rule<?, ?, ?>, String> RULE_NAME_FUNCTION = new Function<Rule<?, ?, ?>, String>() {
        @Override
        public String apply(Rule<?, ?, ?> input) {
            return MD5.asHex(input.getFromFieldId(), input.getToFieldId());
        }
    };

    /** Сервис копирования прав доступа для групп */
    private static final IMergeService.Single<Group> MERGE_SERVICE_GROUP = MergeServices.chain(
            MergeServices.<Group>copyId(),
            MergeServices.<Group>copyPermission()
    );
    /** Сервис копирования флагов МЕТА-поля */
    private static final IMergeService.Single<MetaField> MERGE_SERVICE_META_FIELD = MergeServices.chain(
            MergeServices.<MetaField>copyId(),
            MergeServices.<MetaField>copyName(),
            new MergeServices.MetaFieldFlagsMergeService()
    );


    /** Сервис публикации событий */
    private ApplicationEventPublisher eventPublisher;

    /** Сервис работы с историей документов */
    private IHistoryService historyService;

    /** Сервис управления группами справочников */
    private IGroupService groupService;
    /** Сервис управления справочниками */
    private IDictionaryService dictionaryService;
    /** Сервис управления МЕТА-полями справочников */
    private IMetaFieldService metaFieldService;
    /** Сервис управления значениями полей справочников */
    private IFieldService fieldService;
    /** Сервис работы с записями справочника */
    private IRecordService recordService;

    /** Сервис управления наборами правил перекодирования */
    private IRecodeRuleSetService recodeRuleSetService;
    /** Сервис управления правилами перекодирования */
    private IRecodeRuleService rulesService;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    public void setHistoryService(IHistoryService historyService) {
        this.historyService = historyService;
    }

    public void setGroupService(IGroupService groupService) {
        this.groupService = groupService;
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

    public void setRecordService(IRecordService recordService) {
        this.recordService = recordService;
    }

    public void setRecodeRuleSetService(IRecodeRuleSetService recodeRuleSetService) {
        this.recodeRuleSetService = recodeRuleSetService;
    }

    public void setRulesService(IRecodeRuleService rulesService) {
        this.rulesService = rulesService;
    }

    /**
     * Выполняет оповещение слушателей об изменениях
     *
     * @param descriptor дескриптор изменений
     * @param types целевые типы событий
     */
    private <T extends Essence & Historical> void doPublishEvent(HistoryBuilder<T> descriptor, ChangeType... types) {
        publishChangeEvent(eventPublisher, this, descriptor, types);
    }

    /**
     * Выполняет построение дескриптора изменений именованной сущности
     *
     * @param service сервис поиска сущностей
     * @param filterCriteriaValues критерии поиска существующих сущностей
     * @param values коллекция модификации
     * @param mergeService сервис слияния новой и старой сущности
     * @return Возвращает построенный дескриптор
     */
    private <T extends Essence & Named & Historical> HistoryBuilder<T> buildNamedChangeSet(IFilterService<T> service, Map<String, FilterCriteriaValue<?>> filterCriteriaValues, Collection<T> values, IMergeService.Single<T> mergeService) {
        // Выполняем формирование критерия поиска
        final FilterCriteria filterCriteria = new FilterCriteria().injectFilters(filterCriteriaValues);
        // Получаем существующие сущности
        final Map<String, T> existedEssences = Maps.newHashMap(Maps.uniqueIndex(service.findAllByCriteria(filterCriteria, false), LOWER_CASE_NAME_FUNCTION));
        // Выполняем построение дескриптора изменений
        return historyService.createChangeSet(
                service.retrieveTargetClass(),
                existedEssences,
                Maps.uniqueIndex(values, LOWER_CASE_NAME_FUNCTION),
                mergeService
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public Collection<Group> storeGroups(Collection<Group> groups) {
        Assert.isTrue(!CollectionUtils.isEmpty(groups), "Groups must not be NULL or EMPTY");
        // Выполняем построения дескриптора изменений групп
        HistoryBuilder<Group> groupsDescriptor = buildNamedChangeSet(
                groupService,
                ImmutableMap.<String, FilterCriteriaValue<?>>of(
                        Group.NAME, new FilterCriteriaValue.StringsValue(Collections2.transform(groups, NAME_FUNCTION))
                ),
                groups,
                MERGE_SERVICE_GROUP
        );
        // Для модифицированных групп проверяем права на запись
        for (Group group : groupsDescriptor.getEssences(ChangeType.ACTUAL_SET)) {
            if (!hasPermission(group, Group.PERMISSION_WRITE)) {
                throw new IllegalPermissionsException.IllegalWritePermissionsException(
                        String.format("%s with name '%s' does not have write permission", Group.class.getSimpleName(), group.getName())
                );
            }
        }
        // Выполняем создание и обновление групп
        doPublishEvent(groupsDescriptor, ChangeType.CREATE, ChangeType.UPDATE);

        // Для каждой актуальной группы выполняем модификацию ее справочников
        Collection<Group> storedGroups = groupsDescriptor.getEssences(ChangeType.ACTUAL_SET);
        for (Group group : storedGroups) {
            if (!CollectionUtils.isEmpty(group.getDescendants())) {
                HistoryBuilder<Dictionary> dictionariesDescriptor = storeDictionaries(group, group.getDescendants());
                group.setDescendants(dictionariesDescriptor.getEssences(ChangeType.ACTUAL_SET));
            }
        }

        // Возвращаем модифицированные группы
        return storedGroups;
    }

    /**
     * Выполняет создание и изменение коллекции справочников
     *
     * @param group группа, к которой относятся справочники
     * @param dictionaries модифицируемая коллекция справочников
     * @return Возвращает модифицированные справочники
     */
    private HistoryBuilder<Dictionary> storeDictionaries(final Group group, Collection<Dictionary> dictionaries) {
        // Выполняем построение дескриптора изменения справочников
        HistoryBuilder<Dictionary> dictionariesDescriptor = buildNamedChangeSet(
                dictionaryService,
                ImmutableMap.<String, FilterCriteriaValue<?>>of(
                        Dictionary.GROUP_ID, new FilterCriteriaValue.StringValue(group.getId()),
                        Dictionary.NAME, new FilterCriteriaValue.StringsValue(Collections2.transform(dictionaries, NAME_FUNCTION))
                ),
                Collections2.transform(dictionaries, new Function<Dictionary, Dictionary>() {
                    @Override
                    public Dictionary apply(Dictionary input) {
                        return linkRelative(group, input);
                    }
                }),
                MergeServices.chain(
                        MergeServices.<Dictionary>copyId(),
                        MergeServices.<Dictionary>copyName()
                )
        );
        // Выполняем создание и обновление справочников
        doPublishEvent(dictionariesDescriptor, ChangeType.CREATE, ChangeType.UPDATE);

        // Для каждого актуального справочника выполняем модификацию структуры и контента
        for (Dictionary dictionary : dictionariesDescriptor.getEssences(ChangeType.ACTUAL_SET)) {
            if (!CollectionUtils.isEmpty(dictionary.getDescendants())) {
                HistoryBuilder<MetaField> metaFieldsDescriptor = buildMetaFieldChangeSet(dictionary, dictionary.getDescendants());
                doPublishEvent(metaFieldsDescriptor);

                Map<String, MetaField> existedMetaFields = Maps.uniqueIndex(metaFieldsDescriptor.getEssences(ChangeType.ACTUAL_SET), LOWER_CASE_NAME_FUNCTION);
                String primaryMetaFieldName = LOWER_CASE_NAME_FUNCTION.apply(retrievePrimaryMetaField(existedMetaFields.values()));
                dictionary.setDescendants(existedMetaFields.values());

                if (!CollectionUtils.isEmpty(dictionary.getRecords())) {
                    // Выполняем преобразования название МЕТА-полей записи в нижний регистр
                    ImmutableList.Builder<Record> records = ImmutableList.builder();
                    for (Record record : dictionary.getRecords()) {
                        Map<String, Field> fields = Maps.newLinkedHashMap();
                        for (Map.Entry<String, Field> entry : record.getFields().entrySet()) {
                            Assert.isNull(
                                    fields.put(LOWER_CASE_FUNCTION.apply(entry.getKey()), entry.getValue()),
                                    String.format("Duplicate meta field name '%s'", entry.getKey()),
                                    DuplicateNameException.class
                            );
                        }
                        records.add(record.injectFields(fields));
                    }

                    HistoryBuilder<Field> fieldsDescriptor = buildFieldChangeSets(records.build(), existedMetaFields, primaryMetaFieldName);
                    doPublishEvent(fieldsDescriptor);
                    dictionary.setRecords(createRecords(dictionary.getId(), existedMetaFields.values(), fieldsDescriptor.getEssences(ChangeType.ACTUAL_SET)));
                }
            }
        }

        // Возвращаем сформированный дескриптор
        return dictionariesDescriptor;
    }

    /**
     * Выполняет создание и изменение МЕТА-полей справочника
     *
     * @param dictionary справочник, к которому относятся поля
     * @param metaFields модифицируемые значения МЕТА-полей
     * @return Возвращает модифицированные значения полей
     */
    private HistoryBuilder<MetaField> buildMetaFieldChangeSet(final Dictionary dictionary, Collection<MetaField> metaFields) {
        // Проверяем, что только одно МЕТА-поле является основным
        Assert.notNull(
                retrievePrimaryMetaField(metaFields),
                String.format("Dictionary '%s' must have one primary field", dictionary.getName()),
                IllegalMetaFieldException.class
        );
        // Выполняем построение дескриптора изменений МЕТА-полей
        return buildNamedChangeSet(
                metaFieldService,
                ImmutableMap.<String, FilterCriteriaValue<?>>of(MetaField.DICTIONARY_ID, new FilterCriteriaValue.StringValue(dictionary.getId())),
                Collections2.transform(dictionary.getDescendants(), new Function<MetaField, MetaField>() {
                    @Override
                    public MetaField apply(MetaField input) {
                        return linkRelative(dictionary, input);
                    }
                }),
                MERGE_SERVICE_META_FIELD
        );
    }

    /**
     * Выполняет создание и изменение значений полей справочника
     *
     * @param records модифицируемые значения полей
     * @param metaFields коллекция МЕТА-полей
     * @param primaryMetaFieldName название первичного МЕТА-поля
     * @return Возвращает дескриптор изменений полей
     */
    private HistoryBuilder<Field> buildFieldChangeSets(Collection<Record> records, Map<String, MetaField> metaFields, String primaryMetaFieldName) {
        // Коллекция соответствий МЕТА-поля к значениям полей
        final Map<MetaField, Collection<Field>> currentMetaField2Fields = Maps.newLinkedHashMap();
        // Выполняем валидацию каждого записей справочника
        for (Map.Entry<String, MetaField> entry : metaFields.entrySet()) {
            MetaField metaField = entry.getValue();
            final Collection<String> uniqueFieldValidator = metaField.isFlagEstablished(MetaField.FLAG_UNIQUE) ?
                    new HashSet<String>() :
                    new ArrayList<String>();
            final Collection<Field> fields = Lists.newArrayList();
            for (Record record : records) {
                // Получаем первичный ключ записи
                final Field primaryKey = record.retrieveFieldByName(primaryMetaFieldName);
                Assert.notNull(primaryKey, String.format("Record missed primary value for field with name '%s'", primaryMetaFieldName), IllegalMetaFieldException.class);
                // Получаем поле записи
                Field field = record.retrieveFieldByName(entry.getKey());
                Assert.notNull(field, String.format("Record missed value for field with name '%s'", metaField.getName()), IllegalMetaFieldException.class);
                // Устанавливам системные поля, которые идентифицируют запись
                {
                    field = linkRelative(metaField, field);
                    field = injectName(field, createRecordId(primaryKey));
                }
                // Проверяем, что поле не дублируется
                Assert.isTrue(
                        uniqueFieldValidator.add(LOWER_CASE_FUNCTION.apply(field.getValue())),
                        String.format("Duplicate value '%s' for field with name '%s'", field.getValue(), metaField.getName()),
                        ConstraintException.class
                );
                fields.add(field);
            }
            currentMetaField2Fields.put(metaField, fields);
        }

        // Выполняем формирование дескрипторов изменений полей
        final HistoryBuilder<Field> fieldsDescriptor = new HistoryBuilder<Field>(Field.class);
        for (Map.Entry<MetaField, Collection<Field>> entry : currentMetaField2Fields.entrySet()) {
            HistoryBuilder<Field> descriptor = buildNamedChangeSet(
                    fieldService,
                    ImmutableMap.<String, FilterCriteriaValue<?>>of(Field.META_FIELD_ID, new FilterCriteriaValue.StringValue(entry.getKey().getId())),
                    entry.getValue(),
                    MergeServices.<Field>copyId()
            );
            fieldsDescriptor.addChangeSets(descriptor.getChangeSets());
        }

        // Возвращаем сформированный дескриптор
        return fieldsDescriptor;
    }

    /**
     * Выполняет поиск целевых справочников для экспорта
     *
     * @param path именованный путь справочника или <code>NULL</code>
     * @return Возвращает целевые справочники
     */
    private Collection<Dictionary> dumpDictionaries(DictionaryNamedPath path) {
        // Выполняем поиск целевого справочника
        if (path != null && StringUtils.hasText(path.getGroupName()) && StringUtils.hasText(path.getDictionaryName())) {
            return Arrays.asList(dictionaryService.findUniqueByNamedPath(path, false));
        }

        // Выполняем поиск всех справочников для указанной группы
        if (path != null && StringUtils.hasText(path.getGroupName())) {
            Group group = groupService.findUniqueByNamedPath(path.getGroupName(), false);
            return dictionaryService.findAllByRelativeId(group.getId(), null, true);
        }

        // Выполняем поиск всех справочников по указанному имени
        if (path != null && StringUtils.hasText(path.getDictionaryName())) {
            Collection<Dictionary> dictionaries = dictionaryService.findAllByCriteria(createCriteriaByIDs(Dictionary.NAME, path.getDictionaryName()), true);
            if (!CollectionUtils.isEmpty(dictionaries)) {
                return dictionaries;
            }
            throw new UnknownDictionaryException(path.getDictionaryName());
        }

        // Возвращаем все справочники
        return dictionaryService.findAllByCriteria(new FilterCriteria(), true);
    }

    @Override
    public Collection<Group> dumpGroups(DictionaryNamedPath path) {
        // Выполняем заполнение контента справочников
        Collection<Dictionary> dictionaries = dumpDictionaries(path);
        for (Dictionary dictionary : dictionaries) {
            Collection<MetaField> metaFields = injectRelative(
                    metaFieldService.findAllByRelativeId(dictionary.getId(), null, false),
                    fieldService
            );
            dictionary.setDescendants(metaFields);
        }

        // Формируем карту соответствий группы к справочникам
        Map<Group, Collection<Dictionary>> group2dictionaries = Multimaps.index(dictionaries, Accessors.GROUP_TO_DICTIONARY_INJECTOR).asMap();
        for (Map.Entry<Group, Collection<Dictionary>> entry : group2dictionaries.entrySet()) {
            entry.getKey().setDescendants(entry.getValue());
        }
        return Sets.newLinkedHashSet(group2dictionaries.keySet());
    }

    /**
     * Выполняет поиск подходящего контекста для именнованного пути значения
     *
     * @param contexts коллекция существующих контекстов
     * @return Возвращает найденный контекст
     */
    private <NP, I> Context<NP, I> findExistedContext(String prefix, final NP namedPath, Collection<Context<NP, I>> contexts) {
        // Получаем кандидатов
        Collection<Context<NP, I>> candidates = Sets.newHashSet(Collections2.filter(contexts, new Predicate<Context<NP, I>>() {
            @Override
            public boolean apply(Context<NP, I> input) {
                return EqualsUtil.equals(input.getNamedPath(), namedPath);
            }
        }));

        if (candidates.size() == 1) { // подходящий контекст найден
            final Context<NP, I> existedContext = candidates.iterator().next();
            // Проверяем, что МЕТА-поле является уникальным
            Assert.isTrue(
                    existedContext.getDocumentContext().metaField.isFlagEstablished(MetaField.FLAG_UNIQUE),
                    String.format("%s meta field '%s' is not unique", prefix, existedContext.getDocumentContext().metaField.getName()),
                    IllegalRecodeRuleException.class
            );
            return existedContext;
        } else if (candidates.isEmpty()) { // кандидаты не найдены
            throw new IllegalRecodeRuleException(
                    String.format("%s value not found (%s)", prefix, namedPath)
            );
        } else { // найдено несколько кандидатов
            throw new IllegalRecodeRuleException(
                    String.format("Too many %s values found (%s). Expected %d, but got %d", prefix, namedPath, 1, candidates.size())
            );
        }
    }

    /**
     * Выполняет валидацию правила
     *
     * @param rule целевое правило
     * @param contexts коллекция контектов
     * @return Возвращает модифицированное правило
     */
    private <NP extends MetaFieldNamedPath, T extends Essence, R extends Rule<NP, T, R>> R validateRuleByContext(R rule, Collection<Context<NP, T>> contexts) {
        Context<NP, T> fromContext = findExistedContext("source", rule.getFromNamedPath(), contexts);
        rule.injectFrom(fromContext.getEssence());

        Context<NP, T> toContext = findExistedContext("destination", rule.getToNamedPath(), contexts);
        rule.injectTo(toContext.getEssence());

        Assert.isTrue(
                !EqualsUtil.equals(rule.getFromFieldId(), rule.getToFieldId()),
                String.format("Mapping '%s' to itself is not allowed", rule.getFromNamedPath()),
                IllegalRecodeRuleException.class
        );

        return rule;
    }

    /**
     * Выполняет построение дескриптора изменений для правил перекодирования
     *
     * @param recodeRules правила перекодирования
     * @param contexts контексты значений полей
     * @return Возвращает дескриптор изменений
     */
    public HistoryBuilder<RecodeRule> buildRecodeRuleChangeSet(final RecodeRuleSet ruleSet, Collection<RecodeRule> recodeRules, Collection<DocumentContext> contexts) {
        Collection<Context<FieldNamedPath, Field>> fieldContexts = Lists.newArrayList(Collections2.transform(contexts, Contexts.FIELD_CONTEXT));

        // Выполняем валидацию правил
        Map<FieldNamedPath, RecodeRule> validated = Maps.newLinkedHashMap();
        for (RecodeRule rule : recodeRules) {
            rule = validateRuleByContext(rule, fieldContexts);
            if (validated.put(rule.getFromNamedPath(), rule) != null) {
                throw new IllegalRecodeRuleException(String.format("Duplicate source value for rule '%s'", rule));
            }
        }

        // Выполняем построение дескриптора изменений
        Collection<RecodeRule> existedRules = rulesService.findAllByRelativeId(ruleSet.getId(), null, false);
        Map<String, RecodeRule> existedNamedRules = Maps.newHashMap(Maps.uniqueIndex(existedRules, RULE_NAME_FUNCTION));
        return historyService.createChangeSet(
                RecodeRule.class,
                existedNamedRules,
                Maps.uniqueIndex(validated.values(), RULE_NAME_FUNCTION),
                MergeServices.<RecodeRule>copyId()
        );
    }

    /**
     * Выполняет построение дескриптора изменений для наборов правил перекодирования
     *
     * @param recodeRuleSets наборы правил перекодирования
     * @param contexts контексты значений полей
     * @return Возвращает дескриптор изменений
     */
    public HistoryBuilder<RecodeRuleSet> buildRecodeRuleSetChangeSet(Collection<RecodeRuleSet> recodeRuleSets, Collection<DocumentContext> contexts) {
        Collection<Context<MetaFieldNamedPath, MetaField>> metaFieldContexts = Lists.newArrayList(Collections2.transform(contexts, Contexts.META_FIELD_CONTEXT));

        // Выполняем валидацию наборов правил
        Collection<RecodeRuleSet> validated = Lists.newLinkedList();
        for (RecodeRuleSet ruleSet : recodeRuleSets) {
            validated.add(validateRuleByContext(ruleSet, metaFieldContexts));
            if (ruleSet.getDefaultPath() != null) {
                Context<FieldNamedPath, Field> defaultFieldContext = findExistedContext("default", ruleSet.getDefaultPath(), Collections2.transform(contexts, Contexts.FIELD_CONTEXT));
                ruleSet.setDefaultFieldId(defaultFieldContext.getEssence().getId());
            }
        }

        // Выполняем построение дескриптора изменений
        Collection<DirectionNamedPath<MetaFieldNamedPath>> ruleSetPaths = Collections2.transform(validated, new Function<RecodeRuleSet, DirectionNamedPath<MetaFieldNamedPath>>() {
            @Override
            public DirectionNamedPath<MetaFieldNamedPath> apply(RecodeRuleSet input) {
                return createRulePath(input);
            }
        });
        Map<String, RecodeRuleSet> existedRuleSets = Maps.newHashMap(Maps.uniqueIndex(recodeRuleSetService.findRecodeRuleSetByNamedPath(ruleSetPaths, false), RULE_NAME_FUNCTION));
        return historyService.createChangeSet(
                RecodeRuleSet.class,
                existedRuleSets,
                Maps.uniqueIndex(validated, RULE_NAME_FUNCTION),
                MergeServices.chain(
                        MergeServices.<RecodeRuleSet>copyId(),
                        MergeServices.<RecodeRuleSet>copyName()
                )
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public Collection<RecodeRuleSet> storeRecodeRuleSets(Collection<RecodeRuleSet> recodeRuleSets) {
        Assert.isTrue(!CollectionUtils.isEmpty(recodeRuleSets), "Recode rule sets must not be NULL or EMPTY");
        // Получаем коллекцию существующих контекстов
        Collection<DocumentContext> documentContexts = fieldService.findDocumentContexts(createNamedPath(recodeRuleSets));

        // Выполняем построение дескрипторов изменений
        HistoryBuilder<RecodeRuleSet> ruleSetDescriptor = buildRecodeRuleSetChangeSet(recodeRuleSets, documentContexts);
        HistoryBuilder<RecodeRule> ruleDescriptor = new HistoryBuilder<RecodeRule>(RecodeRule.class);
        for (final RecodeRuleSet ruleSet : ruleSetDescriptor.getEssences(ChangeType.ACTUAL_SET)) {
            if (!CollectionUtils.isEmpty(ruleSet.getRecodeRules())) {
                HistoryBuilder<RecodeRule> changeDescriptor = buildRecodeRuleChangeSet(
                        ruleSet,
                        Collections2.transform(ruleSet.getRecodeRules(), new Function<RecodeRule, RecodeRule>() {
                            @Override
                            public RecodeRule apply(RecodeRule input) {
                                return input
                                        .injectRecodeRuleSet(ruleSet)
                                        .injectFromNamedPath(new FieldNamedPath(ruleSet.getFromNamedPath(), input.getFromNamedPath().getFieldValue()))
                                        .injectToNamedPath(new FieldNamedPath(ruleSet.getToNamedPath(), input.getToNamedPath().getFieldValue()));
                            }
                        }),
                        documentContexts
                );
                ruleDescriptor.addChangeSets(changeDescriptor.getChangeSets());
                ruleSet.setRecodeRules(changeDescriptor.getEssences(ChangeType.ACTUAL_SET));
            }
        }

        // Модифицируем правила
        doPublishEvent(ruleSetDescriptor, ChangeType.CREATE, ChangeType.UPDATE);
        doPublishEvent(ruleDescriptor);

        // Возвращаем модифицированные наборы правил
        return recodeRuleSets;
    }

    @Override
    public <T extends ApplicationEvent> void propagateEvent(T event) {
        if (event instanceof ContextEvent) {
            ((ContextEvent) event).overrideSource(this);
            eventPublisher.publishEvent(event);
        }
    }
}
