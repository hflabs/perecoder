package ru.hflabs.rcd.model;

import java.io.Serializable;

/**
 * Интерфейс <class>Named</class> декларирует методы объекта, который обладает уникальным именем в пределах своего типа
 *
 * @see Serializable
 */
public interface Named extends Serializable {

    /** Название поля с именем */
    String NAME = "name";

    /** Минимальный размер названия */
    int NAME_MIN_SIZE = 1;
    /** Максимальный размер названия */
    int NAME_MAX_SIZE = 255;

    /**
     * @return Возвращает имя
     */
    String getName();

    /**
     * Устанавливает имя
     *
     * @param name имя
     */
    void setName(String name);
}
