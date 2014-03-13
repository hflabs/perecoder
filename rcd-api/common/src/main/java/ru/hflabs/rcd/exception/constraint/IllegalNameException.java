package ru.hflabs.rcd.exception.constraint;

/**
 * Класс <class>IllegalNameException</class> реализует исключительную ситуацию, возникающую при некорректном имени
 *
 * @see ConstraintException
 * @see ru.hflabs.rcd.model.Named
 */
public class IllegalNameException extends ConstraintException {

    private static final long serialVersionUID = -6098930139534757892L;

    public IllegalNameException(String message) {
        super(message);
    }
}
