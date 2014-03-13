package ru.hflabs.rcd.web.model.document;

import com.google.common.base.Function;
import ru.hflabs.rcd.model.Permissioned;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.document.MetaFieldType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

import static ru.hflabs.rcd.accessor.Accessors.GROUP_TO_META_FIELD_INJECTOR;
import static ru.hflabs.rcd.model.ModelUtils.hasPermission;

/**
 * Класс <class>MetaFieldBean</class> реализует декоратор МЕТА-поля справочника
 *
 * @see MetaField
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
public class MetaFieldBean implements Serializable {

    private static final long serialVersionUID = -1646979087852208227L;

    /** Функция создания декоратора */
    public static final Function<MetaField, MetaFieldBean> CONVERT = new Function<MetaField, MetaFieldBean>() {

        @Override
        public MetaFieldBean apply(MetaField input) {
            return new MetaFieldBean(input);
        }
    };

    /** МЕТА-поле справочника */
    private MetaField delegate;

    public MetaFieldBean() {
        this(new MetaField());
    }

    public MetaFieldBean(MetaField delegate) {
        this.delegate = delegate;
    }

    @XmlTransient
    public MetaField getDelegate() {
        return delegate;
    }

    public String getId() {
        return delegate.getId();
    }

    public void setId(String id) {
        delegate.setId(id);
    }

    public String getHistoryId() {
        return delegate.getHistoryId();
    }

    public void setHistoryId(String historyId) {
        delegate.setHistoryId(historyId);
    }

    public String getDictionaryId() {
        return delegate.getDictionaryId();
    }

    public void setDictionaryId(String dictionaryId) {
        delegate.setDictionaryId(dictionaryId);
    }

    public String getName() {
        return delegate.getName();
    }

    public void setName(String name) {
        delegate.setName(name);
    }

    public String getDescription() {
        return delegate.getDescription();
    }

    public void setDescription(String description) {
        delegate.setDescription(description);
    }

    public void setType(MetaFieldType type) {
        delegate.setType(type);
    }

    public MetaFieldType getType() {
        return delegate.getType();
    }

    public boolean isPrimary() {
        return delegate.isFlagEstablished(MetaField.FLAG_PRIMARY);
    }

    public void setPrimary(boolean primary) {
        delegate.changeFlag(primary, MetaField.FLAG_PRIMARY);
    }

    public boolean isUnique() {
        return delegate.isFlagEstablished(MetaField.FLAG_UNIQUE);
    }

    public void setUnique(boolean unique) {
        delegate.changeFlag(unique, MetaField.FLAG_UNIQUE);
    }

    public boolean isHidden() {
        return delegate.isFlagEstablished(MetaField.FLAG_HIDDEN);
    }

    public void setHidden(boolean hidden) {
        delegate.changeFlag(hidden, MetaField.FLAG_HIDDEN);
    }

    public boolean isWritable() {
        return hasPermission(GROUP_TO_META_FIELD_INJECTOR.apply(delegate), Permissioned.PERMISSION_WRITE);
    }
}
