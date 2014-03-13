package ru.hflabs.rcd.model.rule;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import ru.hflabs.rcd.model.Essence;
import ru.hflabs.rcd.model.document.DocumentTemplate;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;

import javax.validation.constraints.Size;

/**
 * Класс <class>Rule</class> реализует базовый класс, содержащий информацию о именнованных путях источника и назначения правила
 *
 * @see MetaFieldNamedPath
 */
@Getter
@Setter
public abstract class Rule<NP extends MetaFieldNamedPath, T extends Essence, R extends Rule<NP, T, R>> extends DocumentTemplate {

    private static final long serialVersionUID = 541728175712087915L;

    /*
     * Название полей с идентификаторами
     */
    public static final String FROM_PREFIX = "from";
    public static final String TO_PREFIX = "to";
    public static final String FIELD_ID = "FieldId";
    public static final String FIELD_NAME = "FieldName";
    public static final String NAMED_PATH = "NamedPath";

    public static final String FROM_PATH = FROM_PREFIX + NAMED_PATH;
    public static final String FROM_FIELD_ID = FROM_PREFIX + FIELD_ID;

    public static final String TO_PATH = TO_PREFIX + NAMED_PATH;
    public static final String TO_FIELD_ID = TO_PREFIX + FIELD_ID;

    /** Именованный путь, идентифицирующий исходную запись */
    private NP fromNamedPath;
    /** Идентификатор поля исходной записи */
    @Size(max = PRIMARY_KEY_MAX_SIZE)
    private String fromFieldId;
    /** Значение исходной записи */
    private transient T from;

    /** Именованный путь, идентифицирующий целевую запись */
    private NP toNamedPath;
    /** Идентификатор поля целевой записи */
    @Size(max = PRIMARY_KEY_MAX_SIZE)
    private String toFieldId;
    /** Значение целевой записи */
    private transient T to;

    @SuppressWarnings("unchecked")
    public R injectFromNamedPath(NP namedPath) {
        setFromNamedPath(namedPath);
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R injectFrom(T relativeFrom) {
        setFrom(relativeFrom);
        setFromFieldId(relativeFrom != null ? relativeFrom.getId() : null);
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R injectToNamedPath(NP namedPath) {
        setToNamedPath(namedPath);
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    public R injectTo(T relativeTo) {
        setTo(relativeTo);
        setToFieldId(relativeTo != null ? relativeTo.getId() : null);
        return (R) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E copy() {
        Rule<NP, T, R> cloned = super.copy();
        cloned.setFromNamedPath(fromNamedPath != null ? (NP) fromNamedPath.copy() : null);
        cloned.setFrom(from != null ? (T) from.copy() : null);
        cloned.setToNamedPath(toNamedPath != null ? (NP) toNamedPath.copy() : null);
        cloned.setTo(to != null ? (T) to.copy() : null);
        return (E) cloned;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (fromFieldId != null ? fromFieldId.hashCode() : 0);
        result = 31 * result + (fromNamedPath != null ? fromNamedPath.hashCode() : 0);
        result = 31 * result + (toFieldId != null ? toFieldId.hashCode() : 0);
        result = 31 * result + (toNamedPath != null ? toNamedPath.hashCode() : 0);
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

        Rule rule = (Rule) o;

        if (fromFieldId != null ? !fromFieldId.equals(rule.fromFieldId) : rule.fromFieldId != null) {
            return false;
        }
        if (fromNamedPath != null ? !fromNamedPath.equals(rule.fromNamedPath) : rule.fromNamedPath != null) {
            return false;
        }
        if (toFieldId != null ? !toFieldId.equals(rule.toFieldId) : rule.toFieldId != null) {
            return false;
        }
        if (toNamedPath != null ? !toNamedPath.equals(rule.toNamedPath) : rule.toNamedPath != null) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append(FROM_PREFIX, getFromNamedPath())
                .append(TO_PREFIX, getToNamedPath())
                .toString();
    }
}
