package ru.hflabs.rcd.exception.search.rule;

import ru.hflabs.rcd.exception.search.UnknownNamedDocumentException;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;

/**
 * Класс <class>UnknownRuleSetNameException</class> реализует исключитульную ситуацию, возникающую, если невозможно найти набор правил по его имени
 *
 * @see UnknownNamedDocumentException
 */
public class UnknownRuleSetNameException extends UnknownNamedDocumentException {

    private static final long serialVersionUID = -3896141925856375532L;

    public UnknownRuleSetNameException(String name) {
        super(RecodeRuleSet.class, name);
    }
}
