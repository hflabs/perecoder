package ru.hflabs.rcd.web.model.document;

import ru.hflabs.rcd.model.document.Group;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

import static ru.hflabs.rcd.model.ModelUtils.hasPermission;

/**
 * Класс <class>GroupBean</class> реализует декоратор группы справочников
 *
 * @see Group
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
public class GroupBean implements Serializable {

    private static final long serialVersionUID = 5286791652798240290L;

    /** Группа справочников */
    private Group delegate;
    /** Статистика группы */
    private DictionaryStatisticBean statistic;

    public GroupBean() {
        this(new Group(), new DictionaryStatisticBean());
    }

    public GroupBean(Group delegate, DictionaryStatisticBean statistic) {
        this.delegate = delegate;
        this.statistic = statistic;
    }

    @XmlTransient
    public Group getDelegate() {
        return delegate;
    }

    public void setDelegate(Group delegate) {
        this.delegate = delegate;
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

    public String getOwner() {
        return delegate.getOwner();
    }

    public void setOwner(String owner) {
        delegate.setOwner(owner);
    }

    public boolean isWritable() {
        return hasPermission(delegate, Group.PERMISSION_WRITE);
    }

    public DictionaryStatisticBean getStatistic() {
        return statistic;
    }

    public void setStatistic(DictionaryStatisticBean statistic) {
        this.statistic = statistic;
    }
}
