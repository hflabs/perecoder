package ru.hflabs.rcd.web.model.document;

import com.google.common.base.Function;
import ru.hflabs.rcd.model.Permissioned;
import ru.hflabs.rcd.model.document.Dictionary;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

import static ru.hflabs.rcd.accessor.Accessors.GROUP_TO_DICTIONARY_INJECTOR;
import static ru.hflabs.rcd.model.ModelUtils.hasPermission;

/**
 * Класс <class>DictionaryBean</class> реализует декоратор справочника
 *
 * @see Dictionary
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
public class DictionaryBean implements Serializable {

    private static final long serialVersionUID = 6451127455822062905L;

    /** Функция создания декоратора */
    public static final Function<Dictionary, DictionaryBean> CONVERT = new Function<Dictionary, DictionaryBean>() {
        @Override
        public DictionaryBean apply(Dictionary input) {
            return new DictionaryBean(input);
        }
    };

    /** Справочник */
    private Dictionary delegate;

    public DictionaryBean() {
        this(new Dictionary());
    }

    public DictionaryBean(Dictionary delegate) {
        this.delegate = delegate;
    }

    @XmlTransient
    public Dictionary getDelegate() {
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

    public String getGroupId() {
        return delegate.getGroupId();
    }

    public void setGroupId(String groupId) {
        delegate.setGroupId(groupId);
    }

    public String getName() {
        return delegate.getName();
    }

    public void setName(String name) {
        delegate.setName(name);
    }

    public String getCode() {
        return delegate.getCode();
    }

    public void setCode(String code) {
        delegate.setCode(code);
    }

    public String getDescription() {
        return delegate.getDescription();
    }

    public void setDescription(String description) {
        delegate.setDescription(description);
    }

    public String getVersion() {
        return delegate.getVersion();
    }

    public void setVersion(String version) {
        delegate.setVersion(version);
    }

    public boolean isWritable() {
        return hasPermission(GROUP_TO_DICTIONARY_INJECTOR.apply(delegate), Permissioned.PERMISSION_WRITE);
    }
}
