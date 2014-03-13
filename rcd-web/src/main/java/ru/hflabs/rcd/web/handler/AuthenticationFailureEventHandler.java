package ru.hflabs.rcd.web.handler;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Класс <class>AuthenticationFailureEventHandler</class> реализует обработчика событий неудачных аутентификаций
 *
 * @see AuthenticationFailureHandler
 */
public class AuthenticationFailureEventHandler implements AuthenticationFailureHandler, AccessDeniedHandler {

    /** Шаблон URL-f перенаправления */
    private final String urlTemplate;
    /** Стратегия перенаправления */
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public AuthenticationFailureEventHandler(String urlTemplate) {
        this.urlTemplate = urlTemplate;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        redirectStrategy.sendRedirect(request, response, String.format(urlTemplate, exception.getClass().getSimpleName()));
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException, ServletException {
        request.getRequestDispatcher(String.format(urlTemplate, exception.getClass().getSimpleName())).forward(request, response);
    }
}
