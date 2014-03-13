package ru.hflabs.rcd.exception.search;

import ru.hflabs.rcd.exception.search.document.UnknownDocumentException;

/**
 * Класс <class>UnknownNamedDocumentException</class> реализует исключитульную ситуацию, возникающую, если невозможно найти именованный документ
 *
 * @see UnknownDocumentException
 */
public class UnknownNamedDocumentException extends UnknownDocumentException {

    private static final long serialVersionUID = 5769670506468362971L;

    /** Шаблон сообщения об ошибки */
    private static final String MESSAGE_TEMPLATE = "Can't find %s with name";
    /** Имя несуществующего документа */
    private final String name;

    public UnknownNamedDocumentException(Class<?> targetClass, String name) {
        this(String.format(MESSAGE_TEMPLATE, targetClass.getSimpleName().toLowerCase()), name);
    }

    public UnknownNamedDocumentException(String template, String name) {
        super(String.format(template + " '%s'", name));
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Object[] getExceptionParameters() {
        return new Object[]{name};
    }
}
