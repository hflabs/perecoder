package ru.hflabs.rcd.soap;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.Setter;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.Version;
import ru.hflabs.rcd.event.recode.RecodeFailedEvent;
import ru.hflabs.rcd.event.recode.RecodeSuccessEvent;
import ru.hflabs.rcd.exception.ApplicationException;
import ru.hflabs.rcd.exception.constraint.IllegalPrimaryKeyException;
import ru.hflabs.rcd.exception.search.document.UnknownDictionaryException;
import ru.hflabs.rcd.exception.search.document.UnknownFieldException;
import ru.hflabs.rcd.exception.search.document.UnknownGroupException;
import ru.hflabs.rcd.exception.search.rule.UnknownRecodeRuleException;
import ru.hflabs.rcd.exception.search.rule.UnknownRecodeRuleSetException;
import ru.hflabs.rcd.exception.search.rule.UnknownRuleSetNameException;
import ru.hflabs.rcd.model.criteria.FilterCriteria;
import ru.hflabs.rcd.model.criteria.FilterCriteriaValue;
import ru.hflabs.rcd.model.criteria.FilterResult;
import ru.hflabs.rcd.model.document.*;
import ru.hflabs.rcd.model.notification.NotifyType;
import ru.hflabs.rcd.model.path.DictionaryNamedPath;
import ru.hflabs.rcd.model.path.FieldNamedPath;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;
import ru.hflabs.rcd.model.rule.RecodeRule;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;
import ru.hflabs.rcd.model.rule.Rule;
import ru.hflabs.rcd.service.document.*;
import ru.hflabs.rcd.service.rule.IRecodeRuleService;
import ru.hflabs.rcd.service.rule.IRecodeRuleSetService;
import ru.hflabs.rcd.soap.model.*;
import ru.hflabs.rcd.term.Condition;
import ru.hflabs.util.core.FormatUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.Map;

import static ru.hflabs.rcd.model.CriteriaUtils.createCriteriaByIDs;
import static ru.hflabs.rcd.model.CriteriaUtils.createCriteriaByRelative;
import static ru.hflabs.rcd.model.ModelUtils.*;
import static ru.hflabs.rcd.service.ServiceUtils.extractSingleDocument;
import static ru.hflabs.rcd.service.ServiceUtils.findOneDocumentBy;
import static ru.hflabs.rcd.soap.mapper.ThrowableMapper.createError;

/**
 * Класс <class>RecodeWebService</class> реализует WEB сервис перекодировки справочников
 *
 * @author Nazin Alexander
 * @see WService
 */
