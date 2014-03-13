package ru.hflabs.rcd.model.notification;

import org.springframework.util.comparator.NullSafeComparator;
import ru.hflabs.rcd.model.Comparators;

import java.util.Comparator;

/**
 * Класс <class>NotificationComparators</class> реализует агрегацию сервисов сравнения событий оповещения
 *
 * @see Comparators
 */
public abstract class NotificationComparators extends Comparators {

    /** Сервис сравнения по типу события */
    public static final Comparator<Notification> BY_TYPE = new Comparator<Notification>() {
        @Override
        @SuppressWarnings("unchecked")
        public int compare(Notification o1, Notification o2) {
            return NullSafeComparator.NULLS_LOW.compare(o1.getType(), o2.getType());
        }
    };

    /** Сервис сравнения по группе источника */
    public static final Comparator<Notification> BY_FROM_GROUP = new Comparator<Notification>() {
        @Override
        @SuppressWarnings("unchecked")
        public int compare(Notification o1, Notification o2) {
            return NullSafeComparator.NULLS_LOW.compare(o1.getFromGroupName(), o2.getFromGroupName());
        }
    };

    /** Сервис сравнения по справочнику источника */
    public static final Comparator<Notification> BY_FROM_DICTIONARY = new Comparator<Notification>() {
        @Override
        @SuppressWarnings("unchecked")
        public int compare(Notification o1, Notification o2) {
            return NullSafeComparator.NULLS_LOW.compare(o1.getFromDictionaryName(), o2.getFromDictionaryName());
        }
    };

    /** Сервис сравнения по группе назначения */
    public static final Comparator<Notification> BY_TO_GROUP = new Comparator<Notification>() {
        @Override
        @SuppressWarnings("unchecked")
        public int compare(Notification o1, Notification o2) {
            return NullSafeComparator.NULLS_LOW.compare(o1.getToGroupName(), o2.getToGroupName());
        }
    };

    /** Сервис сравнения по справочнику назначения */
    public static final Comparator<Notification> BY_TO_DICTIONARY = new Comparator<Notification>() {
        @Override
        @SuppressWarnings("unchecked")
        public int compare(Notification o1, Notification o2) {
            return NullSafeComparator.NULLS_LOW.compare(o1.getToDictionaryName(), o2.getToDictionaryName());
        }
    };

    protected NotificationComparators() {
        // embedded constructor
    }
}
