package ru.hflabs.rcd.service;

import ru.hflabs.rcd.RoleNames;
import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.criteria.FilterCriteria;
import ru.hflabs.rcd.model.criteria.FilterResult;

import javax.annotation.security.RolesAllowed;
import java.util.Collection;

/**
 * Интерфейс <class>IFilterService</class> декларирует методы сервиса фильтрации документов
 *
 * @see FilterCriteria
 * @see FilterResult
 */
public interface IFilterService<E extends Identifying> extends IFindService<E> {

    /**
     * Возвращает результат фильтрации коллекции документов
     *
     * @param criteria критерий поиска
     * @param fillTransitive флаг необходимости заполнения транзитивных зависимостей
     * @return Возвращает результат фильтрации
     */
    @RolesAllowed(RoleNames.OPERATOR)
    FilterResult<E> findByCriteria(FilterCriteria criteria, boolean fillTransitive);


    /**
     * Возвращает <i>все</i> документы по заданному критерию.
     * <p/>
     * Метод <u>не рекоммендуется</u> использовать, если известно, что результат выборки будет заведомо большим
     *
     * @param criteria критерий поиска
     * @param fillTransitive флаг необходимости заполнения транзитивных зависимостей
     * @return Возвращает все найденные документы по критерию
     */
    @RolesAllowed(RoleNames.OPERATOR)
    Collection<E> findAllByCriteria(FilterCriteria criteria, boolean fillTransitive);

    /**
     * Возвращает количество документов по критерию
     *
     * @param criteria критерий поиска
     * @return Возвращает количество документов
     */
    @RolesAllowed(RoleNames.OPERATOR)
    int countByCriteria(FilterCriteria criteria);
}
