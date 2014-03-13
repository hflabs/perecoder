package ru.hflabs.izpack.validator.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Класс <class>DatabaseConnectorTemplate</class> реализует базовые методы для соединения с БД
 *
 * @author Nazin Alexander
 */
public abstract class DatabaseConnectorTemplate {

    /**
     * Выполняет запрос получения количества
     *
     * @param connection соединение
     * @param sql запрос
     * @param parameter строковый параметр запроса
     * @return Возвращает количество
     */
    protected static int doCountQuery(Connection connection, String sql, String parameter) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, parameter);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                } else {
                    return 0;
                }
            }
        }
    }
}
