package ru.hflabs.rcd.exception.constraint.rule;

import ru.hflabs.rcd.exception.constraint.ConstraintException;

/**
 * Класс <class>IllegalRecodeRuleException</class> реализует исключительную ситуацию, возникающую при некорректном правиле перекодирования
 *
 * @see ConstraintException
 */
public class IllegalRecodeRuleException extends ConstraintException {

    private static final long serialVersionUID = 7673087784834115212L;

    public IllegalRecodeRuleException(String message) {
        super(message);
    }
}
