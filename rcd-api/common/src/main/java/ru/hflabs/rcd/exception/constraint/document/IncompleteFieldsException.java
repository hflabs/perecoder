package ru.hflabs.rcd.exception.constraint.document;

import ru.hflabs.rcd.exception.constraint.ConstraintException;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;

/**
 * Класс <class>IncompleteFieldsException</class> реализует исключительную ситуацию, возникающую при неполном наборе значений полей
 *
 * @see ConstraintException
 */
public class IncompleteFieldsException extends ConstraintException {

    private static final long serialVersionUID = 6723345911071786486L;

    /** МЕТА-поле, для которого нехватает значений */
    private final MetaFieldNamedPath value;

    public IncompleteFieldsException(MetaFieldNamedPath value) {
        super(String.format("Field values ​​for '%s' are incomplete", value));
        this.value = value;
    }

    public MetaFieldNamedPath getValue() {
        return value;
    }
}
