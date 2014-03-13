package ru.hflabs.rcd.storage;

import java.util.Collection;

/**
 * Интерфейс <class>ChangeDocumentCallback</class> декларирует методы процессора изменений документов
 *
 * @author Nazin Alexander
 */
public interface ChangeDocumentCallback<E> {

    /**
     * Действия перед модификацией
     *
     * @param objects коллекция изменившихся объектов
     * @return Возвращает коллекцию модифицированных объектов
     */
    Collection<E> beforeModify(Collection<E> objects);

    /**
     * Действия модификации
     *
     * @param objects коллекция изменившихся объектов
     * @return Возвращает коллекцию модифицированных объектов
     */
    Collection<E> doModify(Collection<E> objects);

    /**
     * Действия по завершению модификации
     *
     * @param objects коллекция изменившихся объектов
     * @return Возвращает коллекцию модифицированных объектов
     */
    Collection<E> afterModify(Collection<E> objects);
}
