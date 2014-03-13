package ru.hflabs.rcd.service.notification.collector;

import ru.hflabs.rcd.model.notification.Notification;

import java.util.Collection;

/**
 * Интерфейс <class>NotificationCollector</class> декларирует методы коллектора оповещений
 *
 * @author Nazin Alexander
 */
public interface NotificationCollector {

    /**
     * Добавляет оповещение
     *
     * @param notification добавляемое оповещение
     */
    boolean appendNotification(Notification notification) throws Exception;

    /**
     * Возвращает коллекцию групп оповещений
     *
     * @return Возвращает коллекцию групп оповещений
     */
    Collection<Notification> retrieveNotifications();
}
