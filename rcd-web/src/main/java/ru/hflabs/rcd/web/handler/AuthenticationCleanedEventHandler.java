package ru.hflabs.rcd.web.handler;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import ru.hflabs.util.security.authentication.event.AuthenticationCleanedEvent;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Класс <class>AuthenticationCleanedEventHandler</class> реализует обработчика событий завершения сессии
 *
 * @see LogoutSuccessHandler
 */
public class AuthenticationCleanedEventHandler implements LogoutSuccessHandler, ApplicationEventPublisherAware {

    /** Делегат обработчика */
    private LogoutSuccessHandler delegate;
    /** Сервис публикации событий */
    private ApplicationEventPublisher eventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    public void setDelegate(LogoutSuccessHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // Если аутентификация задана, то выполняем публикацию события об успешном выходе
        if (authentication != null) {
            eventPublisher.publishEvent(new AuthenticationCleanedEvent(authentication));
        }
        // Выполняем методы делегата
        if (delegate != null) {
            delegate.onLogoutSuccess(request, response, authentication);
        }
    }
}
