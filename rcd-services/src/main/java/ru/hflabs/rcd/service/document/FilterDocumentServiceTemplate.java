package ru.hflabs.rcd.service.document;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DocumentStoredFieldVisitor;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.*;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.event.ContextEvent;
import ru.hflabs.rcd.event.index.IndexRebuildedEvent;
import ru.hflabs.rcd.lucene.LuceneRebuildCallbackFactory;
import ru.hflabs.rcd.lucene.NamedIndexManager;
import ru.hflabs.rcd.lucene.binder.LuceneBinderTransformerFactory;
import ru.hflabs.rcd.lucene.criteria.LuceneCriteriaHolder;
import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.change.ChangeType;
import ru.hflabs.rcd.model.criteria.FilterCriteria;
import ru.hflabs.rcd.model.criteria.FilterResult;
import ru.hflabs.rcd.service.ICriteriaBuilder;
import ru.hflabs.rcd.service.IFilterService;
import ru.hflabs.rcd.service.IQueryProvider;
import ru.hflabs.rcd.service.IStorageService;
import ru.hflabs.rcd.storage.ChangeServiceTemplate;
import ru.hflabs.util.core.Three;
import ru.hflabs.util.io.IOUtils;
import ru.hflabs.util.lucene.*;
import ru.hflabs.util.spring.Assert;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.hflabs.rcd.model.CriteriaUtils.createCriteriaByIDs;
import static ru.hflabs.rcd.service.ServiceUtils.checkFoundDocuments;
import static ru.hflabs.rcd.service.ServiceUtils.extractSingleDocument;

/**
 * Класс <class>FilterDocumentServiceTemplate</class> реализует базовый сервис изменения документов в поисковом индексе
 *
 * @author Nazin Alexander
 */
public class FilterDocumentServiceTemplate<E extends Identifying> extends ChangeServiceTemplate<E> implements IFilterService<E>, IQueryProvider<E, LuceneCriteriaHolder>, LuceneRebuildCallbackFactory<E>, ApplicationEventPublisherAware, InitializingBean, DisposableBean {

    public static final int REBUILD_FETCH_SIZE = 10000;

    /** Сервис публикации событий */
    protected ApplicationEventPublisher eventPublisher;

    /** Сервис преобразования критерии поиска в нативный запрос */
    protected ICriteriaBuilder<E, LuceneCriteriaHolder, ?> criteriaBuilder;
    /** Сервис выполняет нативных запросов */
    protected IQueryProvider<E, LuceneCriteriaHolder> queryProvider;

    /** Сервис доступа к сущностям */
    private IStorageService<E> storageService;

    /** Сервис преобразования сущностей */
    protected LuceneBinderTransformer<E, String> binderTransformer;
    /** Менеджер индекса */
    protected NamedIndexManager indexManager;
    /** Флаг, указывающий, что индекс поврежден */
    private final AtomicBoolean indexCorrupted;
    /** Менеджер поиска */
    private SearcherManager searcherManager;
    /** Общее количество документов в индексе */
    private final AtomicInteger totalDocumentCount;

