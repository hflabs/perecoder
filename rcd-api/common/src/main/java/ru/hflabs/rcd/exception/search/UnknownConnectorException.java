package ru.hflabs.rcd.exception.search;

import ru.hflabs.rcd.exception.ApplicationException;

/**
 * Класс <class>UnknownConnectorException</class> реализует исключительную ситуацию, возникающую при некорректной настройке коннектора
 *
 * @see ApplicationException
 */
public class UnknownConnectorException extends ApplicationException {

    private static final long serialVersionUID = 3883876674520799342L;

    public UnknownConnectorException(String message) {
        super(message);
    }
}
