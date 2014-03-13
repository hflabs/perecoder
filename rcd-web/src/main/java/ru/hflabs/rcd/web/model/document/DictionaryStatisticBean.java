package ru.hflabs.rcd.web.model.document;

import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

/**
 * Класс <class>DictionaryStatisticBean</class> описывает статистику справочников для группы
 *
 * @see ru.hflabs.rcd.model.document.Group
 * @see ru.hflabs.rcd.model.document.Dictionary
 */
@Getter
@XmlAccessorType(XmlAccessType.PROPERTY)
public class DictionaryStatisticBean implements Serializable {

    private static final long serialVersionUID = 6516051256254649155L;

    /** Общее количество справочников в группе */
    private int totalCount;
    /** Количество несопоставленных справочников в группе */
    private int unmatchedCount;

    public DictionaryStatisticBean() {
        this(0, 0);
    }

    public DictionaryStatisticBean(int totalCount, int unmatchedCount) {
        this.totalCount = totalCount;
        this.unmatchedCount = unmatchedCount;
    }
}
