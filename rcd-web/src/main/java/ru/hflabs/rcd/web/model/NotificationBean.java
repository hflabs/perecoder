package ru.hflabs.rcd.web.model;

import lombok.AccessLevel;
import lombok.Getter;
import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.Named;
import ru.hflabs.rcd.model.notification.Notification;
import ru.hflabs.rcd.model.notification.NotifyState;
import ru.hflabs.rcd.model.notification.NotifyType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;
import java.util.Date;

import static ru.hflabs.rcd.model.ModelUtils.ID_FUNCTION;
import static ru.hflabs.rcd.model.ModelUtils.NAME_FUNCTION;

/**
 * Класс <class>NotificationBean</class> реализует декоратор оповещения
 *
 * @see Notification
 */
@Getter
@XmlAccessorType(XmlAccessType.PROPERTY)
public class NotificationBean implements Serializable {

    private static final long serialVersionUID = -4569464947477963076L;

    /** Оповещение */
    @Getter(AccessLevel.NONE)
    private Notification delegate;
    /** Набор правил перекодирования */
    private Reference rrs;
    /** Группа источника */
    private Reference fromGroup;
    /** Справочник источник */
    private Reference fromDictionary;
    /** Группа назначения */
    private Reference toGroup;
    /** Справочник назначения */
    private Reference toDictionary;

    public NotificationBean(Notification delegate, Reference rrs, Reference fromGroup, Reference fromDictionary, Reference toGroup, Reference toDictionary) {
        this.delegate = delegate;
        this.rrs = rrs;
        this.fromGroup = fromGroup;
        this.fromDictionary = fromDictionary;
        this.toGroup = toGroup;
        this.toDictionary = toDictionary;
    }

    public String getId() {
        return delegate.getId();
    }

    public String getHistoryId() {
        return delegate.getHistoryId();
    }

    public Date getStartDate() {
        return delegate.getStartDate();
    }

    public Date getEndDate() {
        return delegate.getEndDate();
    }

    public NotifyType getType() {
        return delegate.getType();
    }

    public Integer getCount() {
        return delegate.getCount();
    }

    public Date getProcessingDate() {
        return delegate.getProcessingDate();
    }

    public NotifyState getProcessingState() {
        return delegate.getProcessingState();
    }

    public String getProcessingAuthor() {
        return delegate.getProcessingAuthor();
    }

    public String getFromValue() {
        return delegate.getFromValue();
    }

    /**
     * Класс <class>Reference</class> реализует ссылку на именованную сущность
     *
     * @see Identifying
     * @see Named
     */
    @XmlAccessorType(XmlAccessType.PROPERTY)
    public static class Reference<T extends Identifying & Named> implements Serializable {

        private static final long serialVersionUID = 2683825505116670716L;

        /** Существующая сущность */
        private final T essence;
        /** Название сущности */
        private final String name;

        public Reference(T essence, String name) {
            this.name = name;
            this.essence = essence;
        }

        public String getId() {
            return ID_FUNCTION.apply(essence);
        }

        public String getName() {
            return (essence != null) ? NAME_FUNCTION.apply(essence) : name;
        }
    }
}
