package ru.hflabs.rcd.model.document;

import java.io.Serializable;

/**
 * Класс <class>DocumentContext</class> содержит информацию о контексте документа
 *
 * @see Group
 * @see Dictionary
 * @see MetaField
 * @see Field
 */
public final class DocumentContext implements Serializable {

    private static final long serialVersionUID = -5266444935902652687L;

    /** Группа справочников */
    public final Group group;
    /** Справочник */
    public final Dictionary dictionary;
    /** МЕТА-информация поля справочника */
    public final MetaField metaField;
    /** Значение поля справочника */
    public final Field field;

    public DocumentContext(Group group, Dictionary dictionary, MetaField metaField, Field field) {
        this.group = group;
        this.dictionary = dictionary;
        this.metaField = metaField;
        this.field = field;
    }

    @Override
    public int hashCode() {
        int result = group.hashCode();
        result = 31 * result + dictionary.hashCode();
        result = 31 * result + metaField.hashCode();
        result = 31 * result + field.hashCode();
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

        DocumentContext that = (DocumentContext) o;

        if (!group.equals(that.group)) {
            return false;
        }
        if (!dictionary.equals(that.dictionary)) {
            return false;
        }
        if (!metaField.equals(that.metaField)) {
            return false;
        }
        if (!field.equals(that.field)) {
            return false;
        }

        return true;
    }
}
