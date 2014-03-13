package ru.hflabs.rcd.model;

/**
 * Интерфейс <class>ManyToOne</class> декларирует методы объекта, у которого есть родительская сущность
 *
 * @see OneToMany
 */
public interface ManyToOne<T extends Identifying> {

    /**
     * @return Возвращает идентификатор связанной сущности
     */
    String getRelativeId();

    /**
     * @return Возвращает связанную сущность
     */
    T getRelative();

    /**
     * Устанавливает связанную сущность
     *
     * @param relative связанная сущность
     */
    void setRelative(T relative);
}
