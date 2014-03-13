package ru.hflabs.rcd.service;

import ru.hflabs.rcd.RoleNames;
import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.criteria.FilterCriteria;

import javax.annotation.security.RolesAllowed;
import java.util.Collection;
import java.util.Set;

/**
 * Интерфейс <class>IDocumentService</class> декларирует методы для работы с документами
 *
 * @see ru.hflabs.rcd.model.Essence
 */
public interface IDocumentService<E extends Identifying> extends IChangeService<E>, IFilterService<E> {

    /**
     * Обновляет документы
     *
     * @param newObjects коллекция документов для обновления
     * @param oldObjects коллекция существующих документов
     * @param needValidation флаг, указывающий на необходимость валидации обновляемых документов
     * @return Возвращает коллекцию обновленных документов
     */
    @RolesAllowed(RoleNames.ADMINISTRATOR)
    Collection<E> update(Collection<E> newObjects, Collection<E> oldObjects, boolean needValidation);

    /**
     * Закрывает документы по их идентификаторам
     *
     * @param ids коллекция идентификаторов
     */
    @RolesAllowed(RoleNames.ADMINISTRATOR)
    void closeByIDs(Set<String> ids);

    /**
     * Выполняет закрытие документов по критерию
     *
     * @param criteria критерий отбора документов для закрытия
     * @return Возвращает закрытые документы
     */
    @RolesAllowed(RoleNames.ADMINISTRATOR)
    Collection<E> closeByCriteria(FilterCriteria criteria);

    /**
     * Выполняет отмену закрытия документов
     *
     * @param ids идентификаторы документов
     * @return Возвращает переоткрытые документы
     */
    @RolesAllowed(RoleNames.ADMINISTRATOR)
    Collection<E> reopen(Set<String> ids);
}
