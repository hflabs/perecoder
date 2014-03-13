package ru.hflabs.rcd.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

/**
 * Интерфейс <class>Identifying</class> декларирует методы объекта, который обладает уникальным идентификатором
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
public interface Identifying extends Serializable {

    /** Название поля с первичным ключем */
    String PRIMARY_KEY = "id";
    /** Максимальный размер идентификатора */
    int PRIMARY_KEY_MAX_SIZE = 36;

    /**
     * @return Возвращает уникальный идентификатор
     */
    String getId();

    /**
     * Устанавливает уникальный идентификатор
     *
     * @param id идентификатор
     */
    void setId(String id);

    /**
     * Выполняет установку идентификатора с заполнением дополнительных полей
     *
     * @param targetId идентификатор
     */
    void injectId(String targetId);
}
