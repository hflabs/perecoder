package ru.hflabs.rcd.exception.constraint.document;

import ru.hflabs.rcd.exception.constraint.ConstraintException;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;

/**
 * Класс <class>NotUniqueFieldsException</class> реализует исключительную ситуацию, возникающую при попытке модифицировать уникальное значение поля
 *
 * @see ConstraintException
 * @see ru.hflabs.rcd.model.document.MetaField#FLAG_UNIQUE_NAME
 */
public class NotUniqueFieldsException extends ConstraintException {

    private static final long serialVersionUID = -536057296611472727L;

    public NotUniqueFieldsException(String message) {
        super(message);
    }

    public NotUniqueFieldsException(MetaFieldNamedPath value) {
        this(String.format("Fields value for '%s' is not unique", value));
    }
}
