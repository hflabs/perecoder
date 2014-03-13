package ru.hflabs.rcd.model.document;

import lombok.Getter;
import lombok.Setter;
import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.ManyToOne;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Map;

/**
 * Класс <class>Record</class> содержит информацию о записи справочника
 *
 * @see Field
 * @see MetaField
 */
@Getter
@Setter
public final class Record implements Identifying, ManyToOne<Dictionary> {

    private static final long serialVersionUID = -4362681013535670995L;

    /*
     * Название полей с идентификаторами
     */
    public static final String DICTIONARY_ID = "dictionaryId";

    /** Идентификатор записи */
    private String id;
    /** Идентификатор справочника, к которому относится запись */
    @NotNull
    @Size(max = PRIMARY_KEY_MAX_SIZE)
    private String dictionaryId;
    /** Справочник, к которому относится запись */
    private transient Dictionary relative;
    /** Коллекция значений полей записи */
    @NotNull
    private Map<String, Field> fields;

    public Record() {
        this(null);
    }

    public Record(String id) {
        this.id = id;
    }

    @Override
    public void injectId(String targetId) {
        setId(targetId);
    }

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

    public Record injectDictionary(Dictionary relativeDictionary) {
        setRelative(relativeDictionary);
        return this;
    }

    public Record injectFields(Map<String, Field> relativeFields) {
        setFields(relativeFields);
        return this;
    }

    public Field retrieveFieldByName(String name) {
        return fields != null ?
                fields.get(name) :
                null;
    }
}
