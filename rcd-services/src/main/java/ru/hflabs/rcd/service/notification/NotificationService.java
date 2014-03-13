package ru.hflabs.rcd.service.notification;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.hflabs.rcd.model.notification.Notification;
import ru.hflabs.rcd.model.notification.NotifyState;
import ru.hflabs.rcd.service.INotificationService;
import ru.hflabs.rcd.service.document.DocumentServiceTemplate;
import ru.hflabs.util.security.SecurityUtil;
import ru.hflabs.util.spring.transaction.support.TransactionUtil;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import static ru.hflabs.rcd.accessor.Accessors.shallowClone;

/**
 * Класс <class>NotificationService</class> реализует сервис работы с оповещениями
 *
 * @author Nazin Alexander
 */
public class NotificationService extends DocumentServiceTemplate<Notification> implements INotificationService {

    public NotificationService() {
        super(Notification.class);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public Collection<Notification> changeNotifyState(Set<String> ids, final NotifyState notifyState) {
        // Получаем оповещения по их идентификаторам и отсекаем те, у которых уже установлен новый статус
        Collection<Notification> notifications = Collections2.filter(findByIDs(ids, false, true), new Predicate<Notification>() {
            @Override
            public boolean apply(Notification input) {
                return !notifyState.equals(input.getProcessingState());
            }
        });

        // Устанавливаем измененные поля
        ImmutableList.Builder<Notification> toUpdate = ImmutableList.builder();
        final Date processingDate = TransactionUtil.getTransactionStartDate();
        final String processingAuthor = SecurityUtil.getCurrentUserName();
        for (Notification existed : notifications) {
            Notification changed = shallowClone(existed);
            changed.setProcessingState(notifyState);
            changed.setProcessingDate(processingDate);
            changed.setProcessingAuthor(processingAuthor);

            toUpdate.add(changed);
        }

        // Выполняем обновление
        return update(toUpdate.build(), notifications, false);
    }
}
