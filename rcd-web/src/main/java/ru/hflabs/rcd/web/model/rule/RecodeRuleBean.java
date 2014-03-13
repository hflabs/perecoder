package ru.hflabs.rcd.web.model.rule;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

import static ru.hflabs.rcd.model.Identifying.PRIMARY_KEY_MAX_SIZE;

/**
 * Класс <class>RecodeRuleBean</class> реализует базовый декоратор правила перекодирования
 *
 * @author Nazin Alexander
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.PROPERTY)
public abstract class RecodeRuleBean implements Serializable {

    private static final long serialVersionUID = -1372944368402056292L;

    /** Идентификатор целевой записи */
    @Size(max = PRIMARY_KEY_MAX_SIZE)
    private String toRecordId;

    public RecodeRuleBean(String toRecordId) {
        this.toRecordId = toRecordId;
    }
}
