package ru.hflabs.rcd.exception.constraint;

import ru.hflabs.rcd.exception.ApplicationException;

/**
 * Класс <class>ConstraintException</class> реализует исключительную ситуацию, возникающую при нарушении системного ограничения
 *
 * @see ApplicationException
 */
public class ConstraintException extends ApplicationException {

    private static final long serialVersionUID = 3055548156631181173L;

    public ConstraintException(String message) {
        super(message);
    }

    public ConstraintException(Throwable cause) {
        super(cause);
    }

    public ConstraintException(String message, Throwable cause) {
        super(message, cause);
    }
}
