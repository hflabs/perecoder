package ru.hflabs.rcd.model.path;

import lombok.Getter;
import lombok.Setter;

/**
 * Класс <class>FieldNamedPath</class> содержит информацию, идентифицирующую значение записи справочника
 *
 * @see ru.hflabs.rcd.model.document.Field
 */
@Getter
@Setter
public class FieldNamedPath extends MetaFieldNamedPath {

    private static final long serialVersionUID = -5827186400778971005L;

    /*
     * Название полей с идентификаторами
     */
    public static final String FIELD_VALUE = "fieldValue";

    /** Значение поля справочника */
    private String fieldValue;

    public FieldNamedPath() {
        // default constructor
    }

    public FieldNamedPath(DictionaryNamedPath namedPath, String fieldName, String fieldValue) {
        this(namedPath.getGroupName(), namedPath.getDictionaryName(), fieldName, fieldValue);
    }

    public FieldNamedPath(MetaFieldNamedPath namedPath, String fieldValue) {
        this(namedPath.getGroupName(), namedPath.getDictionaryName(), namedPath.getFieldName(), fieldValue);
    }

    public FieldNamedPath(String groupName, String dictionaryName, String fieldName, String fieldValue) {
        super(groupName, dictionaryName, fieldName);
        setFieldValue(fieldValue);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (fieldValue != null ? fieldValue.hashCode() : 0);
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
        FieldNamedPath that = (FieldNamedPath) o;

        if (fieldValue != null ? !fieldValue.equals(that.fieldValue) : that.fieldValue != null) {
            return false;
        }

        return super.equals(o);
    }

    @Override
    public String toString() {
        return String.format("%s='%s'", super.toString(), fieldValue);
    }
}
