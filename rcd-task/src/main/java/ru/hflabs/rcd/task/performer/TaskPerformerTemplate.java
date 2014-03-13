package ru.hflabs.rcd.task.performer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.util.Assert;
import ru.hflabs.rcd.model.task.TaskProgress;
import ru.hflabs.rcd.service.ITaskDefinitionRepository;
import ru.hflabs.rcd.service.task.ITaskPerformer;
import ru.hflabs.rcd.service.task.ITaskProgress;

import java.util.Map;

/**
 * Класс <class>TaskPerformerTemplate</class> реализует базовый класс исполнителя задачи
 *
 * @author Nazin Alexander
 */
public abstract class TaskPerformerTemplate<P extends ParametersHolder, R extends ParametersHolder> implements ITaskPerformer, BeanNameAware {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    /** Идентификатор исполнителя задачи */
    private String performerName;
    /** Сервис работы с репозиторием предопределенных задач */
    private ITaskDefinitionRepository taskDefinitionRepository;

    /** Идентификатор выполняемой задачи */
    private final ThreadLocal<ITaskProgress> taskProgress;

    protected TaskPerformerTemplate() {
        this.taskProgress = new InheritableThreadLocal<>();
    }

    public void setTaskDefinitionRepository(ITaskDefinitionRepository taskDefinitionRepository) {
        this.taskDefinitionRepository = taskDefinitionRepository;
    }

    @Override
    public void setBeanName(String name) {
        this.performerName = name;
    }

    @Override
    public String retrieveName() {
        return performerName;
    }

    /**
     * Возвращает класс параметра задачи
     *
     * @return Возвращает класс параметров задачи
     */
    public abstract Class<P> retrieveParameterClass();

    /**
     * Возвращает класс результата задачи
     *
     * @return Возвращает класс результата задачи
     */
    public abstract Class<R> retrieveResultClass();

    /**
     * Выполняет задачу
     *
     * @param parameters параметры задачи
     * @return Возвращает результат выполнения задачи
     */
    protected abstract R doPerformTask(P parameters) throws Throwable;

    /**
     * Выполняет задачу с подготовленными параметрами
     *
     * @param progress слушатель прогресса выполнения задачи
     * @param parameters параметры выполнения задачи
     * @return Возвращает результат выполнения
     */
    public R doPerformTask(ITaskProgress progress, P parameters) throws Throwable {
        Assert.notNull(progress, "Task progress callback not be NULL");
        Assert.notNull(parameters, "Task parameters must not be NULL");
        taskProgress.set(progress);
        try {
            return doPerformTask(parameters);
        } finally {
            taskProgress.remove();
        }
    }

    @Override
    public Map<String, Object> performTask(ITaskProgress progress, Map<String, Object> parameters) throws Throwable {
        // Формируем параметры задачи
        P taskParameters = taskDefinitionRepository.convertTaskParameters(retrieveParameterClass(), parameters);
        // Выполняем задачу
        R taskResults = doPerformTask(progress, taskParameters);
        // Формируем результат выполнения
        return taskDefinitionRepository.convertTaskResults(taskResults);
    }

    /**
     * Проверяет, что задача не была прервана
     *
     * @return Возвращает флаг проверки
     */
    protected boolean isCancelled() {
        return taskProgress.get().isTaskCanceled();
    }

    /**
     * Формирует и возвращает код шага выполнения задачи
     *
     * @param stepName код шага
     * @return Возвращает сформированный код шага выполнения задачи
     */
    protected String createMessageCode(String stepName) {
        return getClass().getSimpleName() + "." + stepName;
    }

    /**
     * Публикует событие изменения прогресса выполняемой задачи
     *
     * @param progress прогресс выполнения задачи
     */
    protected void changeProgress(TaskProgress progress) {
        taskProgress.get().changeTaskProgress(retrieveName(), progress);
    }

    /**
     * Формирует дескриптор прогресса выполнения и публикует событие
     *
     * @param percent процент выполнения
     * @param step текущий шаг выполнения задачи
     * @param code код шага
     * @param arguments аргументы локализации
     */
    protected void changeProgress(int percent, String step, String code, Object... arguments) {
        changeProgress(new TaskProgress(Math.min(percent, 100), step, createMessageCode(code), arguments));
    }

    /**
     * Формирует дескриптор прогресса выполнения и публикует событие
     *
     * @param progressHolder текущий прогресс выполнения
     * @param step текущий шаг выполнения задачи
     * @param code код шага
     * @param arguments аргументы локализации
     */
    protected void changeProgress(TaskProgressHolder progressHolder, String step, String code, Object... arguments) {
        changeProgress(
                Math.round(progressHolder.totalProgress() * 100.0f),
                step,
                code,
                arguments
        );
    }
}
