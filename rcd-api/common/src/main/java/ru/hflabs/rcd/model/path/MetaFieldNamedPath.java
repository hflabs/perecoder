package ru.hflabs.rcd.model.path;

import lombok.Getter;
import lombok.Setter;
import ru.hflabs.rcd.model.Named;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static ru.hflabs.util.core.EqualsUtil.lowerCaseEquals;
import static ru.hflabs.util.core.EqualsUtil.lowerCaseHashCode;

/**
 * Класс <class>MetaFieldNamedPath</class> содержит информацию, уникально идентифицирующую МЕТА-поле справочника
 *
 * @see ru.hflabs.rcd.model.document.MetaField
 * @see Named
 */
@Getter
@Setter
public class MetaFieldNamedPath extends DictionaryNamedPath {

    private static final long serialVersionUID = -5827186400778971005L;

    /*
     * Название полей с идентификаторами
     */
    public static final String FIELD_NAME = "fieldName";

    /** Имя поля справочника */
    @NotNull
    @Size(min = Named.NAME_MIN_SIZE, max = Named.NAME_MAX_SIZE)
    private String fieldName;

    public MetaFieldNamedPath() {
        // default constructor
    }

    public MetaFieldNamedPath(DictionaryNamedPath namedPath, String fieldName) {
        this(namedPath.getGroupName(), namedPath.getDictionaryName(), fieldName);
    }

    public MetaFieldNamedPath(String groupName, String dictionaryName, String fieldName) {
        super(groupName, dictionaryName);
        setFieldName(fieldName);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + lowerCaseHashCode(fieldName);
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

        MetaFieldNamedPath that = (MetaFieldNamedPath) o;

        return lowerCaseEquals(fieldName, that.fieldName) && super.equals(o);
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", super.toString(), fieldName);
    }
}
