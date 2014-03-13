package ru.hflabs.rcd.service;

import ru.hflabs.rcd.RoleNames;
import ru.hflabs.rcd.model.Identifying;

import javax.annotation.security.RolesAllowed;
import java.util.Collection;
import java.util.Set;

/**
 * Интерфейс <class>IFindService</class> декларирует методы сервиса поиска документов
 *
 * @see Identifying
 */
public interface IFindService<E extends Identifying> extends ISingleClassObserver<E> {

    /**
     * Возвращает документ по его идентификатору
     *
     * @param id идентификатор документа
     * @param fillTransitive флаг необходимости заполнения транзитивных зависимостей
     * @param quietly флаг безопасной проверки существования документа
     * @return Возвращает документ по его идентификатору или <code>NULL</code>, если установлен флаг <i>quietly</i> и такого документа не найдено
     */
    @RolesAllowed(RoleNames.OPERATOR)
    E findByID(String id, boolean fillTransitive, boolean quietly);

    /**
     * Возвращает документы по их идентификаторам
     *
     * @param ids идентификаторы документов
     * @param fillTransitive флаг необходимости заполнения транзитивных зависимостей
     * @param quietly флаг безопасной проверки существования документов
     * @return Возвращает коллекцию найденных документов
     */
    @RolesAllowed(RoleNames.OPERATOR)
    Collection<E> findByIDs(Set<String> ids, boolean fillTransitive, boolean quietly);
}
