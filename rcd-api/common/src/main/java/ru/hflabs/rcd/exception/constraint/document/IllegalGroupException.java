package ru.hflabs.rcd.exception.constraint.document;

import ru.hflabs.rcd.exception.constraint.ConstraintException;

/**
 * Класс <class>IllegalGroupException</class> реализует исключительную ситуацию, возникающую при некоррекной группе справочников
 *
 * @see ConstraintException
 */
public class IllegalGroupException extends ConstraintException {

    private static final long serialVersionUID = -3599632893384608285L;

    public IllegalGroupException(String message) {
        super(message);
    }
}
