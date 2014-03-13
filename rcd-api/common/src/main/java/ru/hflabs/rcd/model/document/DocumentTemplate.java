package ru.hflabs.rcd.model.document;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import ru.hflabs.rcd.model.EssenceTemplate;
import ru.hflabs.rcd.model.Historical;
import ru.hflabs.rcd.model.change.ChangeType;
import ru.hflabs.rcd.model.change.History;

import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Date;

/**
 * Класс <class>DocumentTemplate</class> реализует базовый класс документа
 *
 * @see ru.hflabs.rcd.model.Essence
 * @see Historical
 */
public abstract class DocumentTemplate extends EssenceTemplate implements Historical {

    private static final long serialVersionUID = -5977901973470067744L;

    /** Идентификатор события истории */
    @Getter
    @Setter
    @Size(max = PRIMARY_KEY_MAX_SIZE)
    private String historyId;
    /** Событие истории */
    private transient History history;

    @XmlTransient
    @Override
    public String getHistoryName() {
        return getClass().getSimpleName().toUpperCase();
    }

    @XmlTransient
    @Override
    public ChangeType getChangeType() {
        return history != null ? history.getEventType() : null;
    }

    @XmlTransient
    @Override
    public Date getChangeDate() {
        return history != null ? history.getEventDate() : null;
    }

    @XmlTransient
    @Override
    public History getHistory() {
        return history;
    }

    @Override
    public void setHistory(History history) {
        this.history = history;
        setHistoryId(history != null ? history.getId() : null);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append(HISTORY_ID, getHistoryId())
                .toString();
    }
}
