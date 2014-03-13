package ru.hflabs.rcd.exception.constraint.document;

import ru.hflabs.rcd.exception.constraint.ConstraintException;

/**
 * Класс <class>IllegalDictionaryException</class> реализует исключительную ситуацию, возникающую при некоррекном справочнике
 *
 * @see ConstraintException
 */
public class IllegalDictionaryException extends ConstraintException {

    private static final long serialVersionUID = -7451942486501166167L;

    public IllegalDictionaryException(String message) {
        super(message);
    }
}
