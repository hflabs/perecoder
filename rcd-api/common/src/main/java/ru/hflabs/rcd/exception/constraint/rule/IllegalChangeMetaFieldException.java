package ru.hflabs.rcd.exception.constraint.rule;

import ru.hflabs.rcd.model.path.MetaFieldNamedPath;

/**
 * Класс <class>IllegalChangeMetaFieldException</class> реализует исключительную ситуацию, возникающую при некорректной смене МЕТА-поля для правила
 *
 * @see IllegalRecodeRuleSetException
 */
public class IllegalChangeMetaFieldException extends IllegalRecodeRuleSetException {

    private static final long serialVersionUID = 6808273477351702303L;

    /** Старое значение МЕТА-поле */
    private MetaFieldNamedPath oldValue;
    /** Новое знаение МЕТА-поля */
    private MetaFieldNamedPath newValue;

    public IllegalChangeMetaFieldException(MetaFieldNamedPath oldValue, MetaFieldNamedPath newValue) {
        super(String.format("Incorrect change meta field (expected '%s', but got '%s')", oldValue, newValue));
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public MetaFieldNamedPath getOldValue() {
        return oldValue;
    }

    public MetaFieldNamedPath getNewValue() {
        return newValue;
    }
}
