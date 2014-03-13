package ru.hflabs.rcd.exception.constraint.document;

import ru.hflabs.rcd.exception.constraint.ConstraintException;

/**
 * Класс <class>IllegalFieldException</class> реализует исключительную ситуацию, возникающую при некоррекном значении поля
 *
 * @see ConstraintException
 */
public class IllegalFieldException extends ConstraintException {

    private static final long serialVersionUID = -716493356098476563L;

    public IllegalFieldException(String message) {
        super(message);
    }
}
