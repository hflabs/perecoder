package ru.hflabs.izpack.validator.db;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator;

import java.net.Socket;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

import static ru.hflabs.izpack.validator.db.DatabaseConnector.*;


/**
 * Класс <class>DatabaseValidator</class> реализует валидатор соединения с БД
 *
 * @author Nazin Alexander
 */
public class DatabaseValidator implements DataValidator {

    public static final String ERROR_UNSUPPORTED_TYPE = "unsupportedType";
    public static final String ERROR_UNSUPPORTED_DRIVER = "unsupportedDriver";
    public static final String ERROR_CONNECTION = "connection";
    public static final String ERROR_AUTHENTICATION = "authentication";
    public static final String ERROR_EXIST_USER = "exist.user";
    public static final String ERROR_EXIST_DB = "exist.db";
    public static final String ERROR_PERMISSION_USER = "permission.user";
    public static final String ERROR_PERMISSION_DB = "permission.db";

    /** Коллекция поддерживаемых коннекторов */
    private static final Map<String, DatabaseConnector> CONNECTORS;

    static {
        CONNECTORS = new HashMap<>();
        CONNECTORS.put(OracleDatabaseConnector.TYPE, new OracleDatabaseConnector());
        CONNECTORS.put(MySQLDatabaseConnector.TYPE_MYSQL, new MySQLDatabaseConnector(MySQLDatabaseConnector.TYPE_MYSQL));
        CONNECTORS.put(MySQLDatabaseConnector.TYPE_MARIADB, new MySQLDatabaseConnector(MySQLDatabaseConnector.TYPE_MARIADB));
    }

    /** Последнее сообщение проверки БД */
    private String errorId = "";

    /**
     * Проверяет валидность введенного адреса БД
     *
     * @param host хост
     * @param port порт
     * @return Возвращает <code>TRUE</code>, если возможно создать соединение
     */
    private boolean isConnectionValid(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            return socket.isConnected();
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Создает и возвращает соединение с JDBC
     *
     * @param url URL соединения
     * @param userName имя пользователя
     * @param password пароль пользователя
     * @return Возвращает соединение с БД
     */
    private Connection createConnection(String url, String userName, String password) {
        try {
            Connection connection = DriverManager.getConnection(url, userName, password);
            if (!connection.isValid(0)) {
                connection.close();
            }
            return connection;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public Status validateData(InstallData installData) {
        errorId = "";
        // Получаем коннектор по его типу
        DatabaseConnector connector = CONNECTORS.get(installData.getVariable(JDBC_TYPE));
        if (connector == null) {
            errorId = ERROR_UNSUPPORTED_TYPE;
            return Status.ERROR;
        }
        // Проверяем возможность соединения с БД
        if (!isConnectionValid(installData.getVariable(JDBC_HOST), Integer.valueOf(installData.getVariable(JDBC_PORT)))) {
            errorId = ERROR_CONNECTION;
            return Status.ERROR;
        }
        // Формируем URL соединения
        String jdbcUrl = connector.createUrl(installData);
        String jdbcDriverClassName = connector.driverClassName();
        try {
            DriverManager.registerDriver((Driver) Class.forName(jdbcDriverClassName).newInstance());
            DriverManager.getDriver(jdbcUrl);
        } catch (Exception ex) {
            errorId = ERROR_UNSUPPORTED_DRIVER;
            return Status.ERROR;
        }
        // Устанавливаем сформированные переменные JDBC
        installData.setVariable(JDBC_DRIVER, jdbcDriverClassName);
        installData.setVariable(JDBC_URL, jdbcUrl);
        // Проверяем соединение с БД
        try (Connection connection = createConnection(jdbcUrl, installData.getVariable(JDBC_DBA_USERNAME), installData.getVariable(JDBC_DBA_PASSWORD))) {
            if (connection == null) {
                errorId = ERROR_AUTHENTICATION;
                return Status.ERROR;
            }
            // Проверяем существование пользователя
            if (connector.isUserExist(connection, installData.getVariable(JDBC_USERNAME))) {
                errorId = ERROR_EXIST_USER;
                return Status.ERROR;
            }
            // Проверяем существование пространтства хранения данных
            if (connector.isDBExist(connection, installData.getVariable(JDBC_USERNAME))) {
                errorId = ERROR_EXIST_DB;
                return Status.ERROR;
            }
            // Проверяем право на создание пользователя
            if (!connector.isAllowUserCreate(connection, installData.getVariable(JDBC_HOST), installData.getVariable(JDBC_DBA_USERNAME))) {
                errorId = ERROR_PERMISSION_USER;
                return Status.ERROR;
            }
            // Проверяем право на создание пространтства хранения данных
            if (!connector.isAllowDBCreate(connection, installData.getVariable(JDBC_HOST), installData.getVariable(JDBC_DBA_USERNAME))) {
                errorId = ERROR_PERMISSION_DB;
                return Status.ERROR;
            }
        } catch (Exception ex) {
            return Status.ERROR;
        }
        // Все проверки пройдены
        return Status.OK;
    }

    @Override
    public String getErrorMessageId() {
        return createMessageId(Status.ERROR, errorId);
    }

    @Override
    public String getWarningMessageId() {
        return createMessageId(Status.WARNING, null);
    }

    @Override
    public boolean getDefaultAnswer() {
        return false;
    }

    /**
     * Формирует и возвращает сообщение об ошибке
     *
     * @param status статус
     * @param id идентификатор детализированного сообщения
     * @return Возвращает сформированный идентификатор сообщения
     */
    private static String createMessageId(Status status, String id) {
        StringBuilder builder = new StringBuilder();
        builder.append(DatabaseValidator.class.getSimpleName());
        builder.append('.');
        builder.append(status.getAttribute());
        if (id != null && !id.isEmpty()) {
            builder.append('.');
            builder.append(id);
        }
        return builder.toString();
    }
}
