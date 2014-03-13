package ru.hflabs.rcd.backend.console;

import com.beust.jcommander.IDefaultProvider;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.defaultprovider.PropertyDefaultProvider;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.backend.console.preference.AuthenticationPreference;
import ru.hflabs.rcd.backend.console.preference.ConnectionPreference;
import ru.hflabs.rcd.backend.console.preference.Preference;
import ru.hflabs.rcd.exception.ApplicationException;
import ru.hflabs.rcd.service.IManagerService;
import ru.hflabs.rcd.service.IVersionService;
import ru.hflabs.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Класс <class>RunTemplate</class> реализует шаблон консольного приложения
 *
 * @author Nazin Alexander
 */
public abstract class RunTemplate<P extends Preference, C extends Command, D extends RunDescriptor> {

    private static final Logger LOG = LoggerFactory.getLogger(RunTemplate.class);

    /** Название файла конфигурации с настройками по умолчанию */
    private static final String DEFAULT_PROPERTIES = "default.properties";

    /** Сервис управления документами */
    protected IManagerService managerService;

    public void setManagerService(IManagerService managerService) {
        this.managerService = managerService;
    }

    /**
     * Выполняет действия утилиты на основании подготовленных параметров
     *
     * @param command команда для выполнения
     * @return Возвращает дескриптор выполнения
     */
    protected abstract D doExecute(P preference, C command) throws Exception;

    /**
     * Создает и возвращает основной контекст приложения
     *
     * @param preference настройки импорта
     * @return Возвращает основной контекст приложения
     */
    protected static ConfigurableApplicationContext createApplicationContext(ConnectionPreference preference) {
        System.setProperty(ConnectionPreference.SERVER_HOST, preference.getServerHost());
        System.setProperty(ConnectionPreference.SERVER_PORT, preference.getServerPort().toString());
        System.setProperty(ConnectionPreference.SERVER_PROTOCOL, preference.getServerProtocol());

        final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(
                new String[]{
                        ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "backend.remote-services.xml",
                        ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "backend.xml"
                },
                false
        );
        context.registerShutdownHook();

        return context;
    }

    /**
     * Создает и возвращает аутентификацию
     *
     * @param beanFactory фабрика создания классов
     * @param preference настройки аутентификации
     * @return Возвращает аутентификацию
     */
    protected static Authentication createAuthentication(BeanFactory beanFactory, AuthenticationPreference preference) {
        return beanFactory.getBean(AuthenticationManager.class).authenticate(
                new UsernamePasswordAuthenticationToken(preference.getLogin(), preference.getPassword())
        );
    }

    /**
     * Определяет {@link com.beust.jcommander.IDefaultProvider провайдер} получения значений настроек по умолчанию
     *
     * @param defaultPropertiesFile путь к файлу со значениями по умолчанию
     * @return Возвращает провайдер или <code>NULL</code>, если его не существует
     */
    protected static IDefaultProvider createDefaultProvider(File defaultPropertiesFile) throws IOException {
        Resource resource = new DefaultResourceLoader().getResource(defaultPropertiesFile.toURI().toString());
        if (resource.exists() && resource.isReadable()) {
            return new PropertyDefaultProvider(PropertiesLoaderUtils.loadProperties(resource));
        } else {
            LOG.warn("File by path '{}' not exist or can't be read", defaultPropertiesFile.getPath());
        }
        return null;
    }

    /**
     * Выполняет разбор аргументов командной строки и импорт документов
     *
     * @param args параметры командной строки
     */
    protected static <RT extends RunTemplate<P, C, RD>, P extends Preference, C extends Command, RD extends RunDescriptor> void doParseCmdArguments(Class<RT> programClass, P preference, Map<String, C> commands, String[] args) throws Exception {
        final JCommander commander = new JCommander(preference);
        // Устанавливаем название приложения
        commander.setProgramName(programClass.getSimpleName());
        // Устанавливаем доступные команды приложения
        for (Map.Entry<String, C> entry : commands.entrySet()) {
            commander.addCommand(entry.getKey(), entry.getValue());
        }
        // Устанавливает провайдер чтения настроект по умолчанию
        String pathToDefaultProperties = System.getProperty(DEFAULT_PROPERTIES, null);
        if (StringUtils.hasText(pathToDefaultProperties)) {
            commander.setDefaultProvider(createDefaultProvider(new File(pathToDefaultProperties)));
        }
        try {
            // Производим разбор параметров
            commander.parse(args);
            String targetCommandName = commander.getParsedCommand();
            if (preference.isHelp()) {
                if (StringUtils.hasText(targetCommandName)) {
                    commander.usage(targetCommandName);
                } else {
                    commander.usage();
                }
            } else {
                // Формируем контекст приложения
                ConfigurableApplicationContext context = createApplicationContext(preference.getConnectionPreference());
                try {
                    context.refresh();
                    // Выполняем аутентификацию
                    Authentication authentication = createAuthentication(context, preference.getAuthenticationPreference());
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Проверяем соединение и выполняем команду
                    context.getBean(IVersionService.class).currentTime();
                    RT program = context.getBean(programClass);
                    RD descriptor = program.doExecute(preference, commands.get(targetCommandName));

                    // Проверяем ошибки
                    for (Throwable th : descriptor.getErrors()) {
                        LOG.error(th.getMessage(), th);
                    }

                    System.out.println(descriptor.describe());
                } finally {
                    SecurityContextHolder.clearContext();
                    IOUtils.closeQuietly(context);
                }
            }
        } catch (RemoteAccessException ex) {
            throw new ApplicationException(String.format("Can't access to server. Cause by: %s", ex.getMessage()));
        } catch (AuthenticationException ex) {
            throw new ApplicationException(String.format("Authentication failed. Cause by: %s", ex.getMessage()));
        }
    }

    /**
     * Выполняет разбор аргументов командной строки и импорт документов
     *
     * @param args параметры командной строки
     */
    public static <RT extends RunTemplate<P, C, RD>, P extends Preference, C extends Command, RD extends RunDescriptor> void parseCmdArguments(Class<RT> programClass, P preference, Set<C> commands, String[] args) {
        try {
            doParseCmdArguments(programClass, preference, Maps.uniqueIndex(commands, new Function<C, String>() {
                @Override
                public String apply(C input) {
                    return input.getCommandName();
                }
            }), args);
        } catch (ParameterException ex) {
            System.err.println(String.format("%s%nType '%s' for print usage information", ex.getMessage(), "--help"));
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Выполняет разбор аргументов командной строки и импорт документов
     *
     * @param args параметры командной строки
     */
    public static <RT extends RunTemplate<Preference, C, RD>, C extends Command, RD extends RunDescriptor> void parseCmdArguments(Class<RT> programClass, Set<C> commands, String[] args) {
        parseCmdArguments(programClass, new Preference(), commands, args);
    }

    /**
     * Выполняет разбор аргументов командной строки и импорт документов
     *
     * @param args параметры командной строки
     */
    public static <RT extends RunTemplate<P, Command, RD>, P extends Preference, RD extends RunDescriptor> void parseCmdArguments(Class<RT> programClass, P preference, String[] args) {
        parseCmdArguments(programClass, preference, Collections.<Command>emptySet(), args);
    }
}
