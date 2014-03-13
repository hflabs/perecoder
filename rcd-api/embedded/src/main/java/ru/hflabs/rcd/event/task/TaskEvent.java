package ru.hflabs.rcd.event.task;

import lombok.Getter;
import ru.hflabs.rcd.event.ContextEvent;

/**
 * Класс <class>TaskEvent</class> содержит информацию о событии задачи
 *
 * @see ContextEvent
 */
@Getter
public abstract class TaskEvent extends ContextEvent {

    private static final long serialVersionUID = 6390837470503805228L;

    /** Идентификатор задачи */
    private final String id;
    /** Идентификатор исполнителя задачи */
    private final String performerName;

    public TaskEvent(Object source, String id, String performerName) {
        super(source);
        this.id = id;
        this.performerName = performerName;
    }

    public String identity() {
        return String.format("'%s[%s]'", id, performerName);
    }
}
