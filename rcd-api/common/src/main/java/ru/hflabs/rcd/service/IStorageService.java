package ru.hflabs.rcd.service;

import java.util.Iterator;
import java.util.List;

/**
 * Интерфейс <class>IStorageService</class> декларирует методы хранилища сущностей
 *
 * @see ISingleClassObserver
 */
public interface IStorageService<E> extends ISingleClassObserver<E> {

    /**
     * @return Возвращает общее количество сущностей в хранилище
     */
    Integer totalCount();

    /**
     * @return Возвращает все сущности
     */
    List<E> getAll();

    /**
     * @param fetchSize размер страницы итерирования
     * @param cacheSize размер кеша страниц итерирования
     * @return Возвращает итератор сущностей
     */
    Iterator<List<E>> iterateAll(int fetchSize, int cacheSize);
}
