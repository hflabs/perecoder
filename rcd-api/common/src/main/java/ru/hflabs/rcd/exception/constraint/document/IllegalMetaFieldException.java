package ru.hflabs.rcd.exception.constraint.document;

import ru.hflabs.rcd.exception.constraint.ConstraintException;

/**
 * Класс <class>IllegalMetaFieldException</class> реализует исключительную ситуацию, возникающую при некоррекном МЕТА-поле
 *
 * @see ConstraintException
 */
public class IllegalMetaFieldException extends ConstraintException {

    private static final long serialVersionUID = -6573606591440925314L;

    public IllegalMetaFieldException(String message) {
        super(message);
    }
}
