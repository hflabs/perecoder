package ru.hflabs.rcd.exception.constraint.document;

import ru.hflabs.rcd.exception.constraint.ConstraintException;

/**
 * Класс <class>IllegalRecordException</class> реализует исключительную ситуацию, возникающую при некорректной записи справочника
 *
 * @see ConstraintException
 */
public class IllegalRecordException extends ConstraintException {

    private static final long serialVersionUID = -1339335700846746347L;

    public IllegalRecordException(String message) {
        super(message);
    }
}
