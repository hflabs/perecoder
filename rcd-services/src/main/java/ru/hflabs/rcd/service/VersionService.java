package ru.hflabs.rcd.service;

import java.util.Date;

/**
 * Класс <class>VersionService</class> реализует сервис получения системной информации приложения
 *
 * @author Nazin Alexander
 */
public class VersionService implements IVersionService {

    @Override
    public Date currentTime() {
        return new Date();
    }
}
