package ru.hflabs.rcd.model.document;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import ru.hflabs.rcd.model.Descriptioned;
import ru.hflabs.rcd.model.Named;
import ru.hflabs.rcd.model.OneToMany;
import ru.hflabs.rcd.model.Permissioned;
import ru.hflabs.rcd.model.annotation.Hashed;
import ru.hflabs.rcd.model.annotation.Indexed;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Collection;

import static ru.hflabs.util.core.EqualsUtil.lowerCaseEquals;
import static ru.hflabs.util.core.EqualsUtil.lowerCaseHashCode;

/**
 * Класс <class>Group</class> описывает группу справочников
 *
 * @see Named
 * @see Descriptioned
 * @see ru.hflabs.rcd.model.Identifying
 */
@Getter
@Setter
@Indexed(
        id = Group.PRIMARY_KEY,
        fields = {
                @Indexed.Field(Group.HISTORY_ID),
                @Indexed.Field(Group.CHANGE_TYPE),
                @Indexed.Field(Group.CHANGE_DATE),
                @Indexed.Field(value = Group.NAME, search = true),
                @Indexed.Field(value = Group.DESCRIPTION, search = true),
                @Indexed.Field(Group.OWNER)
        }
)
@Hashed(ignore = {Group.PRIMARY_KEY, Group.HISTORY_ID})
public final class Group extends DocumentTemplate implements Named, Descriptioned, Permissioned, OneToMany<Dictionary> {

    private static final long serialVersionUID = -289028798163794162L;

    /*
     * Название полей с идентификаторами
     */
    public static final String OWNER = "owner";
    public static final String PERMISSIONS = "permissions";

    /** Владелец группы */
    @Size(max = NAME_MAX_SIZE)
    private String owner;
    /** Уникальное название группы */
    @NotNull
    @Size(min = NAME_MIN_SIZE, max = NAME_MAX_SIZE)
    private String name;
    /** Описание группы */
    @Size(max = DESCRIPTION_SIZE)
    private String description;
    /** Права безопасности */
    private int permissions = Permissioned.PERMISSION_ALL;

    /** Коллекция справоников группы */
    private Collection<Dictionary> descendants;

    @XmlTransient
    @Override
    public Collection<Dictionary> getDescendants() {
        return descendants;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + lowerCaseHashCode(name);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
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

        Group that = (Group) o;

        if (!lowerCaseEquals(name, that.name)) {
            return false;
        }
        if (description != null ? !description.equals(that.description) : that.description != null) {
            return false;
        }
        if (owner != null ? !owner.equals(that.owner) : that.owner != null) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append(OWNER, getOwner())
                .append(NAME, getName())
                .append(PERMISSIONS, getPermissions())
                .toString();
    }
}
