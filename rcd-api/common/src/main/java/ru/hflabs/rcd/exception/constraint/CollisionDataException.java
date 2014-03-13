package ru.hflabs.rcd.exception.constraint;

/**
 * Класс <class>CollisionDataException</class> реализует исключительную ситуацию, возникающую, если между параметрами/данными возникает конфликт
 *
 * @see ConstraintException
 */
public class CollisionDataException extends ConstraintException {

    private static final long serialVersionUID = 7897258626980887437L;

    public CollisionDataException(String message) {
        super(message);
    }
}
