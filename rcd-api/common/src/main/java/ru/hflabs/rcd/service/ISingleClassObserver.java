package ru.hflabs.rcd.service;

/**
 * Интерфейс <class>ISingleClassObserver</class> декларирует методы сервиса, отслеживающего сущность одного класса
 */
public interface ISingleClassObserver<E> {

    /**
     * @return Возвращает класс отслеживаемых объектов
     */
    Class<E> retrieveTargetClass();
}
