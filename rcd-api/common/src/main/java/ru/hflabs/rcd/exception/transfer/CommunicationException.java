package ru.hflabs.rcd.exception.transfer;

import ru.hflabs.rcd.exception.ApplicationException;

/**
 * Класс <class>CommunicationException</class> реализует исключительную ситуацию, возникающую при ошибке взаимодействия с внешним сервисом
 *
 * @see ApplicationException
 */
public class CommunicationException extends ApplicationException {

    private static final long serialVersionUID = 3066111521026192905L;

    public CommunicationException(String message) {
        super(message);
    }

    public CommunicationException(Throwable cause) {
        super(cause);
    }

    public CommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
