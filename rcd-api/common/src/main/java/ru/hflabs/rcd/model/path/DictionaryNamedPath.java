package ru.hflabs.rcd.model.path;

import lombok.Getter;
import lombok.Setter;
import ru.hflabs.rcd.model.Copyable;
import ru.hflabs.rcd.model.Named;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;

import static ru.hflabs.util.core.EqualsUtil.lowerCaseEquals;
import static ru.hflabs.util.core.EqualsUtil.lowerCaseHashCode;

/**
 * Класс <class>DictionaryNamedPath</class> содержит информацию, уникально идентифицирующую справочник по названиям
 *
 * @see ru.hflabs.rcd.model.document.Group
 * @see ru.hflabs.rcd.model.document.Dictionary
 * @see Named
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.PROPERTY)
public class DictionaryNamedPath implements Serializable, Copyable {

    private static final long serialVersionUID = 4446882655037748495L;

    /*
     * Название полей с идентификаторами
     */
    public static final String GROUP_NAME = "groupName";
    public static final String DICTIONARY_NAME = "dictionaryName";

    /** Название группы справочников */
    @NotNull
    @Size(min = Named.NAME_MIN_SIZE, max = Named.NAME_MAX_SIZE)
    private String groupName;
    /** Название справочника */
    @NotNull
    @Size(min = Named.NAME_MIN_SIZE, max = Named.NAME_MAX_SIZE)
    private String dictionaryName;

    public DictionaryNamedPath() {
        // default constructor
    }

    public DictionaryNamedPath(String groupName, String dictionaryName) {
        setGroupName(groupName);
        setDictionaryName(dictionaryName);
    }

    @Override
    public int hashCode() {
        int result = lowerCaseHashCode(groupName);
        result = 31 * result + lowerCaseHashCode(dictionaryName);
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

        DictionaryNamedPath that = (DictionaryNamedPath) o;

        return lowerCaseEquals(groupName, that.groupName) && lowerCaseEquals(dictionaryName, that.dictionaryName);
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
    public String toString() {
        return String.format("%s.%s", groupName, dictionaryName);
    }
}
