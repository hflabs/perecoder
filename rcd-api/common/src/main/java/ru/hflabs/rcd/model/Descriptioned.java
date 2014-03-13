package ru.hflabs.rcd.model;

import java.io.Serializable;

/**
 * Интерфейс <class>Descriptioned</class> декларирует методы объекта, который обладает описанием
 */
public interface Descriptioned extends Serializable {

    /** Название поля с описанием */
    String DESCRIPTION = "description";

    /** Максимальный размер описания */
    int DESCRIPTION_SIZE = 1000;

    /**
     * @return Возвращает описание сущности
     */
    String getDescription();

    /**
     * Устанавливает описание сущности
     *
     * @param description описание
     */
    void setDescription(String description);
}
