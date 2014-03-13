package ru.hflabs.rcd.exception.constraint.task;

import ru.hflabs.rcd.exception.constraint.ConstraintException;

/**
 * Класс <class>IllegalCronSyntaxException</class> реализует исключительную ситуацию, возникающую при некорректном
 * <a href="http://en.wikipedia.org/wiki/Cron">CRON</a> синтаксисе
 *
 * @see ConstraintException
 */
public class IllegalCronSyntaxException extends ConstraintException {

    private static final long serialVersionUID = 7096687430375629994L;

    public IllegalCronSyntaxException(Throwable cause) {
        super(cause);
    }
}
