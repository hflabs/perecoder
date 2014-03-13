package ru.hflabs.izpack.validator.db;

import com.izforge.izpack.api.data.InstallData;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Класс <class>OracleDatabaseConnector</class> реализует коннектор соединения с БД Oracle
 *
 * @author Nazin Alexander
 */
public class OracleDatabaseConnector extends DatabaseConnectorTemplate implements DatabaseConnector {

    public static final String DRIVER_CLASS_NAME = "oracle.jdbc.OracleDriver";
    public static final String TYPE = "oracle";
    public static final String SID = "izpack.jdbc.sid";
    public static final String URL_TEMPLATE = "jdbc:%s:thin:@%s:%s:%s";

    public static final String SQL_EXIST_USER = "SELECT COUNT(*) FROM dba_users WHERE username = UPPER(?)";
    public static final String SQL_EXIST_SPACE = "SELECT COUNT(*) FROM dba_tablespaces WHERE tablespace_name = UPPER(?)";
    public static final String SQL_CHECK_PRIVILEGE = "SELECT COUNT(*) FROM session_privs WHERE privilege = UPPER(?)";

    @Override
    public String createUrl(InstallData installData) {
        return String.format(
                URL_TEMPLATE,
                TYPE, installData.getVariable(JDBC_HOST), installData.getVariable(JDBC_PORT), installData.getVariable(SID)
        );
    }

    @Override
    public String driverClassName() {
        return DRIVER_CLASS_NAME;
    }

    @Override
    public boolean isUserExist(Connection connection, String userName) throws SQLException {
        return doCountQuery(connection, SQL_EXIST_USER, userName) != 0;
    }

    @Override
    public boolean isAllowUserCreate(Connection connection, String host, String userName) throws SQLException {
        return doCountQuery(connection, SQL_CHECK_PRIVILEGE, "create user") != 0;
    }

    @Override
    public boolean isDBExist(Connection connection, String dbName) throws SQLException {
        return doCountQuery(connection, SQL_EXIST_SPACE, dbName) != 0;
    }

    @Override
    public boolean isAllowDBCreate(Connection connection, String host, String userName) throws SQLException {
        return doCountQuery(connection, SQL_CHECK_PRIVILEGE, "create tablespace") != 0;
    }
}
