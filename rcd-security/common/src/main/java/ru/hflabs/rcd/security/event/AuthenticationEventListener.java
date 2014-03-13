package ru.hflabs.rcd.security.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import ru.hflabs.util.core.date.DateUtil;
import ru.hflabs.util.security.authentication.event.AuthenticationCleanedEvent;
import ru.hflabs.util.spring.context.event.AsyncApplicationListener;

import java.util.Date;
import java.util.regex.Pattern;

/**
 * Класс <class>AuthenticationEventListener</class> реализует слушателя логирования событий аутентификации
 *
 * @author Nazin Alexander
 */
public class AuthenticationEventListener implements AsyncApplicationListener<AbstractAuthenticationEvent> {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    /*
     * Шаблоны сообщений
     */
    private static final Pattern OBJECT_HEX_PATTERN = Pattern.compile("[\\w\\._$]*@\\p{XDigit}*:\\s*");
    private static final String SUCCESS_TEMPLATE = "[%s] Authentication success. Principal: '%s'; Granted authority: '%s'";
    private static final String CLEANED_TEMPLATE = "[%s] Authentication cleaned. Principal: '%s'";
    private static final String FAILURE_TEMPLATE = "[%s] Authentication failure. Principal: '%s'; Credentials: '%s'. Cause by: %s";
    private static final String DETAILS_TEMPLATE = "%s. Details: %s";

    /**
     * Выделяет имя пользователя из аутентификации
     *
     * @param authentication аутентификация
     * @return Возвращает имя пользователя
     */
    private String extractPrincipal(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            return (String) principal;
        } else if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return String.valueOf(principal);
        }
    }

    /**
     * Обрабатывает событие успешной аутентификации
     *
     * @param event событие
     */
    private void handleAuthenticationSuccessEvent(AuthenticationSuccessEvent event) {
        if (LOG.isInfoEnabled()) {
            Authentication authentication = event.getAuthentication();

            String commonMessage = String.format(
                    SUCCESS_TEMPLATE,
                    DateUtil.formatDateTime(new Date(event.getTimestamp())),
                    extractPrincipal(authentication),
                    StringUtils.collectionToCommaDelimitedString(authentication.getAuthorities())
            );
            String detailsMessage = (authentication.getDetails() != null) ? OBJECT_HEX_PATTERN.matcher(authentication.getDetails().toString()).replaceAll("") : null;
            String resultMessage = StringUtils.hasText(detailsMessage) ? String.format(DETAILS_TEMPLATE, commonMessage, detailsMessage) : commonMessage;

            LOG.info(resultMessage);
        }
    }

    /**
     * Обрататывает событие неудачной аутентификации
     *
     * @param event событие
     */
    private void handleAuthenticationFailureEvent(AbstractAuthenticationFailureEvent event) {
        if (LOG.isWarnEnabled()) {
            Authentication authentication = event.getAuthentication();

            String commonMessage = String.format(
                    FAILURE_TEMPLATE,
                    DateUtil.formatDateTime(new Date(event.getTimestamp())),
                    extractPrincipal(authentication),
                    authentication.getCredentials(),
                    event.getException() != null ? event.getException().getMessage() : "unknown"
            );
            String detailsMessage = (authentication.getDetails() != null) ? OBJECT_HEX_PATTERN.matcher(authentication.getDetails().toString()).replaceAll("") : null;
            String resultMessage = StringUtils.hasText(detailsMessage) ? String.format(DETAILS_TEMPLATE, commonMessage, detailsMessage) : commonMessage;

            LOG.warn(resultMessage);
        }
    }

    /**
     * Обрабатывает событие очистки контекста аутентификации
     *
     * @param event событие
     */
    private void handleAuthenticationCleanedEvent(AuthenticationCleanedEvent event) {
        if (LOG.isInfoEnabled()) {
            Authentication authentication = event.getAuthentication();

            String commonMessage = String.format(
                    CLEANED_TEMPLATE,
                    DateUtil.formatDateTime(new Date(event.getTimestamp())),
                    extractPrincipal(authentication)
            );
            String detailsMessage = (authentication.getDetails() != null) ? OBJECT_HEX_PATTERN.matcher(authentication.getDetails().toString()).replaceAll("") : null;
            String resultMessage = StringUtils.hasText(detailsMessage) ? String.format(DETAILS_TEMPLATE, commonMessage, detailsMessage) : commonMessage;

            LOG.info(resultMessage);
        }
    }

    @Override
    public void onApplicationEvent(AbstractAuthenticationEvent event) {
        // Authentication success
        if (event instanceof AuthenticationSuccessEvent) {
            handleAuthenticationSuccessEvent((AuthenticationSuccessEvent) event);
        }
        // Authentication failure
        if (event instanceof AbstractAuthenticationFailureEvent) {
            handleAuthenticationFailureEvent((AbstractAuthenticationFailureEvent) event);
        }
        // Authentication clear
        if (event instanceof AuthenticationCleanedEvent) {
            handleAuthenticationCleanedEvent((AuthenticationCleanedEvent) event);
        }
    }
}
