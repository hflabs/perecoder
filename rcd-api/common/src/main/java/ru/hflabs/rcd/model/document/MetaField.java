package ru.hflabs.rcd.model.document;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import ru.hflabs.rcd.model.Descriptioned;
import ru.hflabs.rcd.model.ManyToOne;
import ru.hflabs.rcd.model.Named;
import ru.hflabs.rcd.model.OneToMany;
import ru.hflabs.rcd.model.annotation.Hashed;
import ru.hflabs.rcd.model.annotation.Indexed;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Collection;

import static ru.hflabs.util.core.EqualsUtil.lowerCaseEquals;
import static ru.hflabs.util.core.EqualsUtil.lowerCaseHashCode;

/**
 * Класс <class>MetaField</class> описывает МЕТА-информацию поля справочника
 *
 * @see Dictionary
 */
@Getter
@Setter
@Indexed(
        id = MetaField.PRIMARY_KEY,
        fields = {
                @Indexed.Field(MetaField.HISTORY_ID),
                @Indexed.Field(MetaField.CHANGE_TYPE),
                @Indexed.Field(MetaField.CHANGE_DATE),
                @Indexed.Field(MetaField.DICTIONARY_ID),
                @Indexed.Field(value = MetaField.NAME, search = true),
                @Indexed.Field(value = MetaField.DESCRIPTION, search = true),
                @Indexed.Field(MetaField.TYPE),
                @Indexed.Field(MetaField.FLAGS)
        }
)
@Hashed(ignore = {MetaField.PRIMARY_KEY, MetaField.HISTORY_ID})
public final class MetaField extends DocumentTemplate implements Named, Descriptioned, ManyToOne<Dictionary>, OneToMany<Field> {

    private static final long serialVersionUID = 6312797153253711953L;

    /** Название МЕТА-поля по умолчанию */
    public static final String DEFAULT_NAME = "CODE";
    /** Позиция МЕТА-поля по умолчанию */
    public static final int DEFAULT_ORDINAL = 0;
    /*
     * Название полей с идентификаторами
     */
    public static final String DICTIONARY_ID = "dictionaryId";
    public static final String ORDINAL = "ordinal";
    public static final String TYPE = "type";
    public static final String FLAGS = "flags";

    /*
     * Доступные типы флагов
     */
    public static final int FLAG_UNIQUE = 0b001;
    public static final String FLAG_UNIQUE_NAME = "unique";
    public static final int FLAG_PRIMARY = 0b011;
    public static final String FLAG_PRIMARY_NAME = "primary";
    public static final int FLAG_HIDDEN = 0b100;
    public static final String FLAG_HIDDEN_NAME = "hidden";

    /** Идентификатор справочника, к которому относится это поле */
    @NotNull
    @Size(max = PRIMARY_KEY_MAX_SIZE)
    private String dictionaryId;
    /** Справочник, к которому относится поле */
    private transient Dictionary relative;
    /** Уникальное название поля внутри справочника */
    @NotNull
    @Size(min = NAME_MIN_SIZE, max = NAME_MAX_SIZE)
    private String name;
    /** Описание поля */
    @Size(max = DESCRIPTION_SIZE)
    private String description;
    /** Позиция поля относительно других */
    private Integer ordinal;
    /** Тип поля */
    private MetaFieldType type = MetaFieldType.STRING;
    /** Флаги поля */
    private int flags = 0;

    /** Коллекция значений полей */
    private Collection<Field> descendants;

    @XmlTransient
    @Override
    public String getRelativeId() {
        return getDictionaryId();
    }

    @XmlTransient
    @Override
    public Dictionary getRelative() {
        return relative;
    }

    @Override
    public void setRelative(Dictionary relative) {
        this.relative = relative;
        setDictionaryId(this.relative != null ? this.relative.getId() : null);
    }

    @XmlTransient
    public Integer getOrdinal() {
        return ordinal;
    }

    @XmlTransient
    public int getFlags() {
        return flags;
    }

    public boolean isFlagEstablished(int targetFlag) {
        return (flags & targetFlag) == targetFlag;
    }

    public void establishFlags(int... targetFlags) {
        int currentFlags = getFlags();
        for (int f : targetFlags) {
            currentFlags = currentFlags | f;
        }
        setFlags(currentFlags);
    }

    public void resetFlags(int... targetFlags) {
        int currentFlags = getFlags();
        for (int f : targetFlags) {
            currentFlags = currentFlags & ~f;
        }
        setFlags(currentFlags);
    }

    public void changeFlag(boolean value, int flag) {
        if (value) {
            establishFlags(flag);
        } else {
            resetFlags(flag);
        }
    }

    @XmlTransient
    @Override
    public Collection<Field> getDescendants() {
        return descendants;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (dictionaryId != null ? dictionaryId.hashCode() : 0);
        result = 31 * result + lowerCaseHashCode(name);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + flags;
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

        MetaField that = (MetaField) o;

        if (dictionaryId != null ? !dictionaryId.equals(that.dictionaryId) : that.dictionaryId != null) {
            return false;
        }
        if (!lowerCaseEquals(name, that.name)) {
            return false;
        }
        if (description != null ? !description.equalsIgnoreCase(that.description) : that.description != null) {
            return false;
        }
        if (type != that.type) {
            return false;
        }
        if (flags != that.flags) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append(DICTIONARY_ID, getDictionaryId())
                .append(NAME, getName())
                .append(ORDINAL, getOrdinal())
                .append(FLAGS, getFlags())
                .toString();
    }
}
