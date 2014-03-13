package ru.hflabs.rcd.security;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.hflabs.util.security.SecurityUtil;
import ru.hflabs.util.security.SystemAuthenticationProvider;
import ru.hflabs.util.security.SystemAuthenticationProviderAware;

/**
 * Класс <class>SystemAuthenticationInvocationInterceptor</class> реализует установки фиктивной аутентификации
 *
 * @author Nazin Alexander
 */
public class SystemAuthenticationInvocationInterceptor implements MethodInterceptor, SystemAuthenticationProviderAware {

    /** Провайдер системной аутентификации */
    private SystemAuthenticationProvider systemAuthenticationProvider;

    @Override
    public void setSystemAuthenticationProvider(SystemAuthenticationProvider systemAuthenticationProvider) {
        this.systemAuthenticationProvider = systemAuthenticationProvider;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        boolean injected = SecurityUtil.isAuthenticated();
        if (!injected) {
            SecurityContextHolder.getContext().setAuthentication(systemAuthenticationProvider.createSystemAuthentication());
        }
        try {
            return invocation.proceed();
        } finally {
            if (!injected) {
                SecurityContextHolder.getContext().setAuthentication(null);
            }
        }
    }
}
