package ru.hflabs.rcd.connector.db.converter;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;
import ru.hflabs.rcd.exception.constraint.DuplicateNameException;
import ru.hflabs.rcd.exception.transfer.IncompleteDataException;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.util.core.EqualsUtil;
import ru.hflabs.util.core.FormatUtil;
import ru.hflabs.util.spring.Assert;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import static ru.hflabs.rcd.accessor.Accessors.injectName;
import static ru.hflabs.rcd.accessor.Accessors.linkFieldToMetaField;
import static ru.hflabs.rcd.model.ModelUtils.createRecordId;

/**
 * Класс <class>DataSourceDictionaryConverter</class> реализует конвертацию значений полей справочника
 *
 * @author Nazin Alexander
 */
public class DataSourceDictionaryConverter implements ResultSetExtractor<Collection<MetaField>> {

    /** Название первичного ключа справочника */
    private final String primaryMetaFieldName;
    /** Сервис преобразования записей */
    private final RowMapper<Map<String, Object>> rowMapper;

    public DataSourceDictionaryConverter(String primaryMetaFieldName) {
        this.primaryMetaFieldName = primaryMetaFieldName;
        this.rowMapper = new ColumnMapRowMapper();
    }

    /**
     * Выполняет преобразование {@link Object} в строку
     *
     * @param target целевое значение
     * @return Возвращает строковое представление значения
     */
    private static String extractStringValue(Object target) {
        if (target == null) {
            return null;
        } else if (target instanceof String) {
            return FormatUtil.parseString((String) target);
        } else if (target instanceof Long) {
            return FormatUtil.format((Long) target);
        } else if (target instanceof Integer) {
            return FormatUtil.format((Integer) target);
        } else if (target instanceof Double) {
            return FormatUtil.format((Double) target);
        } else if (target instanceof BigDecimal) {
            return FormatUtil.format((BigDecimal) target);
        } else if (target instanceof Boolean) {
            return FormatUtil.format((Boolean) target);
        } else if (target instanceof Date) {
            return FormatUtil.format((Date) target);
        } else {
            return String.valueOf(target);
        }
    }

    /**
     * Выполняет формирование коллекции МЕТА-полей из {@link ResultSet}-а
     *
     * @param rs {@link ResultSet} формирования записей
     * @return Возвращает коллекцию сформированных МЕТА-полей
     */
    private Map<String, MetaField> extractMetaFields(ResultSet rs) throws SQLException {
        final ResultSetMetaData rsmd = rs.getMetaData();
        final int columnCount = rsmd.getColumnCount();

        MetaField primaryMetaField = null;
        Map<String, MetaField> metaFields = new LinkedCaseInsensitiveMap<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            // Формируем МЕТА-поле
            String name = FormatUtil.parseString(JdbcUtils.lookupColumnName(rsmd, i));
            MetaField metaField = new MetaField();
            {
                metaField = injectName(metaField, name);
                metaField.setOrdinal(i - 1);
            }
            // Проверяем, что МЕТА-поле является первичным
            if (EqualsUtil.lowerCaseEquals(primaryMetaFieldName, name)) {
                metaField.establishFlags(MetaField.FLAG_PRIMARY);
                Assert.isNull(
                        primaryMetaField,
                        String.format("Duplicate primary meta field name '%s'", metaField.getName()),
                        DuplicateNameException.class
                );
                primaryMetaField = metaField;
            }
            // Сохраняем сформированное МЕТА-поле
            Assert.isNull(
                    metaFields.put(name, metaField),
                    String.format("Duplicate meta field name '%s'", metaField.getName()),
                    DuplicateNameException.class
            );
        }
        // Проверяем, что первичное МЕТА-поле задано
        Assert.notNull(
                primaryMetaField,
                String.format("Missing primary meta field with name '%s'", primaryMetaFieldName),
                IncompleteDataException.class
        );

        return metaFields;
    }

    /**
     * Создает и возвращает первичное МЕТА-поле справочника, указывающее на идентификатор записи
     *
     * @param primaryMetaField первичное МЕТА-поле
     * @param value значение
     * @return Возвращает созданное значение поля
     */
    private Field convertPrimaryField(MetaField primaryMetaField, Object value) {
        Field result = new Field();
        {
            result.setValue(extractStringValue(value));
            result.setName(createRecordId(result));
        }
        return linkFieldToMetaField(primaryMetaField, result);
    }

    /**
     * Создает и возвращает первичное МЕТА-поле справочника, указывающее на идентификатор записи
     *
     * @param primaryField значение первичного МЕТА-поля
     * @param metaField МЕТА-поле
     * @param value значение
     * @return Возвращает созданное значение поля
     */
    private Field convertField(Field primaryField, MetaField metaField, Object value) {
        Field result = new Field();
        {
            result.setValue(extractStringValue(value));
            result.setName(primaryField.getName());
        }
        return linkFieldToMetaField(metaField, result);
    }

    /**
     * Выполняет формирование значения полей
     *
     * @param metaFields коллекция МЕТА-полей
     * @param row целевые значения
     * @return Возвращает коллекцию сформированных значений
     */
    private Map<String, MetaField> convertFields(Map<String, MetaField> metaFields, Map<String, Object> row) {
        Field primaryField = convertPrimaryField(metaFields.get(primaryMetaFieldName), row.remove(primaryMetaFieldName));
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            MetaField metaField = metaFields.get(entry.getKey());
            convertField(primaryField, metaField, entry.getValue());
        }
        return metaFields;
    }

    @Override
    public Collection<MetaField> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, MetaField> metaFields = extractMetaFields(rs);

        int rowNum = 0;
        while (rs.next()) {
            metaFields = convertFields(metaFields, rowMapper.mapRow(rs, rowNum++));
        }

        return metaFields.values();
    }
}
