package ru.hflabs.rcd.service;

import ru.hflabs.rcd.RoleNames;
import ru.hflabs.rcd.model.Identifying;

import javax.annotation.security.RolesAllowed;
import java.util.Collection;

/**
 * Интерфейс <class>IChangeService</class> декларирует методы для изменения документов
 *
 * @see Identifying
 */
public interface IChangeService<E extends Identifying> extends ISingleClassObserver<E> {

    /**
     * Создает документы
     *
     * @param objects коллекция создаваемых документов
     * @param needValidation флаг, указывающий на необходимость валидации создаваемых документов
     * @return Возвращает созданную коллекцию документов
     */
    @RolesAllowed(RoleNames.ADMINISTRATOR)
    Collection<E> create(Collection<E> objects, boolean needValidation);

    /**
     * Обновляет существующие документы
     *
     * @param objects коллекция документов для обновления
     * @param needValidation флаг, указывающий на необходимость валидации обновляемых документов
     * @return Возвращает обновленные документы
     */
    @RolesAllowed(RoleNames.ADMINISTRATOR)
    Collection<E> update(Collection<E> objects, boolean needValidation);

    /**
     * Закрывает документы
     *
     * @param objects коллекция документов для закрытия
     * @param needValidation флаг, указывающий на необходимость валидации удаляемых документов
     * @return Возвращает обновленные документы
     */
    @RolesAllowed(RoleNames.ADMINISTRATOR)
    Collection<E> close(Collection<E> objects, boolean needValidation);
}
