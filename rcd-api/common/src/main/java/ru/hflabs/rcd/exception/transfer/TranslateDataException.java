package ru.hflabs.rcd.exception.transfer;

/**
 * Класс <class>TranslateDataException</class> реализует исключительную ситуацию, возникающую, если невозможно разобрать поступившие данные
 *
 * @see CommunicationException
 */
public class TranslateDataException extends CommunicationException {

    private static final long serialVersionUID = 7583742037678586309L;

    public TranslateDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
