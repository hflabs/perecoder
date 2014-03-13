package ru.hflabs.rcd.exception.constraint.rule;

import ru.hflabs.rcd.exception.constraint.ConstraintException;

/**
 * Класс <class>AttachedRecodeRuleException</class> реализует исключительную ситуацию, возникающую при модификации документа,
 * к которому привязано правило перекодирования
 *
 * @see ConstraintException
 */
public class AttachedRecodeRuleException extends ConstraintException {

    private static final long serialVersionUID = 3873394628089542051L;

    public AttachedRecodeRuleException(String message) {
        super(message);
    }
}
