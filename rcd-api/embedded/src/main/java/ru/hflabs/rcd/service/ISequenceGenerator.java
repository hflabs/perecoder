package ru.hflabs.rcd.service;

import ru.hflabs.rcd.model.Identifying;

/**
 * Интерфейс <class>ISequenceGenerator</class> декларирует методы для генерации уникальных идентификаторов
 *
 * @see Identifying#getId()
 */
public interface ISequenceGenerator {

    /**
     * Создает и возвращает уникальный идентификатор для сущности
     *
     * @param targetClass класс целевой сущности
     * @return Возвращает уникальный идентификатор
     */
    <E extends Identifying> String createIdentifier(Class<E> targetClass);
}
