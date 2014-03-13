package ru.hflabs.rcd.service;

import ru.hflabs.rcd.RoleNames;
import ru.hflabs.rcd.model.ManyToOne;

import javax.annotation.security.RolesAllowed;
import java.util.Collection;

/**
 * Интерфейс <class>IManyToOneService</class> декларирует методы поиска документов по идентификатрам связанных сущностей
 *
 * @see ManyToOne
 */
public interface IManyToOneService<T extends ManyToOne<?>> {

    /**
     * Возвращает уникальное значение сущности по связанному идентификатору
     *
     * @param relativeId идентификатор связанной сущности
     * @param value имя сущности
     * @param fillTransitive флаг необходимости заполнения транзитивных зависимостей
     * @param quietly флаг безопасной проверки существования документа
     * @return Возвращает уникальное значение сущности
     */
    @RolesAllowed(RoleNames.OPERATOR)
    T findUniqueByRelativeId(String relativeId, String value, boolean fillTransitive, boolean quietly);

    /**
     * Возвращает все сущности связанные с указанным идентификатором
     *
     * @param relativeId идентификатор связанной сущности
     * @param searchQuery строка поиска
     * @param fillTransitive флаг необходимости заполнения транзитивных зависимостей
     * @return Возвращает коллекцию найденных сущностей
     */
    @RolesAllowed(RoleNames.OPERATOR)
    Collection<T> findAllByRelativeId(String relativeId, String searchQuery, boolean fillTransitive);
}
