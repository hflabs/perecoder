package ru.hflabs.rcd.exception.transfer;

/**
 * Класс <class>IncompleteDataException</class> реализует исключительную ситуацию, возникающую, если набор параметров/данных неполный
 *
 * @see CommunicationException
 */
public class IncompleteDataException extends CommunicationException {

    private static final long serialVersionUID = 3457498076413500835L;

    public IncompleteDataException(String message) {
        super(message);
    }

    public IncompleteDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
