package ru.hflabs.rcd.model.document;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import ru.hflabs.rcd.model.ManyToOne;
import ru.hflabs.rcd.model.Named;
import ru.hflabs.rcd.model.annotation.Hashed;
import ru.hflabs.rcd.model.annotation.Indexed;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Класс <class>Field</class> описывает значение поля записи
 *
 * @see MetaField
 */
@Getter
@Setter
@Indexed(
        id = Field.PRIMARY_KEY,
        fields = {
                @Indexed.Field(Field.HISTORY_ID),
                @Indexed.Field(Field.CHANGE_TYPE),
                @Indexed.Field(Field.CHANGE_DATE),
                @Indexed.Field(Field.META_FIELD_ID),
                @Indexed.Field(Field.NAME),
                @Indexed.Field(value = Field.VALUE, search = true)
        }
)
@Hashed(ignore = {Field.PRIMARY_KEY, Field.HISTORY_ID})
public final class Field extends DocumentTemplate implements Named, ManyToOne<MetaField> {

    private static final long serialVersionUID = 4361227682457155862L;

    /*
     * Название полей с идентификаторами
     */
    public static final String META_FIELD_ID = "metaFieldId";
    public static final String NAME = "name";
    public static final String VALUE = "value";

    /** Идентификатор МЕТА-поля, к которому относится значение */
    @NotNull
    @Size(max = PRIMARY_KEY_MAX_SIZE)
    private String metaFieldId;
    /** МЕТА-поле, к которому относится значение */
    private transient MetaField relative;
    /** Значение первичного ключа записи справочника (его MD5 сумма) */
    @NotNull
    @Size(max = PRIMARY_KEY_MAX_SIZE)
    private String name;
    /** Значение поля */
    private String value;

    @Override
    public String getRelativeId() {
        return getMetaFieldId();
    }

    @Override
    public void setRelative(MetaField relative) {
        this.relative = relative;
        setMetaFieldId(relative != null ? relative.getId() : null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (metaFieldId != null ? metaFieldId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        Field that = (Field) o;

        if (metaFieldId != null ? !metaFieldId.equals(that.metaFieldId) : that.metaFieldId != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append(META_FIELD_ID, getMetaFieldId())
                .append(NAME, getName())
                .append(VALUE, getValue())
                .toString();
    }
}
