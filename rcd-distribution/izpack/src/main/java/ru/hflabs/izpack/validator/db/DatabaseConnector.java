package ru.hflabs.izpack.validator.db;

import com.izforge.izpack.api.data.InstallData;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Интерфейс <class>DatabaseConnector</class> декларирует методы сознания соединения с БД
 *
 * @author Nazin Alexander
 */
public interface DatabaseConnector {

    /** Полный URL соединения с БД */
    String JDBC_URL = "izpack.jdbc.url";
    /** Название класса драйвера */
    String JDBC_DRIVER = "izpack.jdbc.driverClassName";
    /** Тип БД */
    String JDBC_TYPE = "izpack.jdbc.type";
    /** Хост */
    String JDBC_HOST = "izpack.jdbc.host";
    /** Порт */
    String JDBC_PORT = "izpack.jdbc.port";
    /** Имя администратора */
    String JDBC_DBA_USERNAME = "izpack.jdbc.dba.username";
    /** Пароль администратора */
    String JDBC_DBA_PASSWORD = "izpack.jdbc.dba.password";
    /** Имя пользователя */
    String JDBC_USERNAME = "izpack.jdbc.username";
    /** Пароль пользователя */
    String JDBC_PASSWORD = "izpack.jdbc.password";

    /**
     * Создает и возвращает URL доступа к БД
     *
     * @param installData параметры установки
     * @return Возвращает сформированный URL
     */
    String createUrl(InstallData installData);

    /**
     * Возвращает драйвер работы с БД
     *
     * @return Возвращает драйвер работы с БД
     */
    String driverClassName();

    /**
     * Проверяет существование пользователя
     *
     * @param connection соединение с БД
     * @param userName проверяемый пользователь
     * @return Возвращает <code>TRUE</code>, если пользователь с указанным именем существует
     */
    boolean isUserExist(Connection connection, String userName) throws SQLException;

    /**
     * Проверяет возможность создать пользователя
     *
     * @param connection соединение с БД
     * @param host целевой хост
     * @param userName имя пользователя, от которого будет выполнятся запрос (как правило это DBA)  @return Возвращает <code>TRUE</code>, если пользователь имеет право на создание
     */
    boolean isAllowUserCreate(Connection connection, String host, String userName) throws SQLException;

    /**
     * Проверяет существование пространтства хранения данных
     *
     * @param connection соединение с БД
     * @param dbName проверяемая БД
     * @return Возвращает <code>TRUE</code>, если пользователь с указанным именем существует
     */
    boolean isDBExist(Connection connection, String dbName) throws SQLException;

    /**
     * Проверяет возможность создать пространтство хранения данных
     *
     * @param connection соединение с БД
     * @param host целевой хост
     * @param userName имя пользователя, от которого будет выполнятся запрос (как правило это DBA)  @return Возвращает <code>TRUE</code>, если пользователь имеет право на создание
     */
    boolean isAllowDBCreate(Connection connection, String host, String userName) throws SQLException;
}
