package ru.hflabs.rcd.task;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.event.ContextEvent;
import ru.hflabs.rcd.event.modify.ChangeEvent;
import ru.hflabs.rcd.event.task.TaskExecutionEvent;
import ru.hflabs.rcd.event.task.TaskScheduleEvent;
import ru.hflabs.rcd.exception.constraint.IllegalPrimaryKeyException;
import ru.hflabs.rcd.exception.constraint.task.IllegalTaskParametersException;
import ru.hflabs.rcd.model.ModelUtils;
import ru.hflabs.rcd.model.change.ChangeMode;
import ru.hflabs.rcd.model.change.ChangeSet;
import ru.hflabs.rcd.model.change.ChangeType;
import ru.hflabs.rcd.model.task.TaskDescriptor;
import ru.hflabs.rcd.model.task.TaskExecution;
import ru.hflabs.rcd.model.task.TaskExecutionStatus;
import ru.hflabs.rcd.model.task.TaskResult;
import ru.hflabs.rcd.service.IFindService;
import ru.hflabs.rcd.service.ISequenceService;
import ru.hflabs.rcd.service.ServiceUtils;
import ru.hflabs.rcd.service.task.ITaskLauncher;
import ru.hflabs.rcd.service.task.ITaskPerformer;
import ru.hflabs.util.security.SecurityUtil;
import ru.hflabs.util.security.SystemAuthenticationProvider;
import ru.hflabs.util.security.SystemAuthenticationProviderAware;
import ru.hflabs.util.spring.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * Класс <class>TaskLauncher</class> реализует сервис запуска задач
 *
 * @author Nazin Alexander
 */
