package ru.hflabs.rcd.service;

import ru.hflabs.rcd.RoleNames;
import ru.hflabs.rcd.model.notification.Notification;
import ru.hflabs.rcd.model.notification.NotifyState;

import javax.annotation.security.RolesAllowed;
import java.util.Collection;
import java.util.Set;

/**
 * Интерфейс <class>INotificationService</class> декларирует методы работы с оповещениями
 *
 * @see Notification
 */
public interface INotificationService extends IFilterService<Notification> {

    /**
     * Выполняет смену статуса оповещения
     *
     * @param ids коллекция идентификаторов
     * @param notifyState новое состояние
     * @return Возвращает коллекцию измененных оповещений
     */
    @RolesAllowed(RoleNames.ADMINISTRATOR)
    Collection<Notification> changeNotifyState(Set<String> ids, NotifyState notifyState);
}
