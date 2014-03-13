package ru.hflabs.rcd.event;

import org.springframework.context.ApplicationEvent;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Класс <class>ContextEvent</class> реализует базовый класс события приложения
 *
 * @see ApplicationEvent
 */
public abstract class ContextEvent extends ApplicationEvent {

    private static final long serialVersionUID = 6567957482346867127L;

    /** Коллекция уникальных идентификаторов слушателей, которыми событие уже обработано (необходимо для того, чтобы не выполнялись AOP прокси) */
    private final Set<String> handledListeners;

    protected ContextEvent(Object source) {
        super(source);
        this.handledListeners = new ConcurrentSkipListSet<>();
    }

    /**
     * @return Возвращает дату события
     */
    public Date getEventDate() {
        return new Date(getTimestamp());
    }

    /**
     * Регистрирует слушателя в коллекции обработавших данное событие
     *
     * @param id идентификатор слушателя
     * @return Возвращает <code>TRUE</code>, если слушатель успешно зарегистрирован
     */
    public final boolean registryListener(String id) {
        return handledListeners.add(id);
    }

    /**
     * Выполняет перекрытие изначального источника события
     *
     * @param source новый источник события
     */
    public final void overrideSource(Object source) {
        this.source = source;
    }
}
