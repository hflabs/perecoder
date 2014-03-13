package ru.hflabs.rcd.event.task;

import lombok.Getter;

/**
 * Класс <class>TaskSchedulerEvent</class> содержит информацию о событии постановки задачи в очередь на выполнение
 *
 * @see TaskEvent
 * @see ru.hflabs.rcd.model.task.TaskExecutionStatus
 */
@Getter
public class TaskScheduleEvent extends TaskEvent {

    private static final long serialVersionUID = 3748036119220172663L;

    /** Триггер задачи */
    private final String cron;

    public TaskScheduleEvent(Object source, String id, String performerName, String cron) {
        super(source, id, performerName);
        this.cron = cron;
    }
}
