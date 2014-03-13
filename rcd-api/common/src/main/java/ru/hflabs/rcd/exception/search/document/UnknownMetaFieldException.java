package ru.hflabs.rcd.exception.search.document;

import ru.hflabs.rcd.exception.search.UnknownNamedDocumentException;
import ru.hflabs.rcd.model.document.MetaField;

/**
 * Класс <class>UnknownMetaFieldException</class> реализует исключитульную ситуацию, возникающую, если невозможно найти МЕТА-поле справочника
 *
 * @see UnknownNamedDocumentException
 */
public class UnknownMetaFieldException extends UnknownNamedDocumentException {

    private static final long serialVersionUID = 6221129607227664276L;

    public UnknownMetaFieldException(String name) {
        super(MetaField.class, name);
    }
}
