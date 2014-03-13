package ru.hflabs.rcd.exception.search.rule;

import ru.hflabs.rcd.model.path.MetaFieldNamedPath;

/**
 * Класс <class>UnknownRecodeRuleSetException</class> реализует исключительную ситуацию, возникающую, если невозможно найти набор правил перекодирования
 *
 * @see UnknownRuleException
 */
public class UnknownRecodeRuleSetException extends UnknownRuleException {

    private static final long serialVersionUID = -2370445024920623154L;

    public UnknownRecodeRuleSetException(String message) {
        super(message);
    }

    public UnknownRecodeRuleSetException(MetaFieldNamedPath fromPath, MetaFieldNamedPath toPath) {
        super(fromPath, toPath);
    }
}
