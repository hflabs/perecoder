package ru.hflabs.rcd.model.connector;

/**
 * Интерфейс <class>ConnectorPreference</class> декларирует методы объекта, который содержит настройки соединения с источником/потребителем данных
 */
public interface ConnectorConfiguration {

    /**
     * Возвращает символическое описание конфигурации
     *
     * @return Возвращает описание конфигурации
     */
    String identity();
}
