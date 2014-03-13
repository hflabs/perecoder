package ru.hflabs.rcd.exception;

/**
 * Класс <class>ApplicationException</class> реализует общую исключительную ситуацию системы
 *
 * @see RuntimeException
 */
public class ApplicationException extends RuntimeException {

    private static final long serialVersionUID = -3550012233011033698L;

    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(Throwable cause) {
        super(cause);
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
