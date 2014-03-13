package ru.hflabs.rcd.exception.constraint;

/**
 * Класс <class>IllegalPrimaryKeyException</class> реализует исключительную ситуацию, возникающую при некорректном первичном ключе
 *
 * @see ConstraintException
 * @see ru.hflabs.rcd.model.Identifying
 */
public class IllegalPrimaryKeyException extends ConstraintException {

    private static final long serialVersionUID = 8281418074907532359L;

    public IllegalPrimaryKeyException(String message) {
        super(message);
    }

    public IllegalPrimaryKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}
