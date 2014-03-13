package ru.hflabs.rcd.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.validation.constraints.Size;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * Класс <class>EssenceTemplate</class> реализует базовый класс для сущности
 *
 * @see Essence
 */
@Getter
@Setter
public abstract class EssenceTemplate implements Essence {

    private static final long serialVersionUID = -1774077907476160785L;

    /** Уникальный идентификатор */
    @Size(max = PRIMARY_KEY_MAX_SIZE)
    private String id;

    @Override
    public void injectId(String targetId) {
        setId(targetId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E copy() {
        try {
            return (E) clone();
        } catch (CloneNotSupportedException ex) {
            throw new UndeclaredThrowableException(ex);
        }
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EssenceTemplate that = (EssenceTemplate) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append(PRIMARY_KEY, getId())
                .toString();
    }
}
