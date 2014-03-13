package ru.hflabs.rcd.service;

import ru.hflabs.rcd.model.criteria.FilterResult;

/**
 * Интерфейс <class>IPagingService</class> декларирует методы получения результата фильтрации страницы объектов
 *
 * @see FilterResult
 */
public interface IPagingService<T> {

    /**
     * @param count количество запрашиваемых объектов
     * @param offset смещение относительно начала объектов
     * @return Возвращает результат фильтрации объектов
     */
    FilterResult<T> findPage(int count, int offset);
}
