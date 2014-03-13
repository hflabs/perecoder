package ru.hflabs.rcd.service;

import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.criteria.CriteriaHolder;
import ru.hflabs.util.core.Three;

import java.util.Collection;

/**
 * Интерфейс <class>IQueryProvider</class> декларирует методы провайдера выполнения запросов
 *
 * @see ICriteriaBuilder
 * @see CriteriaHolder
 */
public interface IQueryProvider<E extends Identifying, C extends CriteriaHolder<?>> {

    /**
     * Выполняет поиск документов по подготовленному критерию
     *
     * @param criteria критерий поиска
     * @param offset смещение относительно начала объектов
     * @param count количество запрашиваемых объектов
     * @return Возвращает результат фильтрации, состоящий из коллекции найденных документов, количества документов по фильтру, общего количества документов
     */
    Three<Collection<E>, Integer, Integer> executeByCriteria(C criteria, int offset, int count);

    /**
     * Возвращает количество документов по подготовленному критерию
     *
     * @param criteria критерий поиска
     * @return Возвращает количество найденных документов
     */
    Integer executeCountByCriteria(C criteria);
}
