package ru.hflabs.rcd.exception.constraint.document;

/**
 * Класс <class>PrimaryMetaFieldException</class> реализует исключительную ситуацию, возникающую при некорректном значении первичного МЕТА-поля
 *
 * @see IllegalMetaFieldException
 * @see ru.hflabs.rcd.model.document.MetaField#FLAG_PRIMARY_NAME
 */
public class PrimaryMetaFieldException extends IllegalMetaFieldException {

    private static final long serialVersionUID = -536057296611472727L;

    public PrimaryMetaFieldException(String name) {
        super(String.format("Meta field '%s' is primary", name));
    }
}
