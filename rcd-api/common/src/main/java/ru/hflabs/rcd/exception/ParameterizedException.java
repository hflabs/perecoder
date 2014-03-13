package ru.hflabs.rcd.exception;

/**
 * Интерфейс <class>ParameterizedException</class> декларирует методы исключительной ситуации, которая обладает параметрами локализации
 */
public interface ParameterizedException {

    /**
     * @return Возвращает параметры локализации
     */
    Object[] getExceptionParameters();
}
