package ru.hflabs.rcd.service;

import ru.hflabs.rcd.model.change.Diff;

import java.util.Collection;

/**
 * Интерфейс <class>IDifferenceService</class> декларирует методы сервиса работы с информацией об изменении сущностей
 *
 * @see Diff
 */
public interface IDifferenceService<E> {

    /**
     * Выполняет построение коллекции {@link Diff}-ов
     *
     * @param from старое значение сущности
     * @param to новое значение сущности
     * @return Возвращает коллекцию изменений или <code>NULL</code>, если изменений не обнаружено
     */
    Collection<Diff> createDiff(E from, E to);

    /**
     * Выполняет создание хеш суммы полей, которые учествуют в построении {@link Diff}-ов
     *
     * @param target целевая сущность
     * @return Возвращает хеш код сущности
     */
    String createHashCode(E target);
}
