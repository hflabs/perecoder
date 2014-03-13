package ru.hflabs.rcd.model;

/**
 * Интерфейс <class>Copyable</class> декларирует методы копирования объекта
 *
 * @see Cloneable
 * @see Object#clone()
 */
public interface Copyable extends Cloneable {

    /**
     * Выполняет безопасное глубокое клонирование объекта
     *
     * @return Возвращает копию объекта
     */
    <E> E copy();
}
