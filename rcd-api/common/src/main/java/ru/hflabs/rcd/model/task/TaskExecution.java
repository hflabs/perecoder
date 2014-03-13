package ru.hflabs.rcd.model.task;

import lombok.Getter;
import lombok.Setter;
import ru.hflabs.rcd.model.Identifying;

import java.io.Serializable;

/**
 * Класс <class>TaskExecutionBean</class> описывает декоратор задачи
 *
 * @see TaskDescriptor
 * @see TaskResult
 */
@Getter
@Setter
public class TaskExecution implements Identifying, Serializable {

    private static final long serialVersionUID = -2392581061766228108L;

    /** Параметры задачи */
    private final TaskDescriptor descriptor;
    /** Результат задачи */
    private final TaskResult result;
    /** Текущий статус выполнения задачи */
    private final TaskExecutionStatus status;
    /** Прогресс выполнения задачи */
    private TaskProgress progress;

    public TaskExecution(TaskDescriptor descriptor, TaskResult result, TaskExecutionStatus status, TaskProgress progress) {
        this.descriptor = descriptor;
        this.result = result;
        this.status = status;
        this.progress = progress;
    }

    @Override
    public String getId() {
        return descriptor.getId();
    }

    @Override
    public void setId(String id) {
        // do nothing
    }

    @Override
    public void injectId(String targetId) {
        setId(targetId);
    }
}
