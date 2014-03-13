package ru.hflabs.rcd.service.notification.collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.CollectionUtils;
import ru.hflabs.rcd.Constants;
import ru.hflabs.rcd.model.change.HistoryBuilder;
import ru.hflabs.rcd.model.notification.Notification;
import ru.hflabs.rcd.service.IHistoryService;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static ru.hflabs.rcd.service.ServiceUtils.publishChangeEvent;

/**
 * Класс <class>ByIntervalCollector</class> реализует коллектор оповещений с группировкой по интервалу времени
 *
 * @author Nazin Alexander
 */
public class ByIntervalCollector implements InitializingBean, ApplicationEventPublisherAware, NotificationCollector {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    /** Сервис публикации событий */
    private ApplicationEventPublisher eventPublisher;
    /** Сервис работы с историей документов */
    private IHistoryService historyService;

    /** Блогировка коллектора */
    private final Lock collectLock;
    /** Целевой коллектор */
    private NotificationCollector delegate;
    /** Пул потоков */
    private ScheduledExecutorService executorService;
    /** Интервал агрегирования оповещений */
    private Long interval;

    public ByIntervalCollector() {
        this.collectLock = new ReentrantLock();
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    public void setHistoryService(IHistoryService historyService) {
        this.historyService = historyService;
    }

    public void setDelegate(NotificationCollector delegate) {
        this.delegate = delegate;
    }

    public void setExecutorService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setInterval(Long interval) {
        this.interval = interval;
    }

    @Override
    public boolean appendNotification(Notification notification) throws Exception {
        collectLock.lock();
        try {
            return delegate.appendNotification(notification);
        } finally {
            collectLock.unlock();
        }
    }

    @Override
    public Collection<Notification> retrieveNotifications() {
        collectLock.lock();
        try {
            return delegate.retrieveNotifications();
        } finally {
            collectLock.unlock();
        }
    }

    /**
     * Выполняет агрегацию групп оповещений
     *
     * @param startDate дата начала агрегации
     * @param endDate дата завершения агрегации
     */
    private void dumpNotifications(Date startDate, Date endDate, Collection<Notification> notifications) {
        HistoryBuilder<Notification> changeDescriptor = new HistoryBuilder<Notification>(Notification.class);
        // Выполняем формирование дескриптора создания оповещений
        for (Notification notification : notifications) {
            notification.setStartDate(startDate);
            notification.setEndDate(endDate);
            changeDescriptor.addChange(historyService.createChangeHistory(endDate, Constants.RCD_NAME, null, notification));
        }
        // Публикуем событие создания оповещений
        publishChangeEvent(eventPublisher, this, changeDescriptor);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        executorService.scheduleAtFixedRate(new DumpNotificationsTask(new Date()), interval, interval, TimeUnit.SECONDS);
    }

    /**
     * Класс <class>DumpNotificationsTask</class> реализует задачу переодической агрегации групп оповещений
     *
     * @author Nazin Alexander
     */
    private class DumpNotificationsTask implements Runnable {

        /** Дата запуска задачи */
        private Date date;

        private DumpNotificationsTask(Date date) {
            this.date = date;
        }

        @Override
        public void run() {
            final Date endDate = new Date();
            try {
                Collection<Notification> notifications = retrieveNotifications();
                if (!CollectionUtils.isEmpty(notifications)) {
                    dumpNotifications(date, endDate, notifications);
                }
            } catch (Exception ex) {
                LOG.error(String.format("Can't dump notifications. Cause by: %s", ex.getMessage()), ex);
            } finally {
                date = endDate;
            }
        }
    }
}
