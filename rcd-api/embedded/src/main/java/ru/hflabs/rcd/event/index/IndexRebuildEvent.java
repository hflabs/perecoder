package ru.hflabs.rcd.event.index;

import lombok.Getter;
import lombok.Setter;
import ru.hflabs.rcd.event.ContextEvent;

import java.util.Set;

/**
 * Класс <class>IndexRebuildEvent</class> содержит информацию о событии запроса перестроения хранилища документов
 *
 * @see IndexRebuildedEvent
 */
@Getter
@Setter
public class IndexRebuildEvent extends ContextEvent {

    private static final long serialVersionUID = 6841251397903863505L;

    /** Коллекция целевых классов или <code>NULL</code> */
    private final Set<Class<?>> targetClasses;
    /** Флаг принудительного перестроения */
    private boolean force;

    public IndexRebuildEvent(Object source) {
        this(source, null);
    }

    public IndexRebuildEvent(Object source, Set<Class<?>> targetClasses) {
        super(source);
        this.targetClasses = targetClasses;
        this.force = false;
    }
}
