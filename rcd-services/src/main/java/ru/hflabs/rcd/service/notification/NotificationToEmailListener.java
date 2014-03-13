package ru.hflabs.rcd.service.notification;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.event.modify.ChangeEvent;
import ru.hflabs.rcd.model.ModelUtils;
import ru.hflabs.rcd.model.change.ChangeType;
import ru.hflabs.rcd.model.notification.Notification;
import ru.hflabs.util.core.FormatUtil;
import ru.hflabs.util.core.date.DateInterval;
import ru.hflabs.util.core.date.DateUtil;
import ru.hflabs.util.spring.context.event.AsyncApplicationListener;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.*;

/**
 * Класс <class>NotificationToEmailListener</class> реализует сервис отправки оповещений по электронной почте
 *
 * @author Nazin Alexander
 */
public class NotificationToEmailListener implements InitializingBean, AsyncApplicationListener<ChangeEvent> {

    /** Шаблон текста сообщения */
    private static final String MESSAGE_TEMPLATE_HEADER = "email_notification_header.template";
    private static final String MESSAGE_TEMPLATE_BODY = "email_notification_body.template";

    /*
     * Переменные контекста шаблона
     */
    private static final String VAR_NOTIFICATIONS = "notifications";
    private static final String VAR_NOTIFICATION_INTERVAL = "interval";

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    /** Сервис сравнения оповещений */
    private Comparator<Notification> notificationComparator;
    /** Параметры отправки сообщения */
    private MessageParameters messageParameters;

    /** Провайдер отправки email-ов */
    private JavaMailSender mailSender;
    /** Шаблонизатор текста сообщения */
    private VelocityEngine velocityEngine;
    /** Флаг HTML сообщения */
    private boolean htmlMessage;

    public void setNotificationComparator(Comparator<Notification> notificationComparator) {
        this.notificationComparator = notificationComparator;
    }

    public void setMessageParameters(MessageParameters messageParameters) {
        this.messageParameters = messageParameters;
    }

    public void setHtmlMessage(boolean htmlMessage) {
        this.htmlMessage = htmlMessage;
    }

    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    /**
     * Выполняет форматирование текста
     *
     * @param templateName название шаблона
     * @param context переменные контекста
     * @return Возвращает отформатированный текст
     */
    private String doFormatMessageTemplate(String templateName, Map<String, Object> context) throws Exception {
        return VelocityEngineUtils.mergeTemplateIntoString(
                velocityEngine, templateName, Charsets.UTF_8.name(), context
        );
    }

    /**
     * Выполняет отправку email сообщений
     *
     * @param notifications коллекция оповещений
     */
    private void doSendMessage(final Collection<Notification> notifications) {
        // Формируем интервал событий
        final DateInterval notificationInterval = ModelUtils.createNotificationInterval(notifications);
        // Формируем сообщение
        MimeMessagePreparator preparator = new MimeMessagePreparator() {

            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
                message = messageParameters.injectTo(message);

                Map<String, Object> context = Maps.newHashMap();
                {
                    context.put(DateUtil.class.getSimpleName(), DateUtil.class);
                    context.put(VAR_NOTIFICATIONS, notifications);
                    context.put(VAR_NOTIFICATION_INTERVAL, notificationInterval);
                }

                message.setSubject(doFormatMessageTemplate(MESSAGE_TEMPLATE_HEADER, context));
                message.setText(doFormatMessageTemplate(MESSAGE_TEMPLATE_BODY, context), htmlMessage);
            }
        };
        // Выполняем отправку сообщения
        try {
            mailSender.send(preparator);
        } catch (Throwable th) {
            LOG.error(String.format("Can't send email. Cause by: %s", th.getMessage()), th);
        }
    }

    @Override
    public void onApplicationEvent(ChangeEvent event) {
        if (event.registryListener(getClass().getName()) &&
                messageParameters.isEnabled() &&
                Notification.class.isAssignableFrom(event.getChangedClass()) &&
                ChangeType.CREATE.equals(event.getChangeType())) {
            List<Notification> targetNotifications = Lists.newArrayList(event.getChanged(Notification.class));
            Collections.sort(targetNotifications, notificationComparator);
            doSendMessage(targetNotifications);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!messageParameters.isEnabled()) {
            LOG.info("Email notification will be skipped: {} not properly configured", getClass().getSimpleName());
        }
    }

    /**
     * Класс <class>MessageParameters</class> описывает параметры отправки сообщения
     *
     * @author Nazin Alexander
     */
    public static class MessageParameters {

        private static final String MULTIPLE_ADDRESS_DELIMITER = ";";

        /** Адрес источника */
        private String from;
        /** Адреса получателей */
        private String[] to;
        /** Адреса копий */
        private String[] cc;
        /** Адреса скрытых копий */
        private String[] bcc;

        private static String[] convertAddresses(String address) {
            return StringUtils.hasText(FormatUtil.parseString(address)) ?
                    StringUtils.trimArrayElements(address.split(MULTIPLE_ADDRESS_DELIMITER)) :
                    null;
        }

        public boolean isEnabled() {
            return StringUtils.hasText(from) && to != null;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public void setTo(String to) {
            this.to = convertAddresses(to);
        }

        public void setCc(String cc) {
            this.cc = convertAddresses(cc);
        }

        public void setBcc(String bcc) {
            this.bcc = convertAddresses(bcc);
        }

        /**
         * Устанавливает параметры сообщения в провайдер отправки
         *
         * @param helper провайдер отправки
         * @return Возвращает модифицированный провайдер
         */
        public MimeMessageHelper injectTo(MimeMessageHelper helper) throws MessagingException {
            helper.setFrom(from);
            helper.setTo(to);
            if (cc != null) {
                helper.setCc(cc);
            }
            if (bcc != null) {
                helper.setBcc(bcc);
            }
            return helper;
        }
    }
}
