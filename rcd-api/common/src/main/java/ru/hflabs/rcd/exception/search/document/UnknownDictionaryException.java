package ru.hflabs.rcd.exception.search.document;

import ru.hflabs.rcd.exception.search.UnknownNamedDocumentException;
import ru.hflabs.rcd.model.document.Dictionary;

/**
 * Класс <class>UnknownDictionaryException</class> реализует исключитульную ситуацию, возникающую, если невозможно найти справочник
 *
 * @see UnknownNamedDocumentException
 */
public class UnknownDictionaryException extends UnknownNamedDocumentException {

    private static final long serialVersionUID = 6886628619296371542L;

    public UnknownDictionaryException(String name) {
        super(Dictionary.class, name);
    }
}
