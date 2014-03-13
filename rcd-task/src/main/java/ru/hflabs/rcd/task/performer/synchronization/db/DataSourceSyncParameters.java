package ru.hflabs.rcd.task.performer.synchronization.db;

import ru.hflabs.rcd.model.task.TaskParameterDefinition;
import ru.hflabs.rcd.task.performer.synchronization.SynchronizationParameters;
import ru.hflabs.util.core.FormatUtil;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Класс <class>DataSourceSyncParameters</class> реализует декоратор параметров соединения с БД
 *
 * @see SynchronizationParameters
 */
public class DataSourceSyncParameters extends SynchronizationParameters {

    /** Регулярное выражение названия таблицы */
    public static final transient String TABLE_NAME_PATTERN = "^\\w+(\\.\\w+)*$";

    /** Идентификатор драйвера */
    public static final transient TaskParameterDefinition<String> DRIVER_NAME = new TaskParameterDefinition<>("driverName", null);
    /** URL соединения */
    public static final transient TaskParameterDefinition<String> JDBC_URL = new TaskParameterDefinition<>("jdbcUrl", null);
    /** Дополнительные настройки соединения через разделитель */
    public static final transient TaskParameterDefinition<String> JDBC_PROPERTIES = new TaskParameterDefinition<>("jdbcProperties", null);
    /** Имя пользователя */
    public static final transient TaskParameterDefinition<String> USERNAME = new TaskParameterDefinition<>("username", null);
    /** Пароль пользователя */
    public static final transient TaskParameterDefinition<String> PASSWORD = new TaskParameterDefinition<>("password", null);
    /** Название таблицы с описанием справочников (table of content) */
    public static final transient TaskParameterDefinition<String> TOC_TABLE_NAME = new TaskParameterDefinition<>("tocTableName", null);

    @NotNull
    public String getDriverName() {
        return retrieveParameter(DRIVER_NAME.name, String.class);
    }

    public void setDriverName(String driverName) {
        injectParameter(DRIVER_NAME.name, FormatUtil.parseString(driverName));
    }

    @NotNull
    public String getJdbcUrl() {
        return retrieveParameter(JDBC_URL.name, String.class);
    }

    public void setJdbcUrl(String jdbcUrl) {
        injectParameter(JDBC_URL.name, FormatUtil.parseString(jdbcUrl));
    }

    public String getJdbcProperties() {
        return retrieveParameter(JDBC_PROPERTIES.name, String.class);
    }

    public void setJdbcProperties(String jdbcProperties) {
        injectParameter(JDBC_PROPERTIES.name, FormatUtil.parseString(jdbcProperties));
    }

    public String getUsername() {
        return retrieveParameter(USERNAME.name, String.class);
    }

    public void setUsername(String username) {
        injectParameter(USERNAME.name, FormatUtil.parseString(username));
    }

    public String getPassword() {
        return retrieveParameter(PASSWORD.name, String.class);
    }

    public void setPassword(String password) {
        injectParameter(PASSWORD.name, FormatUtil.parseString(password));
    }

    @NotNull
    @Pattern(regexp = TABLE_NAME_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE)
    public String getTocTableName() {
        return retrieveParameter(TOC_TABLE_NAME.name, String.class);
    }

    public void setTocTableName(String tocTableName) {
        injectParameter(TOC_TABLE_NAME.name, FormatUtil.parseString(tocTableName));
    }
}
