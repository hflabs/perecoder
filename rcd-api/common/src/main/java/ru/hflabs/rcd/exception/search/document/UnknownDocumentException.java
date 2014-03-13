package ru.hflabs.rcd.exception.search.document;

import ru.hflabs.rcd.exception.ApplicationException;
import ru.hflabs.rcd.exception.ParameterizedException;

/**
 * Класс <class>UnknownDocumentException</class> реализует исключительную ситуацию при поиске документа
 *
 * @see ApplicationException
 */
public abstract class UnknownDocumentException extends ApplicationException implements ParameterizedException {

    private static final long serialVersionUID = -5118976871462531357L;

    public UnknownDocumentException(String message) {
        super(message);
    }

    public UnknownDocumentException(Throwable cause) {
        super(cause);
    }

    public UnknownDocumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
