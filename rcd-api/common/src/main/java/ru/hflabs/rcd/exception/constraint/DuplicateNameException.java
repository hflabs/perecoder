package ru.hflabs.rcd.exception.constraint;

/**
 * Класс <class>NameAlreadyExitException</class> реализует исключительную ситуацию, возникающую при дублировании имени
 *
 * @see IllegalNameException
 */
public class DuplicateNameException extends IllegalNameException {

    private static final long serialVersionUID = 552913853650659086L;

    public DuplicateNameException(String message) {
        super(message);
    }
}
