package ru.hflabs.rcd.task;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import ru.hflabs.rcd.event.task.TaskEvent;
import ru.hflabs.rcd.event.task.TaskExecutionEvent;
import ru.hflabs.rcd.event.task.TaskProgressEvent;
import ru.hflabs.rcd.model.task.*;
import ru.hflabs.rcd.service.task.ITaskPerformer;
import ru.hflabs.rcd.service.task.ITaskProgress;
import ru.hflabs.util.io.IOUtils;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static ru.hflabs.rcd.accessor.Accessors.injectId;

/**
 * Класс <class>TaskWorker</class> реализует обертку над выполняемой задачей с предоставлением доступа к результату выполнения
 *
 * @see ru.hflabs.rcd.model.task.TaskDescriptor
 * @see ru.hflabs.rcd.model.task.TaskResult
 * @see RunnableFuture
 */
public class TaskWorker implements RunnableFuture<TaskResult>, ITaskProgress {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    /** Дескриптор выполнения задачи */
    private final TaskDescriptor descriptor;
    /** Результат выполнения задачи */
    private final TaskResult result;

    /** Текущий статус выполнения задачи */
    private volatile TaskExecutionStatus status;
    /** Прогресс выполнения */
    private volatile TaskProgress progress;

    /** Исполнитель задачи */
    private final ITaskPerformer performer;
    /** Сервис публикации событий */
    private final ApplicationEventPublisher eventPublisher;

    /** Блокировка смены статуса */
    private final Lock writeLock;
    private final Lock readLock;
    /** Блокировка при выполнении задачи */
    private final CountDownLatch executionLock;

    public TaskWorker(String owner, String author, TaskDescriptor descriptor, ITaskPerformer performer, ApplicationEventPublisher eventPublisher) {
        // initialize variables
        this.descriptor = descriptor;
        this.performer = performer;
        this.eventPublisher = eventPublisher;
        // locks
        final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        this.writeLock = lock.writeLock();
        this.readLock = lock.readLock();
        this.executionLock = new CountDownLatch(1);
        // fill variables
        this.result = injectId(new TaskResult(), descriptor.getId());
        this.result.setDescriptorId(descriptor.getId());
        this.result.setOwner(owner);
        this.result.setAuthor(author);
        this.result.setParameters(descriptor.getParameters());
        this.result.setRegistrationDate(new Date());
        this.result.setStatus(TaskResultStatus.UNKNOWN);
        // change state
        changeState(new TaskProgress(TaskProgress.PENDING_STEP), TaskExecutionStatus.RUNNING);
    }

    /**
     * Выполняет логирование и публикацию события задачи
     *
     * @param event событие задачи
     * @param needLogging флаг необходимости логирования
     */
    private void doPublishEvent(TaskEvent event, boolean needLogging) {
        if (needLogging) {
            LOG.info("Task {}", event.identity());
        }
        eventPublisher.publishEvent(event);
    }

    /**
     * Публикует событие изменения статуса выполнения задачи
     *
     * @param targetState обновленный статус выполнения
     */
    private void changeState(TaskExecutionStatus targetState) {
        writeLock.lock();
        try {
            status = targetState;
            doPublishEvent(new TaskExecutionEvent(this, descriptor.getId(), performer.retrieveName(), targetState), false);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Публикует событие изменения прогресса выполнения задачи
     *
     * @param targetProgress обновленный прогресс выполнения
     */
    private void changeState(TaskProgress targetProgress) {
        writeLock.lock();
        try {
            progress = targetProgress;
            doPublishEvent(new TaskProgressEvent(this, descriptor.getId(), performer.retrieveName(), targetProgress), true);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Публикует событие изменения статуса и прогресса выполнения задачи
     *
     * @param targetState обновленный статус и прогресса выполнения
     */
    private void changeState(TaskProgress targetProgress, TaskExecutionStatus targetState) {
        writeLock.lock();
        try {
            changeState(targetProgress);
            changeState(targetState);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean isTaskCanceled() {
        return isCancelled();
    }

    @Override
    public void changeTaskProgress(String performerName, TaskProgress taskProgress) {
        if (performer.retrieveName().equals(performerName)) {
            changeState(taskProgress);
        }
    }

    /**
     * Устанавливает параметры запуска задачи
     *
     * @see #run()
     */
    private void startTask() {
        writeLock.lock();
        try {
            result.setStartDate(new Date());
            changeState(new TaskProgress(TaskProgress.EXECUTING_STEP));
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Устанавливает параметры завершения задачи
     *
     * @param results результат задачи
     * @param resultStatus статус завершения
     * @param exception исключения, которое произошло в процессе выполнения
     */
    private void endTask(Map<String, Object> results, TaskResultStatus resultStatus, Throwable exception) {
        writeLock.lock();
        try {
            result.setContent(results);
            result.setStatus(resultStatus);
            if (exception != null) {
                result.setErrorMessage(StringUtils.abbreviate(exception.getMessage(), TaskResult.ERROR_MESSAGE_MAX_SIZE - 3));
            }
            result.setEndDate(new Date());
            executionLock.countDown();
            changeState(null, TaskExecutionStatus.READY);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void run() {
        // Устанавливаем системные параметры запуска
        startTask();
        // Выполняем задачу
        Map<String, Object> results = null;
        TaskResultStatus resultStatus;
        Throwable cause = null;
        try {
            results = performer.performTask(this, result.getParameters());
            resultStatus = (isCancelled()) ? TaskResultStatus.CANCELED : TaskResultStatus.FINISHED;
        } catch (Throwable th) {
            resultStatus = TaskResultStatus.ERROR;
            cause = th;
            LOG.error(String.format("Task '%s[%s]' error: %s", descriptor.getId(), performer.retrieveName(), cause.getMessage()), cause);
        }
        // Устанавливаем системные параметры завершения
        endTask(results, resultStatus, cause);
    }

    /**
     * Возвращает декоратор выполняемой задачи
     *
     * @return Возвращает декоратор выполняемой задачи
     */
    public TaskExecution getTaskExecution() {
        readLock.lock();
        try {
            return new TaskExecution(descriptor, IOUtils.deepClone(result), IOUtils.deepClone(status), IOUtils.deepClone(progress));
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Возвращает текущий результат выполнения задачи
     *
     * @return Возвращает текущий результат выполнения задачи
     * @see #get()
     */
    public TaskResult getQuietly() {
        try {
            return get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new UndeclaredThrowableException(ex);
        }
    }

    @Override
    public TaskResult get() throws InterruptedException, ExecutionException {
        executionLock.await();
        return result;
    }

    @Override
    public TaskResult get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!executionLock.await(timeout, unit)) {
            throw new TimeoutException(String.format("Task '%s[%s]' hasn't completed", performer.retrieveName(), descriptor.getId()));
        }
        return result;
    }

    @Override
    public boolean isCancelled() {
        readLock.lock();
        try {
            return TaskExecutionStatus.INTERRUPTING.equals(status);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!isCancelled()) {
            changeState(TaskExecutionStatus.INTERRUPTING);
            return true;
        }
        return false;
    }

    @Override
    public boolean isDone() {
        readLock.lock();
        try {
            return TaskExecutionStatus.READY.equals(status);
        } finally {
            readLock.unlock();
        }
    }
}
