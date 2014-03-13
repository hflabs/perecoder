package ru.hflabs.rcd.service.notification;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hflabs.rcd.accessor.FieldAccessor;
import ru.hflabs.rcd.event.recode.RecodeFailedEvent;
import ru.hflabs.rcd.model.notification.Notification;
import ru.hflabs.rcd.model.notification.NotifyState;
import ru.hflabs.rcd.model.path.DictionaryNamedPath;
import ru.hflabs.rcd.model.path.FieldNamedPath;
import ru.hflabs.rcd.service.notification.collector.NotificationCollector;
import ru.hflabs.util.spring.context.event.AsyncApplicationListener;

/**
 * Класс <class>RecodeFailedAggregateListener</class> реализует слушателя агрегирования оповещений об ошибочной перекодировке
 *
 * @author Nazin Alexander
 */
public class RecodeFailedAggregateListener implements AsyncApplicationListener<RecodeFailedEvent> {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    /** Сервис установки исходного значения в оповещение */
    private static final FieldAccessor<FieldNamedPath, Notification> FROM_PATH_INJECTOR = new FieldAccessor<FieldNamedPath, Notification>() {

        @Override
        public Notification inject(Notification target, FieldNamedPath value) {
            target.setFromGroupName(value != null ? truncateDocumentName(value.getGroupName()) : null);
            target.setFromDictionaryName(value != null ? truncateDocumentName(value.getDictionaryName()) : null);
            target.setFromValue(value != null ? truncateDocumentName(value.getFieldValue()) : null);
            return target;
        }

        @Override
        public FieldNamedPath apply(Notification target) {
            return new FieldNamedPath(target.getFromGroupName(), target.getFromDictionaryName(), null, target.getFromValue());
        }
    };

    /** Сервис установки целевого значения в оповещение */
    private static final FieldAccessor<DictionaryNamedPath, Notification> TO_PATH_INJECTOR = new FieldAccessor<DictionaryNamedPath, Notification>() {

        @Override
        public Notification inject(Notification target, DictionaryNamedPath value) {
            target.setToGroupName(value != null ? truncateDocumentName(value.getGroupName()) : null);
            target.setToDictionaryName(value != null ? truncateDocumentName(value.getDictionaryName()) : null);
            return target;
        }

        @Override
        public DictionaryNamedPath apply(Notification target) {
            return new DictionaryNamedPath(target.getToGroupName(), target.getToDictionaryName());
        }
    };

    /** Коллектор оповещений */
    private NotificationCollector notificationCollector;

    public void setNotificationCollector(NotificationCollector notificationCollector) {
        this.notificationCollector = notificationCollector;
    }

    /**
     * Выполняет валидацию имени документа и в случае необходимости обрезает его до необходимой длинны
     *
     * @param name валидируемое имя
     * @return Возвращает модифицированное имя документа
     */
    private static String truncateDocumentName(String name) {
        return StringUtils.abbreviate(name, Notification.DOCUMENT_NAME_LENGTH - 3);
    }

    @Override
    public void onApplicationEvent(RecodeFailedEvent event) {
        if (event.registryListener(getClass().getName())) {
            Notification notification = new Notification();
            // Устанавливаем значения по умолчанию
            {
                notification.setCount(1);
                notification.setProcessingState(NotifyState.PENDING);
                notification.setType(event.getNotifyType());
                notification.setRuleSetName(truncateDocumentName(event.getRuleSetName()));
            }
            // Заполняем источник и назначение
            notification = FROM_PATH_INJECTOR.inject(notification, event.getFromPath());
            notification = TO_PATH_INJECTOR.inject(notification, event.getToPath());
            // Добавляем сформированное оповещение в коллектор
            try {
                notificationCollector.appendNotification(notification);
            } catch (Exception ex) {
                LOG.error(String.format("Can't collect notification '%s'. Cause by: %s", notification, ex.getMessage()), ex);
            }
        }
    }
}
