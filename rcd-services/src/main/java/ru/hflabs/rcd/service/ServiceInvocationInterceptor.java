package ru.hflabs.rcd.service;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.style.DefaultValueStyler;
import org.springframework.core.style.ValueStyler;
import org.springframework.security.core.AuthenticationException;
import ru.hflabs.rcd.exception.ApplicationException;
import ru.hflabs.rcd.exception.ApplicationUnhandledException;
import ru.hflabs.util.core.ExceptionUtil;

/**
 * Класс <class>ServiceInvocationInterceptor</class> реализует прокси обработки исключительных ситуаций
 *
 * @see MethodInterceptor
 */
public class ServiceInvocationInterceptor implements MethodInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceInvocationInterceptor.class);

    /**
     * Выполняет форматирования аргументов вызова метода
     *
     * @param arguments аргументы
     * @return Возвращает отформатированную строку
     */
    private String formatMethodArguments(Object[] arguments) {
        StringBuilder builder = new StringBuilder();
        ValueStyler valueStyler = new DefaultValueStyler();
        for (int i = 0; i < arguments.length; i++) {
            builder.append("arg[").append(i).append("] = ");
            builder.append(valueStyler.style(arguments[i]));
            builder.append(";");
        }
        return builder.toString();
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        } catch (AuthenticationException ex) {
            LOG.debug(ex.getMessage());
            throw ex;
        } catch (ApplicationException ex) {
            LOG.debug(ex.getMessage());
            throw ex;
        } catch (Throwable th) {
            StringBuilder messageTemplate = new StringBuilder("Unexpected exception on method invocation '%s'");
            if (invocation.getArguments() != null && invocation.getArguments().length != 0) {
                messageTemplate.append(" with parameters:%n").append(formatMethodArguments(invocation.getArguments()));
            }
            LOG.error(String.format(messageTemplate.toString(), invocation.getMethod()), th);
            throw new ApplicationUnhandledException(ExceptionUtil.throwableToString(th, true));
        }
    }
}
