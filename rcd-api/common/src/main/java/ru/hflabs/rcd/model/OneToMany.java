package ru.hflabs.rcd.model;

import java.util.Collection;

/**
 * Интерфейс <class>OneToMany</class> декларирует методы объекта, у которого есть потомки
 *
 * @see ManyToOne
 */
public interface OneToMany<T extends Identifying & ManyToOne<?>> {

    /**
     * @return Возвращает коллекцию связанных сущностей
     */
    Collection<T> getDescendants();

    /**
     * Устанавливает коллекцию связанных сущностей
     *
     * @param relative сущности
     */
    void setDescendants(Collection<T> relative);
}
