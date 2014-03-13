package ru.hflabs.rcd.exception.search.rule;

import ru.hflabs.rcd.model.path.FieldNamedPath;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;

/**
 * Класс <class>UnknownRecodeRuleException</class> реализует исключительную ситуацию, возникающую, если невозможно найти правило перекодирования
 *
 * @see UnknownRuleException
 */
public class UnknownRecodeRuleException extends UnknownRuleException {

    private static final long serialVersionUID = -5583219303035776198L;

    public UnknownRecodeRuleException(String message) {
        super(message);
    }

    public UnknownRecodeRuleException(FieldNamedPath fromPath, MetaFieldNamedPath toPath) {
        super(fromPath, toPath);
    }
}
