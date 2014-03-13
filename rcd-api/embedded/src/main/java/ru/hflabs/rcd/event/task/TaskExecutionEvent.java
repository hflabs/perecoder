package ru.hflabs.rcd.event.task;

import lombok.Getter;
import ru.hflabs.rcd.model.task.TaskExecutionStatus;
import ru.hflabs.rcd.model.task.TaskResult;

import java.util.concurrent.Future;

/**
 * Класс <class>TaskExecutionEvent</class> содержит информацию о событии изменения состояния выполняемой задачи
 *
 * @see TaskEvent
 * @see ru.hflabs.rcd.model.task.TaskExecution
 */
@Getter
public class TaskExecutionEvent extends TaskEvent {

    private static final long serialVersionUID = 6390837470503805228L;

    /** Статус выполняемой задачи */
    private final TaskExecutionStatus status;

    public TaskExecutionEvent(Future<TaskResult> source, String executionId, String performerName, TaskExecutionStatus status) {
        super(source, executionId, performerName);
        this.status = status;
    }

    @Override
    public String identity() {
        return super.identity() + ": " + status;
    }
}
