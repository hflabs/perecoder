package ru.hflabs.rcd.service;

import ru.hflabs.rcd.model.Identifying;

import java.util.Collection;

/**
 * Интерфейс <class>ISequenceGenerator</class> декларирует методы для генерации уникальных идентификаторов
 *
 * @see Identifying#setId(String)
 */
public interface ISequenceService extends ISequenceGenerator {

    /**
     * Выполняет заполнение идентификатора для сущности
     *
     * @param object сущность
     * @param overrideExisted флаг, указывающий на перезапись существующего идентификатора
     * @return Возвращает сущность с заполненным идентификатором
     */
    <E extends Identifying> E fillIdentifier(E object, boolean overrideExisted);

    /**
     * Выполняет заполнение идентификаторов для коллекции сущностей
     *
     * @param objects коллекция сущностей
     * @param overrideExisted флаг, указывающий на перезапись существующего идентификатора
     * @return Возвращает коллекцию с заполненными идентификаторами
     */
    <E extends Identifying> Collection<E> fillIdentifiers(Collection<E> objects, boolean overrideExisted);
}
