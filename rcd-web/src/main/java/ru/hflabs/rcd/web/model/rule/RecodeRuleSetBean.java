package ru.hflabs.rcd.web.model.rule;

import lombok.Getter;
import lombok.Setter;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;

import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

import static ru.hflabs.rcd.model.Identifying.PRIMARY_KEY_MAX_SIZE;

/**
 * Класс <class>RecodeRuleSetBean</class> реализует базовый декоратор для набора правил перекодирования
 *
 * @see RecodeRuleSet
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.PROPERTY)
public abstract class RecodeRuleSetBean implements Serializable {

    private static final long serialVersionUID = 5762654295766459245L;

    /** Набор правил перекодирования */
    private final RecodeRuleSet delegate;
    /** Идентификатор записи перекодирования по умолчанию */
    @Size(max = PRIMARY_KEY_MAX_SIZE)
    private String defaultRecordId;

    public RecodeRuleSetBean(RecodeRuleSet delegate, String defaultRecordId) {
        this.delegate = delegate;
        this.defaultRecordId = defaultRecordId;
    }

    @XmlTransient
    public RecodeRuleSet getDelegate() {
        return delegate;
    }

    public String getId() {
        return delegate.getId();
    }

    public void setId(String id) {
        delegate.setId(id);
    }

    public String getName() {
        return delegate.getName();
    }

    public void setName(String name) {
        delegate.setName(name);
    }

    public String getFromMetaFieldId() {
        return delegate.getFromFieldId();
    }

    public void setFromMetaFieldId(String fromMetaFieldId) {
        delegate.setFromFieldId(fromMetaFieldId);
    }

    public String getToMetaFieldId() {
        return delegate.getToFieldId();
    }

    public void setToMetaFieldId(String toMetaFieldId) {
        delegate.setToFieldId(toMetaFieldId);
    }
}
