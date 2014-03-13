package ru.hflabs.rcd.model.change;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

/**
 * Класс <class>Diff</class> содержит информацию об изменении
 */
@Getter
@Setter
public final class Diff implements Serializable {

    private static final long serialVersionUID = -7655094747159506085L;

    /** Класс целевого поля */
    private String fieldClass;
    /** Название поля */
    private String field;
    /** Старое значение */
    private String oldValue;
    /** Новое значение */
    private String newValue;

    public Diff(String fieldClass, String field, String oldValue, String newValue) {
        this.fieldClass = fieldClass;
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("fieldClass", fieldClass)
                .append("field", field)
                .append("oldValue", oldValue)
                .append("newValue", newValue)
                .toString();
    }
}
