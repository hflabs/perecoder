package ru.hflabs.rcd.exception.constraint.rule;

import ru.hflabs.rcd.exception.constraint.ConstraintException;

/**
 * Класс <class>IllegalRecodeRuleException</class> реализует исключительную ситуацию, возникающую при некорректном наборе правил перекодирования
 *
 * @see ConstraintException
 */
public class IllegalRecodeRuleSetException extends ConstraintException {

    private static final long serialVersionUID = -8393557685995001545L;

    public IllegalRecodeRuleSetException(String message) {
        super(message);
    }
}
