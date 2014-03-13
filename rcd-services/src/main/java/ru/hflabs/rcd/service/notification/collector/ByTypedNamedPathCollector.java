package ru.hflabs.rcd.service.notification.collector;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hflabs.rcd.model.notification.Notification;
import ru.hflabs.rcd.model.notification.NotifyType;
import ru.hflabs.rcd.model.path.DictionaryNamedPath;

import java.io.Serializable;
import java.util.Collection;

/**
 * Класс <class>ByNamedPathCollector</class> реализует коллектор оповещений с группировкой именованным путям
 *
 * @author Nazin Alexander
 */
public class ByTypedNamedPathCollector implements NotificationCollector, RemovalListener<ByTypedNamedPathCollector.TypedDirectionNamedPath, Notification> {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    /** Коллекция оповещений */
    private Cache<ByTypedNamedPathCollector.TypedDirectionNamedPath, Notification> notifications;
    /** Максимальное количество сгруппированных оповещений */
    private int queueSize;

    public ByTypedNamedPathCollector() {
        this.queueSize = 10000;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    /**
     * Выполняет инициализацию кеша
     */
    public void createCollection() throws Exception {
        notifications = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .maximumSize(queueSize)
                .removalListener(this)
                .build();
    }

    @Override
    public void onRemoval(RemovalNotification<TypedDirectionNamedPath, Notification> event) {
        switch (event.getCause()) {
            case SIZE: {
                LOG.warn(String.format("Evicted notification '%s' with %d events", event.getKey(), event.getValue().getCount()));
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    public boolean appendNotification(Notification notification) throws Exception {
        // Формируем ключ кеша
        final TypedDirectionNamedPath cacheKey = new TypedDirectionNamedPath(
                notification.getType(),
                notification.getRuleSetName(),
                new DictionaryNamedPath(notification.getFromGroupName(), notification.getFromDictionaryName()),
                new DictionaryNamedPath(notification.getToGroupName(), notification.getToDictionaryName()),
                notification.getFromValue()
        );
        // Добавляем событие в оповещение
        Notification existed = notifications.getIfPresent(cacheKey);
        if (existed != null) {
            existed.setCount(existed.getCount() + 1);
        } else {
            notifications.put(cacheKey, notification);
        }
        return true;
    }

    @Override
    public Collection<Notification> retrieveNotifications() {
        try {
            return Lists.newArrayList(notifications.asMap().values());
        } finally {
            notifications.invalidateAll();
            notifications.cleanUp();
        }
    }

    /**
     * Класс <class>TypedDirectionNamedPath</class> реализует ключ кеша оповещений
     *
     * @author Nazin Alexander
     */
    public static class TypedDirectionNamedPath implements Serializable {

        private static final long serialVersionUID = -2433623455531702209L;

        /** Тип оповещения */
        private final NotifyType notifyType;
        /** Название набора правил перекодирования */
        private final String ruleSetName;
        /** Именованный путь справочника источника */
        private final DictionaryNamedPath fromDictionaryNamedPath;
        /** Именованный путь справочника назначения */
        private final DictionaryNamedPath toDictionaryNamedPath;
        /** Значение поля источника */
        private final String fromValue;

        private TypedDirectionNamedPath(NotifyType notifyType, String ruleSetName, DictionaryNamedPath from, DictionaryNamedPath to, String value) {
            this.notifyType = notifyType;
            this.ruleSetName = ruleSetName;
            this.fromDictionaryNamedPath = from;
            this.toDictionaryNamedPath = to;
            this.fromValue = value;
        }

        @Override
        public int hashCode() {
            int result = notifyType.hashCode();
            result = 31 * result + (ruleSetName != null ? ruleSetName.hashCode() : 0);
            result = 31 * result + fromDictionaryNamedPath.hashCode();
            result = 31 * result + toDictionaryNamedPath.hashCode();
            result = 31 * result + (fromValue != null ? fromValue.hashCode() : 0);
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            TypedDirectionNamedPath that = (TypedDirectionNamedPath) o;

            if (notifyType != that.notifyType) {
                return false;
            }
            if (ruleSetName != null ? !ruleSetName.equals(that.ruleSetName) : that.ruleSetName != null) {
                return false;
            }
            if (!fromDictionaryNamedPath.equals(that.fromDictionaryNamedPath)) {
                return false;
            }
            if (!toDictionaryNamedPath.equals(that.toDictionaryNamedPath)) {
                return false;
            }
            if (fromValue != null ? !fromValue.equals(that.fromValue) : that.fromValue != null) {
                return false;
            }

            return true;
        }

        @Override
        public String toString() {
            return String.format("%s: %s[%s] TO %s", notifyType, fromDictionaryNamedPath.toString(), fromValue, toDictionaryNamedPath.toString());
        }
    }
}
