package ru.hflabs.rcd.task.performer.index;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.OrderComparator;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.event.index.IndexRebuildEvent;
import ru.hflabs.rcd.event.index.IndexRebuildedEvent;
import ru.hflabs.rcd.lucene.LuceneRebuildCallbackFactory;
import ru.hflabs.rcd.model.task.TaskProgress;
import ru.hflabs.rcd.model.task.TaskResultStatus;
import ru.hflabs.rcd.service.task.ITaskProgress;
import ru.hflabs.rcd.task.performer.TaskPerformerTemplate;
import ru.hflabs.rcd.task.performer.TaskProgressHolder;
import ru.hflabs.util.security.SecurityUtil;
import ru.hflabs.util.spring.util.ReflectionUtil;

import java.util.*;

/**
 * Класс <class>IndexRebuildTaskPerformer</class> реализует контроллер перестроения индексов через следующий алгоритм:<br/>
 * <ul>
 * <li>Сортирует и запоминает собранные фабрики на основании их {@link org.springframework.core.Ordered#getOrder() приоритета}</li>
 * <li>Выполняет поочередное перестроение отсортированной коллекции фабрик</li>
 * </ul>
 *
 * @author Nazin Alexander
 */
public class IndexRebuildTaskPerformer extends TaskPerformerTemplate<IndexRebuildParameters, IndexRebuildResult>
        implements BeanFactoryAware, ApplicationEventPublisherAware, ApplicationListener<ApplicationEvent> {

    /** Фабрика доступа к классам */
    private ListableBeanFactory beanFactory;
    /** Сервис публикации событий */
    private ApplicationEventPublisher eventPublisher;
    /** Коллекция фабрик перестроения индексов */
    private List<LuceneRebuildCallbackFactory> factories;

    public IndexRebuildTaskPerformer() {
        factories = Collections.synchronizedList(new ArrayList<LuceneRebuildCallbackFactory>());
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof ListableBeanFactory) {
            this.beanFactory = (ListableBeanFactory) beanFactory;
        } else {
            throw new FactoryBeanNotInitializedException(String.format("BeanFactory must be instance of '%s'", ListableBeanFactory.class.getSimpleName()));
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    @Override
    public Class<IndexRebuildParameters> retrieveParameterClass() {
        return IndexRebuildParameters.class;
    }

    @Override
    public Class<IndexRebuildResult> retrieveResultClass() {
        return IndexRebuildResult.class;
    }

    /**
     * Формирует коллекцию фабрик перестроения
     *
     * @param targetClasses коллекция целевых классов или <code>NULL</code>, если необходимо перестроить все индексы
     * @return Возвращает коллекцию фабрик
     */
    private List<LuceneRebuildCallbackFactory> createRebuildFactoriesByNames(final Collection<String> targetClasses) {
        // Собираем фабрики, индексы которых повреждены
        List<LuceneRebuildCallbackFactory> targetFactories = Lists.newArrayList(
                Collections2.filter(
                        factories,
                        new Predicate<LuceneRebuildCallbackFactory>() {
                            @Override
                            public boolean apply(LuceneRebuildCallbackFactory input) {
                                return targetClasses == null || targetClasses.contains(input.retrieveTargetClass().getName());
                            }
                        }
                )
        );
        // Сортируем фабрики согласно их порядку
        OrderComparator.sort(targetFactories);
        // Возвращаем коллекцию целевых фабрик
        return targetFactories;
    }

    private List<LuceneRebuildCallbackFactory> createRebuildFactoriesByClasses(final Collection<Class<?>> targetClasses) {
        return createRebuildFactoriesByNames(
                targetClasses != null ?
                        Collections2.transform(targetClasses, new Function<Class<?>, String>() {
                            @Override
                            public String apply(Class<?> input) {
                                return input.getName();
                            }
                        }) :
                        null
        );
    }

    /**
     * Выполняет перестроение индекса
     *
     * @param factory фабрика перестроения
     * @param force флаг принудительного перестроения
     * @return Возвращает результат перестроения
     */
    protected IndexRebuildResult.Index doIndexRebuild(LuceneRebuildCallbackFactory factory, boolean force) throws Exception {
        final Class<?> targetClass = factory.retrieveTargetClass();
        final int documentsCount;
        final TaskResultStatus status;
        // Проверяем, что указан флаг принудительного перестроения
        if (force) {
            documentsCount = factory.executeRebuild();
            status = TaskResultStatus.FINISHED;
        } else {
            // Проверяем, что индекс не поврежден
            if (factory.isCorrupted()) {
                documentsCount = factory.executeRebuild();
                status = TaskResultStatus.FINISHED;
            } else {
                documentsCount = factory.totalDocumentCount();
                status = TaskResultStatus.SKIPPED;
            }
        }
        // Публикуем событие о перестроении индекса
        eventPublisher.publishEvent(new IndexRebuildedEvent(this, targetClass, documentsCount));
        // Возвращаем результат перестроения
        return new IndexRebuildResult.Index(status, targetClass.getName(), documentsCount);
    }

    @Override
    protected synchronized IndexRebuildResult doPerformTask(IndexRebuildParameters parameters) throws Exception {
        // Формируем целевые фабрики
        List<LuceneRebuildCallbackFactory> targetFactories = createRebuildFactoriesByNames(parameters.getTarget());
        // Определяем порядок перестроения
        String rebuildOrder = StringUtils.collectionToCommaDelimitedString(Collections2.transform(targetFactories, new Function<LuceneRebuildCallbackFactory, String>() {
            @Override
            public String apply(LuceneRebuildCallbackFactory input) {
                return input.retrieveTargetClass().getSimpleName();
            }
        }));
        changeProgress(
                TaskProgress.INFINITE_PROGRESS,
                "Executing {0} result rebuild in the following order: {1}",
                "order",
                parameters.isForce() ? "force" : "safe",
                rebuildOrder
        );

        // Выполняем перестроение для каждой фабрики
        List<IndexRebuildResult.Index> result = new ArrayList<>(targetFactories.size());
        TaskProgressHolder context = new TaskProgressHolder(new Date(), SecurityUtil.getCurrentUserName(), targetFactories.size());
        for (Iterator<LuceneRebuildCallbackFactory> iterator = targetFactories.iterator(); !isCancelled() && iterator.hasNext(); ) {
            LuceneRebuildCallbackFactory factory = iterator.next();
            changeProgress(context.nextStep(), "Rebuilding {0}", "rebuilding", factory.retrieveTargetClass().getSimpleName());
            result.add(doIndexRebuild(factory, parameters.isForce()));
        }

        changeProgress(TaskProgress.MAX_PROGRESS, "Full result rebuild finished", "finished");
        // Возвращаем результат перестроения
        return new IndexRebuildResult(result);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        // Событие инициализации корневого контекста приложения
        if (event instanceof ContextRefreshedEvent && ((ContextRefreshedEvent) event).getApplicationContext().getParent() == null) {
            // Собираем все фабрики из контекста
            factories.addAll(beanFactory.getBeansOfType(LuceneRebuildCallbackFactory.class, true, true).values());
            // Публикуем событие полного перестроения индексов
            eventPublisher.publishEvent(new IndexRebuildEvent(this));
        }
        // Событие закрытия конревого контекста приложения
        if (event instanceof ContextClosedEvent && ((ContextClosedEvent) event).getApplicationContext().getParent() == null) {
            factories.clear();
        }
        // Событие перестроения индексов
        if (event instanceof IndexRebuildEvent && ((IndexRebuildEvent) event).registryListener(retrieveName())) {
            IndexRebuildEvent indexRebuildEvent = (IndexRebuildEvent) event;
            // Собираем все фабрики
            Collection<LuceneRebuildCallbackFactory> targetFactories = createRebuildFactoriesByClasses(indexRebuildEvent.getTargetClasses());
            // Формируем дескриптор перестроения индексов для поврежденных фабрик
            if (!targetFactories.isEmpty()) {
                IndexRebuildParameters parameters = new IndexRebuildParameters();
                {
                    parameters.setForce(indexRebuildEvent.isForce());
                    parameters.setTarget(
                            Sets.newHashSet(Collections2.transform(targetFactories, new Function<LuceneRebuildCallbackFactory, String>() {
                                @Override
                                public String apply(LuceneRebuildCallbackFactory input) {
                                    return input.retrieveTargetClass().getName();
                                }
                            }))
                    );
                }
                try {
                    doPerformTask(ITaskProgress.LOGGER_PROGRESS, parameters);
                } catch (Throwable ex) {
                    ReflectionUtil.rethrowRuntimeException(ex);
                }
            }
        }
    }
}
