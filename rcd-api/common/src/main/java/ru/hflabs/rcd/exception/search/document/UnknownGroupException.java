package ru.hflabs.rcd.exception.search.document;

import ru.hflabs.rcd.exception.search.UnknownNamedDocumentException;
import ru.hflabs.rcd.model.document.Group;

/**
 * Класс <class>UnknownGroupException</class> реализует исключитульную ситуацию, возникающую, если невозможно найти группу справочников
 *
 * @see UnknownNamedDocumentException
 */
public class UnknownGroupException extends UnknownNamedDocumentException {

    private static final long serialVersionUID = -558756724085596512L;

    public UnknownGroupException(String name) {
        super(Group.class, name);
    }
}
