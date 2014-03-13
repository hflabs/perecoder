package ru.hflabs.rcd.service;

import com.google.common.collect.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.accessor.FieldAccessor;
import ru.hflabs.rcd.event.modify.ChangeEvent;
import ru.hflabs.rcd.exception.constraint.IllegalPrimaryKeyException;
import ru.hflabs.rcd.model.*;
import ru.hflabs.rcd.model.change.ChangeSet;
import ru.hflabs.rcd.model.change.ChangeType;
import ru.hflabs.rcd.model.change.HistoryBuilder;
import ru.hflabs.rcd.model.change.Predicates;
import ru.hflabs.rcd.model.criteria.FilterActivity;
import ru.hflabs.rcd.model.criteria.FilterCriteria;
import ru.hflabs.rcd.model.criteria.FilterCriteriaValue;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;
import ru.hflabs.rcd.model.rule.Rule;
import ru.hflabs.util.core.ExceptionUtil;
import ru.hflabs.util.core.Pair;

import java.util.*;

import static ru.hflabs.rcd.accessor.Accessors.linkDescendants;
import static ru.hflabs.rcd.model.CriteriaUtils.createCriteriaByIDs;
import static ru.hflabs.rcd.model.ModelUtils.*;

/**
 * Класс <class>ServiceUtils</class> реализует всмомогательные методы для работы с сервисами
 *
 * @see ModelUtils
 */
public abstract class ServiceUtils {

    protected ServiceUtils() {
        // embedded constructor
    }

    /**
     * Выполняет проверку коллекции найденных документов
     *
     * @param checkedCollectionClass класс проверяемых документов
     * @param expectedIDs коллекция ожидаемых идентификаторов
     * @param foundDocuments коллекция найденных документов
     * @param quietly флаг безопасной проверки
     * @return Возвращает коллецию найденных документов
     * @throws IllegalPrimaryKeyException Исключение, возникающее, если коллекция найденных документов не соответствует коллекции ожидаемых идентификаторов
     */
    public static <T extends Identifying> Collection<T> checkFoundDocuments(Class<T> checkedCollectionClass, Set<String> expectedIDs, Collection<T> foundDocuments, boolean quietly) {
        if (!quietly && expectedIDs.size() != foundDocuments.size()) {
            Set<String> notFoundIDs = Sets.difference(expectedIDs, Sets.newHashSet(Collections2.transform(foundDocuments, ID_FUNCTION)));
            throw new IllegalPrimaryKeyException(
                    String.format("Can't find '%s' with IDs '%s'", checkedCollectionClass.getSimpleName(), StringUtils.collectionToCommaDelimitedString(notFoundIDs))
            );
        }
        return foundDocuments;
    }

    /**
     * Возвращает документ из коллекции
     *
     * @param documents коллекция документов
     * @param exception исключительная ситуация или <code>NULL</code>, если необходимо выполнить безопасную проверку
     * @return Возвращает документ
     * @throws E Исключительная ситуация, если размер коллекции не равен <code>1</code>
     */
    public static <E extends RuntimeException, T> T extractSingleDocument(Collection<T> documents, Pair<String, Class<E>> exception) throws E {
        if (documents != null && documents.size() == 1) {
            return documents.iterator().next();
        } else if (exception != null) {
            ExceptionUtil.throwException(exception.first, exception.second);
        }
        return null;
    }

    /**
     * Возвращает документ из коллекции
     *
     * @param documents коллекция документов
     * @return Возвращает документ
     * @throws IllegalArgumentException Исключительная ситуация, если размер коллекции не равен <code>1</code>
     */
    public static <T> T extractSingleDocument(Collection<T> documents) throws IllegalArgumentException {
        return extractSingleDocument(
                documents,
                Pair.valueOf(String.format("Unexpected documents count (expected %d, got %d)", 1, documents.size()), IllegalArgumentException.class)
        );
    }

    /**
     * Позвращает первый документ по сформированному критерию
     *
     * @param service сервис поиска документов
     * @param criteria критерий поиска
     * @param fillTransitive флаг необходимости заполнения транзитивных зависимостей
     * @return Возвращает найденный документ или <code>NULL</code>, если поиск вернул пустой результат
     */
    public static <T extends Identifying> T findOneDocumentBy(IFilterService<T> service, FilterCriteria criteria, boolean fillTransitive) {
        return FluentIterable.<T>from(
                service.findByCriteria(criteria.injectCount(1), fillTransitive).getResult()
        ).first().orNull();
    }

    /**
     * Возвращает документ, который однозначно можно определить по заданному критерию
     *
     * @param service сервис работы с документом
     * @param criteria критерий поиска
     * @param fillTransitive флаг необходимости заполнения транзитивных зависимостей
     * @return Возвращает найденный документ или <code>NULL</code>, если документ однозначно определить не удалось
     */
    public static <T extends Identifying> T findUniqueDocumentBy(IFilterService<T> service, FilterCriteria criteria, boolean fillTransitive) {
        criteria = criteria.injectOffset(0).injectCount(2);
        Collection<T> result = service.findByCriteria(criteria, fillTransitive).getResult();
        return (result.size() == 1) ?
                result.iterator().next() :
                null;
    }

    /**
     * Возвращает документ, который однозначно можно определить по коллекции фильтров
     *
     * @param service сервис работы с документом
     * @param filters коллекция фильтров
     * @param fillTransitive флаг необходимости заполнения транзитивных зависимостей
     * @return Возвращает найденный документ или <code>NULL</code>, если документ однозначно определить не удалось
     */
    public static <T extends Identifying> T findUniqueDocumentBy(IFilterService<T> service, Map<String, FilterCriteriaValue<?>> filters, boolean fillTransitive) {
        return findUniqueDocumentBy(service, new FilterCriteria().injectFilters(filters), fillTransitive);
    }

