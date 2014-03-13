package ru.hflabs.rcd.model;

/**
 * Интерфейс <class>Versioned</class> декларирует методы объекта, который обладает версией
 *
 * @see Identifying
 */
public interface Versioned extends Identifying {

    /** Название поля с версией */
    String VERSION = "version";

    /** Максимальный размер поля */
    int VERSION_SIZE = 100;

    /**
     * @return Возвращает версию
     */
    String getVersion();

    /**
     * Устанавливает версию
     *
     * @param version версия
     */
    void setVersion(String version);
}
