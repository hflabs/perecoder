package ru.hflabs.izpack.validator.db;

import com.izforge.izpack.api.data.InstallData;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Класс <class>MySQLDatabaseConnector</class> реализует коннектор соединения с БД MySql или MariaDB
 *
 * @author Nazin Alexander
 */
public class MySQLDatabaseConnector extends DatabaseConnectorTemplate implements DatabaseConnector {

    public static final String TYPE_MYSQL = "mysql";
    public static final String DRIVER_CLASS_NAME_MYSQL = "com.mysql.jdbc.Driver";
    public static final String TYPE_MARIADB = "mariadb";
    public static final String DRIVER_CLASS_NAME_MARIADB = "org.mariadb.jdbc.Driver";

    public static final String URL_TEMPLATE = "jdbc:%s://%s:%s";

    public static final String SQL_EXIST_USER = "SELECT COUNT(*) FROM mysql.user WHERE user = ?";
    public static final String SQL_EXIST_DB = "SELECT COUNT(*) FROM mysql.db WHERE db = ?";

    private static final String SQL_CHECK_PRIVILEGE = "SELECT count(*) FROM mysql.user where %s = 'Y' and host = '%s' and user = ?";
    public static final String SQL_CHECK_PRIVILEGE_GRANT = "grant_priv";
    public static final String SQL_CHECK_PRIVILEGE_USER = "create_user_priv";
    public static final String SQL_CHECK_PRIVILEGE_DB = "create_priv";

    private final String type;

    public MySQLDatabaseConnector(String type) {
        this.type = type;
    }

    @Override
    public String driverClassName() {
        switch (type) {
            case TYPE_MYSQL: {
                return DRIVER_CLASS_NAME_MYSQL;
            }
            case TYPE_MARIADB: {
                return DRIVER_CLASS_NAME_MARIADB;
            }
            default: {
                throw new IllegalArgumentException("Unknown driver class");
            }
        }
    }

    /**
     * Формирует запрос проверки прав, предварительно проверяя целевой хост
     *
     * @param host целевой хост
     * @param tableName проверяемая таблица
     * @return Возвращает сформированный запрос
     */
    private static String createCheckPrivilegeQuery(String host, String tableName) {
        // Определяем целевой хост
        String targetHost = "localhost".equals(host) || "127.0.0.1".equals(host) ? host : "%";
        // Формируем запрос
        return String.format(SQL_CHECK_PRIVILEGE, tableName, targetHost);
    }

    @Override
    public String createUrl(InstallData installData) {
        return String.format(
                URL_TEMPLATE,
                type, installData.getVariable(JDBC_HOST), installData.getVariable(JDBC_PORT)
        );
    }

    @Override
    public boolean isUserExist(Connection connection, String userName) throws SQLException {
        return doCountQuery(connection, SQL_EXIST_USER, userName) != 0;
    }

    @Override
    public boolean isAllowUserCreate(Connection connection, String host, String userName) throws SQLException {
        return doCountQuery(connection, createCheckPrivilegeQuery(host, SQL_CHECK_PRIVILEGE_USER), userName) != 0 &&
                doCountQuery(connection, createCheckPrivilegeQuery(host, SQL_CHECK_PRIVILEGE_GRANT), userName) != 0;
    }

    @Override
    public boolean isDBExist(Connection connection, String dbName) throws SQLException {
        return doCountQuery(connection, SQL_EXIST_DB, dbName) != 0;
    }

    @Override
    public boolean isAllowDBCreate(Connection connection, String host, String userName) throws SQLException {
        return doCountQuery(connection, createCheckPrivilegeQuery(host, SQL_CHECK_PRIVILEGE_DB), userName) != 0;
    }
}