public class TaskLauncher implements ITaskLauncher,
        BeanFactoryAware, ApplicationEventPublisherAware, SystemAuthenticationProviderAware,
        ApplicationListener<ApplicationEvent>, DisposableBean {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    /** Фабрика создания классов */
    private BeanFactory beanFactory;
    /** Сервис публикации событий */
    private ApplicationEventPublisher eventPublisher;
    /** Провайдер аутентификации системной роли */
    private SystemAuthenticationProvider systemAuthenticationProvider;
    /** Идентификатор сервиса */
    private String launcherId;

    /** Сервис создания уникальных идентификаторов */
    private ISequenceService sequenceService;
    /** Сервис работы с репозиторием задач */
    private IFindService<TaskDescriptor> taskRepository;

    /** Планировщик задач */
    private TaskScheduler schedulerService;
    /** Пул исполнения задач */
    private ExecutorService executorService;

    /** Коллекция запланированных задач, где ключ - идентификатор дескриптора задачи, значение - поток постановки задачи в очередь на выполнение */
    private Map<String, ScheduledFuture> scheduledTasks;
    /** Коллекция исполняемыз задач, где ключ - идентификатор исполняемой задачи, значение - результат выполнения */
    private Map<String, TaskWorker> executedTasks;

    public TaskLauncher() {
        this.executedTasks = new ConcurrentHashMap<>();
        this.scheduledTasks = new ConcurrentHashMap<>();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setSystemAuthenticationProvider(SystemAuthenticationProvider systemAuthenticationProvider) {
        this.systemAuthenticationProvider = systemAuthenticationProvider;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    public void setLauncherId(String launcherId) {
        this.launcherId = launcherId;
    }

    public void setSequenceService(ISequenceService sequenceService) {
        this.sequenceService = sequenceService;
    }

    public void setTaskRepository(IFindService<TaskDescriptor> taskRepository) {
        this.taskRepository = taskRepository;
    }

    public void setSchedulerService(TaskScheduler schedulerService) {
        this.schedulerService = schedulerService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public Class<TaskExecution> retrieveTargetClass() {
        return TaskExecution.class;
    }

    @Override
    public Collection<TaskExecution> findExecuted() {
        ImmutableList.Builder<TaskExecution> result = ImmutableList.builder();
        for (TaskWorker worker : executedTasks.values()) {
            TaskExecution execution = worker.getTaskExecution();
            if (!TaskExecutionStatus.READY.equals(execution.getStatus())) {
                result.add(execution);
            }
        }
        return result.build();
    }

    @Override
    public TaskExecution findByID(String id, boolean fillTransitive, boolean quietly) {
        Assert.isTrue(StringUtils.hasText(id), "ID must not be NULL or EMPTY");
        return ServiceUtils.extractSingleDocument(findByIDs(ImmutableSet.of(id), fillTransitive, quietly), null);
    }

    @Override
    public Collection<TaskExecution> findByIDs(Set<String> ids, boolean fillTransitive, boolean quietly) throws IllegalPrimaryKeyException {
        Assert.isTrue(!CollectionUtils.isEmpty(ids), "IDs must not be NULL or EMPTY");
        ImmutableList.Builder<TaskExecution> result = ImmutableList.builder();
        for (String id : ids) {
            TaskWorker worker = executedTasks.get(id);
            if (worker != null) {
                TaskExecution execution = worker.getTaskExecution();
                if (!TaskExecutionStatus.READY.equals(execution.getStatus())) {
                    result.add(worker.getTaskExecution());
                }
            }
        }
        return ServiceUtils.checkFoundDocuments(retrieveTargetClass(), ids, result.build(), quietly);
    }

    /**
     * Создает и возвращает экземпляр исполнителя задачи
     *
     * @param performerName идентификатор исполнителя задачи
     * @return Возвращает созданный экземпляр исполнителя задачи
     */
    private ITaskPerformer createTaskPerformer(String performerName) {
        return beanFactory.getBean(performerName, ITaskPerformer.class);
    }

    /**
     * Обновляет результаты выполнения задачи
     *
     * @param event событие изменения состояния задачи
     */
    private void refreshTaskResult(TaskExecutionEvent event) {
        if (TaskExecutionStatus.READY.equals(event.getStatus())) {
            TaskWorker worker = executedTasks.get(event.getId());
            Assert.notNull(worker, String.format("Task %s is not registered in launcher '%s'", event.identity(), launcherId));
            try {
                TaskResult taskResult = sequenceService.fillIdentifier(worker.getQuietly(), true);
                eventPublisher.publishEvent(
                        new ChangeEvent(this, new ChangeSet<>(TaskResult.class, ChangeType.CREATE, ChangeMode.DEFAULT, Arrays.asList(taskResult)))
                );
            } catch (Throwable ex) {
                LOG.error(String.format("Can't create task result %s. Cause by: %s", event.identity(), ex.getMessage()), ex);
            } finally {
                executedTasks.remove(event.getId());
            }
        }
    }

    /**
     * Возвращает поток исполнения задачи
     *
     * @param descriptor дескриптор задачи
     * @return Возвращает исполнителя задачи
     */
    private synchronized TaskWorker submitTask(TaskDescriptor descriptor) {
        Assert.notNull(descriptor, "Task descriptor must not be NULL");
        Assert.isTrue(StringUtils.hasText(descriptor.getId()), "ID must not be NULL");
        TaskWorker worker = executedTasks.get(descriptor.getId());
        // Проверяем, что задача еще не выполняется
        if (worker == null) {
            Assert.notNull(descriptor.getParameters(), "Task parameters not properly configured", IllegalTaskParametersException.class);
            worker = new TaskWorker(
                    launcherId,
                    SecurityUtil.getCurrentUserName(),
                    descriptor,
                    createTaskPerformer(descriptor.getName()),
                    eventPublisher
            );
            executedTasks.put(descriptor.getId(), worker);
            executorService.submit(SecurityUtil.wrapWithCurrentAuthentication(worker));
        }
        // Возвращаем дескриптор запущенной задачи
        return worker;
    }

    @Override
    public TaskExecution submitAsyncTask(String descriptorId) {
        return submitAsyncTask(taskRepository.findByID(descriptorId, false, false));
    }

    @Override
    public TaskExecution submitAsyncTask(TaskDescriptor descriptor) {
        return submitTask(descriptor).getTaskExecution();
    }

    @Override
    public TaskResult submitSyncTask(TaskDescriptor descriptor) {
        return submitTask(descriptor).getQuietly();
    }

    /**
     * Регистрирует задачу в планировщике
     *
     * @param event событие постановки задачи в очередь на выполнение
     */
    private void scheduleTask(TaskScheduleEvent event) {
        // Удаляем предыдущую задачу из списка запланированных
        ScheduledFuture existedFuture = scheduledTasks.remove(event.getId());
        if (existedFuture != null) {
            existedFuture.cancel(true);
            LOG.info(String.format("Previous task %s schedule canceled", event.identity()));
        }
        // Добавляем дескриптор выполнения в планировщик
        if (StringUtils.hasText(event.getCron())) {
            Runnable scheduledTask = systemAuthenticationProvider.wrapWithSystemAuthentication(new TaskSchedulerThread(event.getId()));
            try {
                scheduledTasks.put(event.getId(), schedulerService.schedule(scheduledTask, new CronTrigger(event.getCron())));
                LOG.info(String.format("Task %s scheduled", event.identity()));
            } catch (IllegalArgumentException ex) {
                LOG.info(String.format("Task %s schedule skipped. Cause by: %s", event.identity(), ex.getMessage()));
            }
        }
    }

    @Override
    public Collection<TaskExecution> cancelTask(Set<String> resultIDs) {
        Assert.isTrue(!CollectionUtils.isEmpty(resultIDs), "Task execution IDs must not be NULL");
        Collection<TaskExecution> result = new ArrayList<>(resultIDs.size());
        for (String resultId : resultIDs) {
            TaskWorker worker = executedTasks.get(resultId);
            if (worker != null) {
                worker.cancel(false);
                result.add(worker.getTaskExecution());
            }
        }
        return result;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextEvent && ((ContextEvent) event).registryListener(launcherId)) {
            // Событие изменения статуса задачи
            if (event instanceof TaskExecutionEvent) {
                refreshTaskResult((TaskExecutionEvent) event);
            }
            // Событие постановки задачи в планировщик
            if (event instanceof TaskScheduleEvent) {
                scheduleTask((TaskScheduleEvent) event);
            }
            // Событие модификации дескриптора задачи
            if (event instanceof ChangeEvent && TaskDescriptor.class.isAssignableFrom(((ChangeEvent) event).getChangedClass())) {
                ChangeEvent changeEvent = (ChangeEvent) event;
                // Событие закрытия дескриптора задачи
                if (ChangeType.CLOSE.equals(changeEvent.getChangeType())) {
                    cancelTask(Sets.newLinkedHashSet(Collections2.transform(changeEvent.getChanged(TaskDescriptor.class), ModelUtils.ID_FUNCTION)));
                }
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        // Отменяем все ранее запларированные задачи
        for (ScheduledFuture future : scheduledTasks.values()) {
            future.cancel(true);
        }
        scheduledTasks.clear();
        // Прерываем все выполняемые задачи
        executorService.shutdown();
        for (TaskWorker worker : executedTasks.values()) {
            worker.cancel(true);
        }
        executedTasks.clear();
    }

    /**
     * Класс <class>TaskSchedulerThread</class> реализует поток постановки запланированной задачи в очередь на исполнение
     *
     * @see org.springframework.scheduling.Trigger
     */
    private class TaskSchedulerThread implements Runnable {

        /** Идентификатор дескриптора выполнения задачи */
        private final String descriptorId;

        private TaskSchedulerThread(String descriptorId) {
            this.descriptorId = descriptorId;
        }

        @Override
        public void run() {
            try {
                submitAsyncTask(descriptorId);
            } catch (Exception ex) {
                LOG.error(String.format("Can't submit task ID '%s' to execution. Cause by: %s", descriptorId, ex.getMessage()), ex);
            }
        }
    }
}
