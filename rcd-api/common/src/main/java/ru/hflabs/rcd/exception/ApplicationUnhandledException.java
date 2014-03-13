package ru.hflabs.rcd.exception;

/**
 * Класс <class>ApplicationUnhandledException</class> реализует необработанную исключительную ситуацию системы
 *
 * @see ApplicationException
 * @see java.lang.reflect.UndeclaredThrowableException
 */
public class ApplicationUnhandledException extends RuntimeException {

    private static final long serialVersionUID = -8511010259097141658L;

    public ApplicationUnhandledException(String message) {
        super(message);
    }

    public ApplicationUnhandledException(Throwable cause) {
        super(cause);
    }

    public ApplicationUnhandledException(String message, Throwable cause) {
        super(message, cause);
    }
}
