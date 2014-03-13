package ru.hflabs.rcd.exception.constraint.task;

import ru.hflabs.rcd.exception.constraint.ConstraintException;

/**
 * Класс <class>IllegalTaskParametersException</class> реализует исключительную ситуацию, возникающую при некорректных параметрах задачи
 *
 * @see ConstraintException
 */
public class IllegalTaskParametersException extends ConstraintException {

    private static final long serialVersionUID = 7612474027210871981L;

    public IllegalTaskParametersException(String message) {
        super(message);
    }
}
