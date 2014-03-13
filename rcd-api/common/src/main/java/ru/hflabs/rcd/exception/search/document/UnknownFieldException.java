package ru.hflabs.rcd.exception.search.document;

import ru.hflabs.rcd.exception.search.UnknownNamedDocumentException;
import ru.hflabs.rcd.model.document.Field;

/**
 * Класс <class>UnknownFieldException</class> реализует исключитульную ситуацию, возникающую, если невозможно найти значение поля
 *
 * @see UnknownNamedDocumentException
 */
public class UnknownFieldException extends UnknownNamedDocumentException {

    private static final long serialVersionUID = -4451116862050364000L;

    public UnknownFieldException(String value) {
        super(String.format("Can't find %s with value", Field.class.getSimpleName().toLowerCase()), value);
    }
}
