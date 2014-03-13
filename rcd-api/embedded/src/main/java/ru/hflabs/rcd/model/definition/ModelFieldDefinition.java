package ru.hflabs.rcd.model.definition;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

/**
 * Класс <class>ModelFieldDefinition</class> описывает поле модели данных
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ModelFieldDefinition implements Serializable {

    private static final long serialVersionUID = 7597564802539149033L;

    /** Перечисляет типы полей */
    public static enum FieldType {
        STRING,
        NUMBER,
        BOOLEAN,
        DATE
    }

    /** Тип поля */
    private FieldType type;
    /** Минимальная длинна поля (NULL - если нет ограничений) */
    private Long minLength;
    /** Максимальная длинна поля (NULL - если нет ограничений) */
    private Long maxLength;
    /** Признак, указывающий, что поле обязательно для заполнения */
    private boolean required;
    /** Регулярное выражение проверки значени поля (NULL - если нет ограничений) */
    private String pattern;
    /** Признак, указывающий, что по полю может быть сортировка */
    private boolean sortable;

    public ModelFieldDefinition() {
        this.type = FieldType.STRING;
        this.minLength = null;
        this.maxLength = null;
        this.required = false;
        this.pattern = null;
        this.sortable = false;
    }
}
