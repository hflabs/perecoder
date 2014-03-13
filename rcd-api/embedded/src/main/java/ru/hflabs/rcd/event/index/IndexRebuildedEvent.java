package ru.hflabs.rcd.event.index;

import lombok.Getter;
import ru.hflabs.rcd.event.ContextEvent;

/**
 * Класс <class>IndexRebuildedEvent</class> содержит информацию о событии перестроения хранилища документов
 *
 * @see IndexRebuildEvent
 */
@Getter
public class IndexRebuildedEvent extends ContextEvent {

    private static final long serialVersionUID = 6841251397903863505L;

    /** Целевой класс */
    private final Class<?> targetClass;
    /** Количество документов в индексе */
    private final int documentCount;

    public IndexRebuildedEvent(Object source, Class<?> targetClass, int documentCount) {
        super(source);
        this.targetClass = targetClass;
        this.documentCount = documentCount;
    }
}
