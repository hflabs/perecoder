package ru.hflabs.rcd.exception.search;

import ru.hflabs.rcd.model.task.TaskDescriptor;

/**
 * Класс <class>UnknownTaskDefinitionException</class> реализует исключитульную ситуацию, возникающую если невозможно найти описание задачи
 *
 * @see UnknownNamedDocumentException
 */
public class UnknownTaskDefinitionException extends UnknownNamedDocumentException {

    private static final long serialVersionUID = -6359956458466084483L;

    public UnknownTaskDefinitionException(String name) {
        super(TaskDescriptor.class, name);
    }
}
