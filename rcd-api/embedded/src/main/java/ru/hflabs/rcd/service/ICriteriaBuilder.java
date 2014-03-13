package ru.hflabs.rcd.service;

import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.criteria.CriteriaHolder;
import ru.hflabs.rcd.model.criteria.FilterCriteria;

/**
 * Интерфейс <class>ICriteriaBuilder</class> декларирует методы сервиса построения критерии на основе фильтра
 *
 * @see Identifying
 * @see CriteriaHolder
 */
public interface ICriteriaBuilder<E extends Identifying, C extends CriteriaHolder<Q>, Q> {

    /**
     * Создает и возвращает пустой критерий для обработки фильтра
     *
     * @param criteriaClass целевой класс критерии
     * @return Возвращает пустой критерий
     */
    C createEmptyCriteria(Class<E> criteriaClass);

    /**
     * Формирует и возвращает критерий
     *
     * @param criteriaClass целевой класс критерии
     * @param filter фильтр построения критерии
     * @return Возвращает построенный критерий запроса
     */
    C createCriteria(Class<E> criteriaClass, FilterCriteria filter);
}