@Setter
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class RecodeWebService implements ApplicationEventPublisherAware, WService {

    private static final Logger LOG = LoggerFactory.getLogger(RecodeWebService.class);

    /** Фабрика создания SOAP классов */
    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    /** Сервис публикации событий */
    @Setter(AccessLevel.NONE)
    private ApplicationEventPublisher eventPublisher;

    /** Сервис преобразования сущности SOAP в API модель */
    private Mapper mapper;

    /** Сервис работы с группами справочников */
    private IGroupService groupService;
    /** Сервис работы со справочниками */
    private IDictionaryService dictionaryService;
    /** Сервис работы с МЕТА-полями справочника */
    private IMetaFieldService metaFieldService;
    /** Сервис работы со значениями полей */
    private IFieldService fieldService;
    /** Сервис работы с записями справочника */
    private IRecordService recordService;
    /** Сервис работы с наборами правил перекодирования */
    private IRecodeRuleSetService recodeRuleSetService;
    /** Сервис работы с правилами перекодирования */
    private IRecodeRuleService recodeRuleService;

    /*
     * Сервисы конвертации
     */
    private Function<Group, WGroup> toWGroupTransformer;
    private Function<Dictionary, WDictionary> toWDictionaryTransformer;
    private Function<MetaField, WMetaField> toWMetaFieldTransformer;
    private Function<Record, WRecord> toWRecordTransformer;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    @GET
    @Path("/version")
    @Override
    public VersionResponse version() {
        final VersionResponse response = OBJECT_FACTORY.createVersionResponse();
        response.setVersion(Version.getVersion());
        response.setRevision(Version.getRevision());
        return response;
    }

    /**
     * Определяет {@link NotifyType тип оповещения} по исключительной ситуации
     *
     * @param exception исключительная ситуация
     * @return Возвращает тип оповещения
     */
    private static NotifyType determineNotifyType(Throwable exception) {
        if (exception instanceof UnknownRecodeRuleException) { // правило перекодирования не найдено
            return NotifyType.NO_RULE;
        } else if (exception instanceof UnknownRecodeRuleSetException) { // набор правил перекодирования не найден
            return NotifyType.NO_RULE_SET;
        } else if (exception instanceof UnknownRuleSetNameException) {
            return NotifyType.NO_RULE_ALIAS;
        } else if (exception instanceof UnknownFieldException) { // значение поля не найдено
            return NotifyType.NO_VALUE;
        } else if (exception instanceof UnknownDictionaryException) { // справочник не найден
            return NotifyType.NO_DICTIONARY;
        } else if (exception instanceof UnknownGroupException) { // группа справочников не найдена
            return NotifyType.NO_GROUP;
        } else { // необработанная ошибка
            return NotifyType.ERROR;
        }
    }

    /**
     * Выполняет поиск целевого поля перекодирования
     *
     * @param ruleSet целевой набор правил
     * @param fromPath исходный путь записи
     * @param toPath целевой путь записи
     * @return Возвращает найденное целевое поле перекодировани
     */
    private Field doRecode(RecodeRuleSet ruleSet, FieldNamedPath fromPath, MetaFieldNamedPath toPath) throws Exception {
        // Пытаемся определить существующее правило
        RecodeRule rule = findOneDocumentBy(
                recodeRuleService,
                createCriteriaByRelative(RecodeRule.RECODE_RULE_SET_ID, ruleSet.getId(), RecodeRule.VALUE, fromPath.getFieldValue()),
                false
        );
        // Определяем целевую запись
        if (rule != null) { // если найдено конкретное правило перекодирования
            return fieldService.findByID(rule.getToFieldId(), false, false);
        } else {
            // Получаем значение поля по умолчанию
            Field defaultField = (ruleSet.getDefaultFieldId() != null) ?
                    fieldService.findByID(ruleSet.getDefaultFieldId(), false, false) :
                    null;

            boolean isFromFieldExist = fieldService.isFieldExist(ruleSet.getFromFieldId(), fromPath.getFieldValue());
            // Возвращаем результат в зависимости от состояния исходного поля и значения по умолчанию
            if (isFromFieldExist && defaultField != null) {
                return defaultField;
            } else if (!isFromFieldExist && defaultField != null) {
                eventPublisher.publishEvent(
                        new RecodeFailedEvent(
                                this,
                                ruleSet.getName(),
                                fromPath,
                                toPath,
                                NotifyType.NO_VALUE,
                                new UnknownFieldException(fromPath.toString())
                        )
                );
                return defaultField;
            } else if (isFromFieldExist) {
                throw new UnknownRecodeRuleException(fromPath, toPath);
            } else {
                throw new UnknownFieldException(fromPath.toString());
            }
        }
    }

    /**
     * Выполняет поиск целевого значения поля
     *
     * @param ruleSet целевой набор правил
     * @param fromValue исходное значение поля
     * @return Возвращает найденное целевое значение поля
     * @throws ErrorResponse Исключительная ситуация при выполнении перекодировки
     */
    private RecodeResponse doRecode(RecodeRuleSet ruleSet, String fromValue) throws ErrorResponse {
        RecodeResponse recodeResponse = OBJECT_FACTORY.createRecodeResponse();
        final FieldNamedPath fromPath = new FieldNamedPath(ruleSet.getFromNamedPath(), fromValue);
        final MetaFieldNamedPath toPath = ruleSet.getToNamedPath();
        try {
            Field targetField = doRecode(ruleSet, fromPath, toPath);
            String result = targetField.getValue();

            eventPublisher.publishEvent(new RecodeSuccessEvent(this, ruleSet.getName(), fromPath, new FieldNamedPath(toPath, result)));
            recodeResponse.setValue(FormatUtil.format(result));
            return recodeResponse;
        } catch (Throwable th) {
            NotifyType notifyType = determineNotifyType(th);
            eventPublisher.publishEvent(new RecodeFailedEvent(this, ruleSet.getName(), fromPath, toPath, notifyType, th));
            throw createErrorResponse(notifyType, th);
        }
    }

    @POST
    @Path("/recode")
    @Override
    public RecodeResponse recode(RecodeRequest parameters) throws ErrorResponse {
        Assert.notNull(parameters, "Request parameters must not be NULL");
        WRecodeCriteria criteria = parameters.getCriteria();
        final FieldNamedPath fromPath = new FieldNamedPath(
                criteria.getFromGroup(),
                criteria.getFromDictionary(),
                null,
                criteria.getFromValue()
        );
        final MetaFieldNamedPath toPath = new MetaFieldNamedPath(
                criteria.getToGroup(),
                StringUtils.hasText(criteria.getToDictionary()) ? criteria.getToDictionary() : criteria.getFromDictionary(),
                null
        );

        validateDictionaryNamedPath(fromPath);
        validateDictionaryNamedPath(toPath);
        try {
            RecodeRuleSet ruleSet = recodeRuleSetService.findRecodeRuleSetByNamedPath(fromPath, toPath, false, false);
            return doRecode(ruleSet, criteria.getFromValue());
        } catch (ErrorResponse ex) {
            throw ex;
        } catch (Throwable th) {
            NotifyType notifyType = determineNotifyType(th);
            eventPublisher.publishEvent(new RecodeFailedEvent(this, null, fromPath, toPath, determineNotifyType(th), th));
            throw createErrorResponse(notifyType, th);
        }
    }

    @POST
    @Path("/recodeByAlias")
    @Override
    public RecodeResponse recodeByAlias(RecodeByAliasRequest parameters) throws ErrorResponse {
        Assert.notNull(parameters, "Request parameters must not be NULL");
        String alias = parameters.getAlias();
        Assert.isTrue(StringUtils.hasText(alias), "Rule set alias must be not empty");

        try {
            RecodeRuleSet ruleSet = recodeRuleSetService.findUniqueByNamedPath(alias, false);
            return doRecode(ruleSet, parameters.getFromValue());
        } catch (ErrorResponse ex) {
            throw ex;
        } catch (Throwable th) {
            NotifyType notifyType = determineNotifyType(th);
            eventPublisher.publishEvent(new RecodeFailedEvent(this, alias, null, null, notifyType, th));
            throw createErrorResponse(notifyType, th);
        }
    }

    @POST
    @Path("/getGroups")
    @Override
    public SearchGroupsResponse getGroups(SearchGroupsRequest parameters) throws ErrorResponse {
        Assert.notNull(parameters, "Request parameters must not be NULL");
        FilterCriteria filterCriteria = createFilterCriteria(parameters.getCriteria());
        try {
            FilterResult<Group> filterResult = groupService.findByCriteria(filterCriteria, false);

            SearchGroupsResponse response = createSearchResponse(filterResult, OBJECT_FACTORY.createSearchGroupsResponse());
            response.getGroup().addAll(Collections2.transform(filterResult.getResult(), toWGroupTransformer));
            return response;
        } catch (ApplicationException ex) {
            throw createErrorResponse(NotifyType.ERROR, ex, LocationAwareLogger.DEBUG_INT);
        } catch (Throwable th) {
            throw createErrorResponse(NotifyType.ERROR, th);
        }
    }

    @GET
    @Path("getDictionary/{id}")
    @Override
    public WDictionary getDictionary(@PathParam("id") String id) throws ErrorResponse {
        Assert.isTrue(StringUtils.hasText(id), "Dictionary ID must not be NULL or EMPTY");
        try {
            Dictionary dictionary = dictionaryService.findByID(id, true, false);
            dictionary.setDescendants(metaFieldService.findAllByRelativeId(dictionary.getId(), null, false));

            return toWDictionaryTransformer.apply(dictionary);
        } catch (IllegalPrimaryKeyException ex) {
            throw createErrorResponse(NotifyType.NO_DICTIONARY, ex, LocationAwareLogger.DEBUG_INT);
        } catch (Throwable th) {
            throw createErrorResponse(NotifyType.ERROR, th);
        }
    }

    @POST
    @Path("/getDictionaries")
    @Override
    public SearchDictionariesResponse getDictionaries(SearchDictionariesRequest parameters) throws ErrorResponse {
        Assert.notNull(parameters, "Request parameters must not be NULL");

        FilterCriteria filterCriteria = createFilterCriteria(parameters.getCriteria());
        try {
            String search = filterCriteria.getSearch();
            if (StringUtils.hasText(search)) {
                Collection<String> targetGroupIDs = Collections2.transform(
                        groupService.findAllByCriteria(new FilterCriteria().injectSearch(search), false),
                        ID_FUNCTION
                );
                if (!CollectionUtils.isEmpty(targetGroupIDs)) {
                    filterCriteria = filterCriteria
                            .injectSearch(search, Condition.OR)
                            .injectFilters(
                                    ImmutableMap.<String, FilterCriteriaValue<?>>of(
                                            Dictionary.GROUP_ID, new FilterCriteriaValue.StringsValue(targetGroupIDs).injectCondition(Condition.OR)
                                    )
                            );
                }
            }

            FilterResult<Dictionary> filterResult = dictionaryService.findByCriteria(filterCriteria, true);

            SearchDictionariesResponse response = createSearchResponse(filterResult, OBJECT_FACTORY.createSearchDictionariesResponse());
            response.getDictionary().addAll(Collections2.transform(filterResult.getResult(), new Function<Dictionary, WDictionary>() {
                @Override
                public WDictionary apply(Dictionary input) {
                    input.setDescendants(metaFieldService.findAllByRelativeId(input.getId(), null, false));
                    return toWDictionaryTransformer.apply(input);
                }
            }));
            return response;
        } catch (ApplicationException ex) {
            throw createErrorResponse(NotifyType.ERROR, ex, LocationAwareLogger.DEBUG_INT);
        } catch (Throwable th) {
            throw createErrorResponse(NotifyType.ERROR, th);
        }
    }

    /**
     * Выполняет поиск справочнка по его описанию
     *
     * @param dictionaryDefinition описание справочника
     * @return Возвращает найденный справочник
     */
    private Dictionary retrieveDictionary(WDictionaryDefinition dictionaryDefinition) {
        if (StringUtils.hasText(dictionaryDefinition.getId())) {
            return dictionaryService.findByID(dictionaryDefinition.getId(), true, false);
        } else if (dictionaryDefinition.getPath() != null) {
            return dictionaryService.findUniqueByNamedPath(
                    new DictionaryNamedPath(dictionaryDefinition.getPath().getGroupName(), dictionaryDefinition.getPath().getDictionaryName()),
                    false
            );
        } else {
            throw new ApplicationException("Dictionary ID or unique path must not be NULL");
        }
    }

    @POST
    @Path("/getRecords")
    @Override
    public SearchRecordsResponse getRecords(SearchRecordsRequest parameters) throws ErrorResponse {
        Assert.notNull(parameters, "Request parameters must not be NULL");
        WDictionaryDefinition dictionaryDefinition = parameters.getDictionary();
        Assert.notNull(dictionaryDefinition, "Dictionary definition must not be NULL");

        FilterCriteria filterCriteria = createFilterCriteria(parameters.getCriteria());
        try {
            Dictionary dictionary = retrieveDictionary(dictionaryDefinition);
            FilterResult<Record> filterResult = recordService.findRecordsByCriteria(dictionary.getId(), filterCriteria, false);

            SearchRecordsResponse response = createSearchResponse(filterResult, OBJECT_FACTORY.createSearchRecordsResponse());
            response.getRecord().addAll(Collections2.transform(filterResult.getResult(), toWRecordTransformer));
            return response;
        } catch (ApplicationException ex) {
            throw createErrorResponse(NotifyType.ERROR, ex, LocationAwareLogger.DEBUG_INT);
        } catch (Throwable th) {
            throw createErrorResponse(NotifyType.ERROR, th);
        }
    }

    /**
     * Формирует и возвращает коллекцию соответствий идентификатора поля перекодирования к записи справочника
     *
     * @param dictionaryId идентификатор справочника
     * @param metaFieldName название МЕТА-поля перекодирования
     * @param rules коллекция целевых правил
     * @param fieldFunction функция доступа к идентификатору поля перекодирования
     * @return Возвращает коллекцию составленных соответствий
     */
    private Map<String, Record> createRule2Records(String dictionaryId, final String metaFieldName, Collection<RecodeRule> rules, Function<Rule<?, ?, ?>, String> fieldFunction) {
        Collection<Record> records = recordService.findRecordsByCriteria(
                dictionaryId,
                createCriteriaByIDs(Field.PRIMARY_KEY, Collections2.transform(rules, fieldFunction)),
                false
        ).getResult();
        return Maps.uniqueIndex(records, new Function<Record, String>() {
            @Override
            public String apply(Record input) {
                Field field = input.retrieveFieldByName(metaFieldName);
                Assert.notNull(field, String.format("Field with name '%s' not exist in record with ID '%s'", metaFieldName, input.getId()));
                return field.getId();
            }
        });
    }

    /**
     * Выполняет поиск набора правил перекодирования по его описанию
     *
     * @param ruleDefinition описание набора
     * @return Возвращает найденный набор правил перекодирования
     */
    private RecodeRuleSet retrieveRecodeRuleSet(WRuleDefinition ruleDefinition) {
        if (StringUtils.hasText(ruleDefinition.getAlias())) {
            return recodeRuleSetService.findUniqueByNamedPath(ruleDefinition.getAlias(), false);
        } else if (ruleDefinition.getPath() != null) {
            WRulePath rulePath = ruleDefinition.getPath();

            final Dictionary fromDictionary = retrieveDictionary(rulePath.getFromDictionary());
            final Dictionary toDictionary = retrieveDictionary(rulePath.getToDictionary());

            return recodeRuleSetService.findRecodeRuleSetByNamedPath(
                    new MetaFieldNamedPath(createDictionaryNamedPath(fromDictionary), null),
                    new MetaFieldNamedPath(createDictionaryNamedPath(toDictionary), null),
                    true,
                    false
            );
        } else {
            throw new ApplicationException("Rule set alias or unique path must not be NULL");
        }
    }

    @POST
    @Path("/getRules")
    @Override
    public SearchRulesResponse getRules(SearchRulesRequest parameters) throws ErrorResponse {
        Assert.notNull(parameters, "Request parameters must not be NULL");
        WRuleDefinition ruleDefinition = parameters.getRuleSet();
        Assert.notNull(ruleDefinition, "Rule definition must not be NULL");

        FilterCriteria filterCriteria = createFilterCriteria(parameters.getCriteria());
        try {
            final RecodeRuleSet ruleSet = retrieveRecodeRuleSet(ruleDefinition);
            // Устанавливаем название полей перекодировки
            SearchRulesResponse response = OBJECT_FACTORY.createSearchRulesResponse();
            {
                response.setAlias(ruleSet.getName());
                response.setFromFieldName(ruleSet.getFromFieldName());
                response.setToFieldName(ruleSet.getToFieldName());
            }

            // Выполняем поиск записи по умолчанию
            if (StringUtils.hasText(ruleSet.getDefaultFieldId())) {
                Record defaultRecord = extractSingleDocument(
                        recordService.findRecordsByCriteria(
                                ruleSet.getToDictionaryId(),
                                createCriteriaByIDs(Field.PRIMARY_KEY, ruleSet.getDefaultFieldId()),
                                false
                        ).getResult()
                );
                response.setDefaultRecord(toWRecordTransformer.apply(defaultRecord));
            }

            // Выполняем поиск правил перекодирования по найденному набору
            FilterCriteria rulesFilterCriteria = createCriteriaByIDs(RecodeRule.RECODE_RULE_SET_ID, ruleSet.getId())
                    .injectOffset(filterCriteria.getOffset())
                    .injectCount(filterCriteria.getCount());

            FilterResult<RecodeRule> filterResult = recodeRuleService.findByCriteria(rulesFilterCriteria, false);
            response.setFilterCount(filterResult.getCountByFilter());
            response.setTotalCount(filterResult.getCountByFilter());

            // Получаем записи источника и назначения
            final Map<String, Record> fromRecords = createRule2Records(
                    ruleSet.getFromDictionaryId(), ruleSet.getFromFieldName(), filterResult.getResult(), FROM_RULE_FIELD_ID
            );
            final Map<String, Record> toRecords = createRule2Records(
                    ruleSet.getToDictionaryId(), ruleSet.getToFieldName(), filterResult.getResult(), TO_RULE_FIELD_ID
            );
            // Выполняем заполнение правил
            response.getRule().addAll(Collections2.transform(filterResult.getResult(), new Function<RecodeRule, WRule>() {
                @Override
                public WRule apply(RecodeRule input) {
                    WRule result = new WRule();
                    result.setId(input.getId());
                    result.setFromRecord(toWRecordTransformer.apply(fromRecords.get(input.getFromFieldId())));
                    result.setToRecord(toWRecordTransformer.apply(toRecords.get(input.getToFieldId())));
                    return result;
                }
            }));
            return response;
        } catch (ApplicationException ex) {
            throw createErrorResponse(NotifyType.ERROR, ex, LocationAwareLogger.DEBUG_INT);
        } catch (Throwable th) {
            throw createErrorResponse(NotifyType.ERROR, th);
        }
    }

    /**
     * Выполняет конвертацию результатов поиска
     *
     * @param filterResult найденная коллекцию объектов
     * @param response целевой класс ответа
     * @return Возвращает сформированный ответ
     */
    private static <R extends WSearchResponse, T> R createSearchResponse(FilterResult<T> filterResult, R response) {
        response.setFilterCount(filterResult.getCountByFilter());
        response.setTotalCount(filterResult.getTotalCount());
        return response;
    }

    /**
     * Выполняет конвертацию критерия поиска
     *
     * @param wSearchCriteria оригинальный критерий
     * @return Возвращает сформированный критерий поиска
     */
    private FilterCriteria createFilterCriteria(WSearchCriteria wSearchCriteria) {
        return (wSearchCriteria != null) ?
                mapper.map(wSearchCriteria, FilterCriteria.class) :
                new FilterCriteria().injectCount(FilterCriteria.COUNT_DEFAULT);
    }

    /**
     * Формирует ответное сообщение об ошибке выполнения запроса
     *
     * @param notifyType тип ошибки
     * @param cause исключительная ситуация
     * @return Возвращает сообщение об ошибке выполнения запроса
     */
    private static ErrorResponse createErrorResponse(NotifyType notifyType, Throwable cause) {
        switch (notifyType) {
            case ERROR: {
                return createErrorResponse(notifyType, cause, LocationAwareLogger.ERROR_INT);
            }
            default: {
                return createErrorResponse(notifyType, cause, LocationAwareLogger.DEBUG_INT);
            }
        }
    }

    /**
     * Формирует ответное сообщение об ошибке выполнения запроса
     *
     * @param notifyType тип ошибки
     * @param cause исключительная ситуация
     * @param logLevel уровень логирования ошибки
     * @return Возвращает сообщение об ошибке выполнения запроса
     */
    private static ErrorResponse createErrorResponse(NotifyType notifyType, Throwable cause, int logLevel) {
        String message = cause.getMessage();
        if (LocationAwareLogger.class.isAssignableFrom(LOG.getClass())) {
            ((LocationAwareLogger) LOG).log(null, LOG.getClass().getName(), logLevel, message, null, cause);
        } else {
            LOG.error(message, cause);
        }
        return new ErrorResponse(message, createError(notifyType, cause), cause);
    }
}