    /**
     * Выполняет оповещение слушателей об изменениях
     *
     * @param eventPublisher сервис публикации событий
     * @param source источник события
     * @param changeSet набор изменений
     */
    public static <T> void publishChangeEvent(ApplicationEventPublisher eventPublisher, Object source, ChangeSet<T> changeSet) {
        eventPublisher.publishEvent(new ChangeEvent(source, changeSet));
    }

    /**
     * Выполняет оповещение слушателей об изменениях
     *
     * @param eventPublisher сервис публикации событий
     * @param source источник события
     * @param descriptor дескриптор изменений
     * @param types целевые типы событий или <code>NULL</code>, если необходимо опубликовать все типы событий
     */
    public static <T extends Historical> void publishChangeEvent(ApplicationEventPublisher eventPublisher, Object source, HistoryBuilder<T> descriptor, ChangeType... types) {
        Collection<ChangeType> targetTypes = (types == null || types.length == 0) ?
                EnumSet.allOf(ChangeType.class) :
                Sets.newHashSet(types);
        for (ChangeType changeType : targetTypes) {
            if (!descriptor.getChangeSet(changeType).isEmpty()) {
                publishChangeEvent(eventPublisher, source, descriptor.getChangeSet(changeType));
            }
        }
    }

    /**
     * Выполняет заполнение родителей для переданных потомков
     *
     * @param objects коллекция потомков
     * @param service сервис получения связанных родителей
     * @return Возвращает модифицированную коллекцию объектов
     */
    public static <R extends ManyToOne<T>, T extends Identifying> Collection<R> injectRelations(Collection<R> objects, IFilterService<T> service) {
        Collection<R> notNullRelations = Collections2.filter(objects, Predicates.notNull(RELATIVE_ID_FUNCTION));
        if (!CollectionUtils.isEmpty(notNullRelations)) {
            Map<String, Collection<R>> relativeId2objects = Multimaps.index(notNullRelations, RELATIVE_ID_FUNCTION).asMap();
            Collection<T> relations = service.findAllByCriteria(
                    createCriteriaByIDs(T.PRIMARY_KEY, relativeId2objects.keySet()).injectActivity(FilterActivity.ALL),
                    true
            );
            for (T relation : relations) {
                for (R object : relativeId2objects.get(relation.getId())) {
                    object.setRelative(relation);
                }
            }
        }
        return objects;
    }

    /**
     * Выполняет наполнение потомков для переданных родителей
     *
     * @param objects коллекция родительских объектов
     * @param service сервис получения связанных потомков
     * @return Возращает заполненные связанные сущности
     */
    public static <R extends Identifying & OneToMany<T>, T extends Identifying & ManyToOne<R>> Collection<R> injectRelative(Collection<R> objects, IManyToOneService<T> service) {
        List<R> result = Lists.newArrayListWithExpectedSize(objects.size());
        for (R object : objects) {
            result.add(linkDescendants(object, service.findAllByRelativeId(object.getId(), null, false)));
        }
        return result;
    }

    public static <NP extends MetaFieldNamedPath, T extends Essence, R extends Rule<NP, T, R>> Collection<R> injectRuleRelations(Collection<R> objects, IFilterService<T> service, FieldAccessor<T, R> fromAccessor, FieldAccessor<T, R> toAccessor) {
        if (!CollectionUtils.isEmpty(objects)) {
            Map<String, Collection<R>> relativeId2FromObjects = Multimaps.index(objects, FROM_RULE_FIELD_ID).asMap();
            Map<String, Collection<R>> relativeId2ToObjects = Multimaps.index(objects, TO_RULE_FIELD_ID).asMap();

            Set<String> relativeIDs = ImmutableSet.<String>builder()
                    .addAll(relativeId2FromObjects.keySet())
                    .addAll(relativeId2ToObjects.keySet())
                    .build();

            Collection<T> relations = service.findAllByCriteria(
                    createCriteriaByIDs(Identifying.PRIMARY_KEY, relativeIDs).injectActivity(FilterActivity.ALL),
                    true
            );

            for (T relation : relations) {
                Collection<R> fromRules = relativeId2FromObjects.get(relation.getId());
                if (fromRules != null) {
                    for (R rule : fromRules) {
                        fromAccessor.inject(rule, relation);
                    }
                }
                Collection<R> toRules = relativeId2ToObjects.get(relation.getId());
                if (toRules != null) {
                    for (R rule : toRules) {
                        toAccessor.inject(rule, relation);
                    }
                }
            }
        }
        return objects;
    }

    /**
     * Выполняет актуализацию правил перекодирования
     *
     * @param mergeService сервис актуализации
     * @param dependencies коллекцию обновившихся зависимостей
     * @param existed коллекция правил для обновления
     * @return Возвращает коллекцию обновленных правил
     */
    public static <D extends Essence, NP extends MetaFieldNamedPath, T extends Essence, R extends Rule<NP, T, R>> Collection<R> updateRulesByDependencies(
            IMergeService<Collection<D>, Collection<R>, Collection<R>> mergeService, Collection<D> dependencies, Collection<R> existed) {
        return !CollectionUtils.isEmpty(existed) ?
                mergeService.merge(dependencies, existed) :
                existed;
    }
}
