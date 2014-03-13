package ru.hflabs.rcd.model.document;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import ru.hflabs.rcd.model.*;
import ru.hflabs.rcd.model.annotation.Hashed;
import ru.hflabs.rcd.model.annotation.Indexed;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Collection;

import static ru.hflabs.util.core.EqualsUtil.lowerCaseEquals;
import static ru.hflabs.util.core.EqualsUtil.lowerCaseHashCode;

/**
 * Класс <class>Dictionary</class> описывает справочник
 *
 * @see Group
 */
@Getter
@Setter
@Indexed(
        id = Dictionary.PRIMARY_KEY,
        fields = {
                @Indexed.Field(Dictionary.HISTORY_ID),
                @Indexed.Field(Dictionary.CHANGE_TYPE),
                @Indexed.Field(Dictionary.CHANGE_DATE),
                @Indexed.Field(value = Dictionary.GROUP_ID, sort = false),
                @Indexed.Field(value = Dictionary.NAME, search = true),
                @Indexed.Field(value = Dictionary.DESCRIPTION, search = true),
                @Indexed.Field(Dictionary.VERSION)
        }
)
@Hashed(ignore = {Dictionary.PRIMARY_KEY, Dictionary.HISTORY_ID})
public final class Dictionary extends DocumentTemplate implements Named, Descriptioned, Versioned, ManyToOne<Group>, OneToMany<MetaField> {

    private static final long serialVersionUID = 7248988833891492563L;

    /*
     * Название полей с идентификаторами
     */
    public static final String GROUP_ID = "groupId";
    public static final String CODE = "code";

    /** Идентификатор группы, к которой относится справочник */
    @NotNull
    @Size(max = PRIMARY_KEY_MAX_SIZE)
    private String groupId;
    /** Группа, к которой относится справочник */
    private transient Group relative;
    /** Единый код справочника */
    @Size(min = NAME_MIN_SIZE, max = NAME_MAX_SIZE)
    private String code;
    /** Уникальное название справочника внутри группы */
    @NotNull
    @Size(min = NAME_MIN_SIZE, max = NAME_MAX_SIZE)
    private String name;
    /** Описание справочника */
    @Size(max = DESCRIPTION_SIZE)
    private String description;
    /** Версия справочника */
    @Size(max = VERSION_SIZE)
    private String version;

    /** МЕТА-информация справочника */
    private Collection<MetaField> descendants;
    /** Записи справочника */
    private Collection<Record> records;

    @XmlTransient
    @Override
    public String getRelativeId() {
        return getGroupId();
    }

    @XmlTransient
    @Override
    public Group getRelative() {
        return relative;
    }

    @Override
    public void setRelative(Group relative) {
        this.relative = relative;
        setGroupId(relative != null ? relative.getId() : null);
    }

    @XmlTransient
    @Override
    public Collection<MetaField> getDescendants() {
        return descendants;
    }

    @XmlTransient
    public Collection<Record> getRecords() {
        return records;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (groupId != null ? groupId.hashCode() : 0);
        result = 31 * result + lowerCaseHashCode(name);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
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

        Dictionary that = (Dictionary) o;

        if (groupId != null ? !groupId.equals(that.groupId) : that.groupId != null) {
            return false;
        }
        if (!lowerCaseEquals(name, that.name)) {
            return false;
        }
        if (description != null ? !description.equals(that.description) : that.description != null) {
            return false;
        }
        if (version != null ? !version.equals(that.version) : that.version != null) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append(GROUP_ID, getGroupId())
                .append(NAME, getName())
                .append(CODE, getCode())
                .append(VERSION, getVersion())
                .toString();
    }
}