    public FilterDocumentServiceTemplate(Class<E> documentClass) {
        super(documentClass);
        this.indexCorrupted = new AtomicBoolean(true);
        this.totalDocumentCount = new AtomicInteger(0);
        setQueryProvider(this);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    public void setCriteriaBuilder(ICriteriaBuilder<E, LuceneCriteriaHolder, ?> criteriaBuilder) {
        this.criteriaBuilder = criteriaBuilder;
    }

    public void setQueryProvider(IQueryProvider<E, LuceneCriteriaHolder> queryProvider) {
        this.queryProvider = queryProvider;
    }

    public void setStorageService(IStorageService<E> storageService) {
        this.storageService = storageService;
    }

    public void setBinderTransformerFactory(LuceneBinderTransformerFactory<E, String> binderTransformerFactory) {
        this.binderTransformer = binderTransformerFactory.retrieveService(retrieveTargetClass());
    }

    public void setIndexManager(NamedIndexManager indexManager) {
        this.indexManager = indexManager;
    }

    /**
     * Выполняет заполнение транзитивных зависимостей
     *
     * @param objects коллекцию документов
     * @return Возвращает модифицированную коллекцию
     */
    protected Collection<E> injectTransitiveDependencies(Collection<E> objects) {
        return objects;
    }

    @Override
    public E findByID(String id, boolean fillTransitive, boolean quietly) {
        Assert.isTrue(StringUtils.hasText(id), "ID must not be NULL or EMPTY");
        return extractSingleDocument(findByIDs(ImmutableSet.of(id), fillTransitive, quietly), null);
    }

    @Override
    public Collection<E> findByIDs(Set<String> ids, boolean fillTransitive, boolean quietly) {
        Assert.isTrue(!CollectionUtils.isEmpty(ids), "IDs must not be NULL or EMPTY");
        Collection<E> result = findAllByCriteria(createCriteriaByIDs(E.PRIMARY_KEY, ids), fillTransitive);
        return checkFoundDocuments(retrieveTargetClass(), ids, result, quietly);
    }

    @Override
    public FilterResult<E> findByCriteria(FilterCriteria criteria, boolean fillTransitive) {
        Assert.notNull(criteria, "Filter criteria must not be NULL");
        if (criteria.getCount() == FilterCriteria.COUNT_ALL) {
            Collection<E> result = findAllByCriteria(criteria, fillTransitive);
            return new FilterResult<E>(result, result.size(), result.size());
        } else {
            Three<Collection<E>, Integer, Integer> result = queryProvider.executeByCriteria(
                    criteriaBuilder.createCriteria(retrieveTargetClass(), criteria),
                    criteria.getOffset(),
                    criteria.getCount()
            );
            return new FilterResult<E>(
                    fillTransitive ? Lists.newArrayList(injectTransitiveDependencies(result.first)) : result.first,
                    result.second,
                    result.third
            );
        }
    }

    @Override
    public Collection<E> findAllByCriteria(FilterCriteria criteria, boolean fillTransitive) {
        int count = countByCriteria(criteria);
        return (count > 0) ?
                findByCriteria(criteria.injectOffset(0).injectCount(count), fillTransitive).getResult() :
                Collections.<E>emptyList();
    }

    @Override
    public int countByCriteria(FilterCriteria criteria) {
        return queryProvider.executeCountByCriteria(criteriaBuilder.createCriteria(retrieveTargetClass(), criteria));
    }

    /**
     * Проверяет и выполняет обновление менеджера поиска
     *
     * @param force флаг принудительно обновления
     * @return Возвращает актуальный менеджера поиска
     */
    protected SearcherManager refreshSearcherManager(boolean force) {
        try {
            if (force || !searcherManager.isSearcherCurrent()) {
                if (searcherManager.maybeRefresh()) {
                    totalDocumentCount.set(LuceneQueryUtil.totalCount(searcherManager));
                } else {
                    searcherManager.maybeRefreshBlocking();
                }
            }
            return searcherManager;
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    /**
     * Выполняет поиск коллекции сущностей по подготовленному запросу
     *
     * @param referenceManager сервис поиска
     * @param query запрос поиска
     * @param sort сортировка
     * @param filter фильтр сущностей
     * @param offset смещение относительно начала объектов
     * @param count количество запрашиваемых объектов
     * @return Возвращает коллекцию сущностей, удовлетворящих критериям поиска
     */
    private Collection<E> doQuery(
            final ReferenceManager<IndexSearcher> referenceManager,
            final Query query,
            final Sort sort,
            final Filter filter,
            final Integer offset,
            final Integer count) {
        try {
            return LuceneQueryUtil.query(binderTransformer, new LuceneQueryCallback() {
                @Override
                public ReferenceManager<IndexSearcher> getSearcherManager() {
                    return referenceManager;
                }

                @Override
                public LuceneQueryDescriptor getQueryDescriptor() {
                    return new LuceneQueryDescriptor(query, sort, filter, offset, count);
                }

                @Override
                public DocumentStoredFieldVisitor createStoredFieldVisitor() {
                    return new DocumentStoredFieldVisitor();
                }
            });
        } catch (Throwable ex) {
            throw new RuntimeException(String.format("Can't execute query '%s' on '%s' index. Cause by: %s", query, retrieveTargetClassName(), ex.getMessage()), ex);
        }
    }

    /**
     * Выполняет поиск количества сущностей по подготовленному запросу
     *
     * @param referenceManager сервис поиска
     * @param query запрос поиска
     * @param filter фильтр найденных сущностей
     * @return Возвращает количество найденных сущностей
     */
    private int doCountQuery(ReferenceManager<IndexSearcher> referenceManager, Query query, Filter filter) {
        try {
            return LuceneQueryUtil.count(referenceManager, query, filter);
        } catch (Throwable ex) {
            throw new RuntimeException(String.format("Can't execute count query '%s' on '%s' index. Cause by: %s", query, retrieveTargetClassName(), ex.getMessage()), ex);
        }
    }

    /**
     * Выполняет модификацию индекса
     *
     * @param operationName название операции модификации
     * @return Возвращает количество измененных сущностей
     */
    protected int doModify(String operationName, LuceneModifierCallback callback) {
        try {
            return LuceneModifierUtil.doWithCallback(operationName, indexManager, callback);
        } catch (IOException ex) {
            throw new RuntimeException(String.format("Can't execute '%s' on '%s' index. Cause by: %s", operationName, retrieveTargetClassName(), ex.getMessage()), ex);
        }
    }

    @Override
    public Three<Collection<E>, Integer, Integer> executeByCriteria(LuceneCriteriaHolder criteria, int offset, int count) {
        ReferenceManager<IndexSearcher> queryManager = refreshSearcherManager(false);

        // Получаем запрос и фильтр
        Query targetQuery = criteria.buildQuery();
        Filter targetFilter = criteria.buildFilter();

        Collection<E> result = doQuery(queryManager, targetQuery, criteria.buildSort(), targetFilter, offset, count);

        // Если размер коллекции меньше, чем запрошенное количество, и нет смещения, то НЕ производим вычисление количества документов по фильтру
        Integer countByFilter = (LuceneQueryUtil.createTargetOffset(offset) == 0 && result.size() < LuceneQueryUtil.createTargetCount(count)) ?
                result.size() :
                doCountQuery(queryManager, targetQuery, targetFilter);
        Integer totalCount = (targetFilter != null) ?
                doCountQuery(queryManager, new MatchAllDocsQuery(), targetFilter) :
                totalDocumentCount.get();

        return Three.valueOf(result, countByFilter, totalCount);
    }

    @Override
    public Integer executeCountByCriteria(LuceneCriteriaHolder criteria) {
        return doCountQuery(refreshSearcherManager(false), criteria.buildQuery(), criteria.buildFilter());
    }

    @Override
    protected Collection<E> handleSelfCreateEvent(Collection<E> changed) {
        doModify("insert", LuceneModifierUtil.createInsertCallback(binderTransformer, changed));
        return changed;
    }

    @Override
    protected Collection<E> handleSelfUpdateEvent(Collection<E> changed) {
        doModify("update", LuceneModifierUtil.createUpdateCallback(indexManager, binderTransformer, changed));
        return changed;
    }

    @Override
    protected Collection<E> handleSelfRestoreEvent(Collection<E> changed) {
        throw new UnsupportedOperationException(String.format("%s change type not supported by %s", ChangeType.RESTORE.name(), getClass().getName()));
    }

    @Override
    protected Collection<E> handleSelfCloseEvent(Collection<E> changed) {
        doModify("close", LuceneModifierUtil.createDeleteCallback(binderTransformer, changed));
        return changed;
    }

    /**
     * Выполняет обработку события перестроения индекса
     *
     * @param event событие
     */
    protected void handleSelfIndexRebuildEvent(IndexRebuildedEvent event) {
        // do nothing
    }

    @Override
    protected void handleContextEvent(ContextEvent event) {
        super.handleContextEvent(event);
        // Событие перестроения индекса
        if (event instanceof IndexRebuildedEvent && retrieveTargetClass().isAssignableFrom(((IndexRebuildedEvent) event).getTargetClass())) {
            handleSelfIndexRebuildEvent((IndexRebuildedEvent) event);
        }
    }

    @Override
    public int totalDocumentCount() {
        return totalDocumentCount.get();
    }

    @Override
    public boolean isCorrupted() {
        return indexCorrupted.get();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public int executeRebuild() {
        try {
            final int count = LuceneModifierUtil.doWithCallback("full rebuild", indexManager, new RebuildModifierCallback());

            totalDocumentCount.set(count);
            indexCorrupted.set(false);

            return count;
        } catch (Throwable ex) {
            throw new RuntimeException(String.format("Can't execute full rebuild '%s' index. Cause by: %s", retrieveTargetClassName(), ex.getMessage()), ex);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public void afterPropertiesSet() throws Exception {
        indexManager.open(retrieveTargetClassName());

        searcherManager = indexManager.createSearcherManager();
        searcherManager = refreshSearcherManager(true);

        indexCorrupted.set(totalDocumentCount.get() != storageService.totalCount());
    }

    @Override
    public void destroy() throws Exception {
        IOUtils.closeQuietly(searcherManager);
        totalDocumentCount.set(0);
        indexManager.close();
    }

    /**
     * Класс <class>RebuildModifierCallback</class> реализует процессор перестроения индекса
     *
     * @author Nazin Alexander
     */
    protected class RebuildModifierCallback implements LuceneModifierCallback {

        /** Интервал логирования */
        private static final int LOG_INTERVAL_COUNT = 100000;

        @Override
        public int process(IndexWriter writer) throws Exception {
            // Удаляем все документы из индекса
            writer.deleteAll();
            final AtomicInteger processedCount = new AtomicInteger(0);

            // Итерируем документы, находящиеся в хранилище
            Iterator<List<E>> iterator = storageService.iterateAll(REBUILD_FETCH_SIZE, 1);
            try {
                while (iterator.hasNext()) {
                    Collection<E> page = injectTransitiveDependencies(iterator.next());
                    writer.addDocuments(Collections2.transform(page, new Function<E, Document>() {
                        @Override
                        public Document apply(E input) {
                            return binderTransformer.reverseConvert(input);
                        }
                    }));

                    int count = processedCount.addAndGet(page.size());
                    if (LOG.isInfoEnabled() && count / LOG_INTERVAL_COUNT != (count - page.size()) / LOG_INTERVAL_COUNT) {
                        LOG.info("Processed {} {}", count, retrieveTargetClassName());
                    }
                }
                return processedCount.get();
            } finally {
                IOUtils.closeQuietly(iterator);
            }
        }
    }
}
