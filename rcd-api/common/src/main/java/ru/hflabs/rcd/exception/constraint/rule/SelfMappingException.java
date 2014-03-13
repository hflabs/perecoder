package ru.hflabs.rcd.exception.constraint.rule;

import ru.hflabs.rcd.model.path.MetaFieldNamedPath;

/**
 * Класс <class>SelfMappingException</class> реализует исключительную ситуацию, возникающую, если набор правил ссылается сам на себя
 *
 * @see IllegalRecodeRuleSetException
 */
public class SelfMappingException extends IllegalRecodeRuleSetException {

    private static final long serialVersionUID = -6190241098405216216L;

    /** Старое значение МЕТА-поле */
    private MetaFieldNamedPath value;

    public SelfMappingException(MetaFieldNamedPath value) {
        this(String.format("Mapping '%s' to itself is not allowed", value), value);
    }

    public SelfMappingException(String message, MetaFieldNamedPath value) {
        super(message);
        this.value = value;
    }

    public MetaFieldNamedPath getValue() {
        return value;
    }
}
