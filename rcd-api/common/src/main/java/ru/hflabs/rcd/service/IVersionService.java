package ru.hflabs.rcd.service;

import java.util.Date;

/**
 * Интерфейс <class>IVersionService</class> декларирует методы получения системной информации приложения
 *
 * @see ru.hflabs.rcd.Version
 */
public interface IVersionService {

    /**
     * Возвращает текущее время
     *
     * @return Возвращает текущее время
     */
    Date currentTime();
}
