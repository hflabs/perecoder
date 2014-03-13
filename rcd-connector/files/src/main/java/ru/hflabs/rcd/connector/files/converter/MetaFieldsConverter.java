package ru.hflabs.rcd.connector.files.converter;

import com.google.common.collect.ImmutableList;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.exception.constraint.CollisionDataException;
import ru.hflabs.rcd.exception.constraint.DuplicateNameException;
import ru.hflabs.rcd.exception.constraint.IllegalNameException;
import ru.hflabs.rcd.exception.constraint.document.IllegalMetaFieldException;
import ru.hflabs.rcd.exception.transfer.TranslateDataException;
import ru.hflabs.rcd.model.ModelUtils;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.util.core.FormatUtil;
import ru.hflabs.util.core.collection.ArrayUtil;
import ru.hflabs.util.spring.Assert;
import ru.hflabs.util.spring.core.convert.converter.ReverseConverter;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static ru.hflabs.rcd.accessor.Accessors.injectTrimmedName;
import static ru.hflabs.rcd.accessor.Accessors.linkDescendants;

/**
 * Класс <class>MetaFieldsConverter</class> реализует класс сигнатуры МЕТА-полей справочников в {@link ITable}
 *
 * @author Nazin Alexander
 * @see Dictionary
 */
public class MetaFieldsConverter implements ReverseConverter<ITable, Dictionary> {

    /** Постфикс названия таблицы по умолчанию */
    public static final String DEFAULT_TABLE_POSTFIX = "_meta";

    /** Постфикс названия таблицы */
    private String tablePostfix;

    public MetaFieldsConverter() {
        this(DEFAULT_TABLE_POSTFIX);
    }

    public MetaFieldsConverter(String tablePostfix) {
        this.tablePostfix = tablePostfix;
    }

    /**
     * Создает название таблицы из названия справочника
     *
     * @param originalName исходное название
     * @return Возвращает название таблицы
     */
    private String createDictionaryName(String originalName) {
        return originalName + tablePostfix;
    }

    /**
     * Выделяет название справочника из названия таблицы
     *
     * @param originalName исходное название
     * @return Возвращает название справочника
     */
    private String extractDictionaryName(String originalName) {
        int indexOfDot = originalName.indexOf(tablePostfix);
        return (indexOfDot != -1 && originalName.endsWith(tablePostfix)) ?
                originalName.substring(0, originalName.length() - tablePostfix.length()) :
                originalName;
    }

    /**
     * Устанавливает свойство МЕТА-поля основываясь на названии колонки таблицы
     *
     * @param metaField целевое МЕТА-поле
     * @param columnName название колонки таблицы
     * @param rawValue значение ячейки таблицы
     * @return Возвращает модифицированное МЕТА-поле
     */
    private static MetaField populateMetaField(MetaField metaField, String columnName, Object rawValue) throws DataSetException {
        String stringValue = FormatUtil.parseString((rawValue != null) ? String.valueOf(rawValue) : null);

        if (MetaField.NAME.equalsIgnoreCase(columnName)) {
            metaField.setName(stringValue);
        } else if (MetaField.DESCRIPTION.equals(columnName)) {
            metaField.setDescription(stringValue);
        } else {
            boolean booleanValue = FormatUtil.parseBoolean(stringValue);
            if (booleanValue) {
                if (MetaField.FLAG_PRIMARY_NAME.equals(columnName)) {
                    metaField.establishFlags(MetaField.FLAG_PRIMARY);
                }
                if (MetaField.FLAG_UNIQUE_NAME.equals(columnName)) {
                    metaField.establishFlags(MetaField.FLAG_UNIQUE);
                }
                if (MetaField.FLAG_HIDDEN_NAME.equals(columnName)) {
                    metaField.establishFlags(MetaField.FLAG_HIDDEN);
                }
            }
        }

        return metaField;
    }

    @Override
    public Dictionary convert(ITable source) {
        // Формируем справочник
        Assert.notNull(source, "Source table must not be NULL");
        ITableMetaData tableMetaData = source.getTableMetaData();
        Assert.notNull(tableMetaData, "Table columns must not be NULL");
        String dictionaryName = extractDictionaryName(tableMetaData.getTableName());
        Assert.isFalse(
                StringUtils.isEmpty(dictionaryName),
                String.format("Dictionary name must not be empty"),
                IllegalNameException.class
        );
        Dictionary dictionary = injectTrimmedName(new Dictionary(), dictionaryName);

        // Формируем МЕТА-поля справочника
        try {
            MetaField primaryMetaField = null;
            Map<String, MetaField> metaFields = new LinkedHashMap<>(source.getRowCount());
            // Для каждой строки таблицы формируем МЕТА-поле
            for (int i = 0; i < source.getRowCount(); i++) {
                MetaField metaField = new MetaField();
                metaField.setOrdinal(i);
                for (Column column : tableMetaData.getColumns()) {
                    String columnName = column.getColumnName();
                    metaField = populateMetaField(metaField, columnName, source.getValue(i, columnName));
                }
                // Проверяем, что название МЕТА-поле задано
                Assert.isFalse(
                        StringUtils.isEmpty(metaField.getName()),
                        String.format("Dictionary '%s' contains meta field with empty name", dictionaryName),
                        IllegalNameException.class
                );
                // Сохраняем МЕТА-поле, проверяя, что его название уникально
                Assert.isNull(
                        metaFields.put(ModelUtils.LOWER_CASE_FUNCTION.apply(metaField.getName()), metaField),
                        String.format("Dictionary '%s' contains duplicate meta fields '%s'", dictionary.getName(), metaField.getName()),
                        DuplicateNameException.class
                );
                // Сохраняем первичное МЕТА-поле
                if (metaField.isFlagEstablished(MetaField.FLAG_PRIMARY)) {
                    Assert.isNull(
                            primaryMetaField,
                            String.format("Dictionary '%s' contains duplicate primary meta fields '%s'", dictionary.getName(), metaField.getName()),
                            CollisionDataException.class
                    );
                    primaryMetaField = metaField;
                }
            }
            // Проверяем, что первичное МЕТА-поле установлено
            Assert.notNull(
                    primaryMetaField,
                    String.format("Dictionary '%s' must have one primary meta field", dictionary.getName()),
                    IllegalMetaFieldException.class
            );

            return linkDescendants(dictionary, metaFields.values());
        } catch (DataSetException ex) {
            throw new TranslateDataException(String.format("Can't convert table to dictionary '%s'. Cause by: %s", dictionary.getName(), ex.getMessage()), ex);
        }
    }

    /**
     * Создает и возвращает МЕТА-данные таблицы на основе МЕТА-полей справочника
     *
     * @param dictionaryName название справочника
     * @return Возвращает описание таблицы
     */
    private ITableMetaData createTableMetaData(String dictionaryName) {
        Collection<Column> columns = ImmutableList.<Column>builder()
                .add(new Column(MetaField.NAME, DataType.VARCHAR))
                .add(new Column(MetaField.DESCRIPTION, DataType.VARCHAR))
                .add(new Column(MetaField.FLAG_PRIMARY_NAME, DataType.VARCHAR))
                .add(new Column(MetaField.FLAG_UNIQUE_NAME, DataType.VARCHAR))
                .add(new Column(MetaField.FLAG_HIDDEN_NAME, DataType.VARCHAR))
                .build();
        return new DefaultTableMetaData(dictionaryName, ArrayUtil.toArray(Column.class, columns));
    }

    @Override
    public ITable reverseConvert(Dictionary dictionary) {
        Assert.notNull(dictionary, "Target dictionary must not be NULL");

        String dictionaryName = createDictionaryName(dictionary.getName());
        Collection<MetaField> metaFields = dictionary.getDescendants();
        Assert.notNull(metaFields, "Dictionary meta fields must not be NULL");

        try {
            DefaultTable table = new DefaultTable(createTableMetaData(dictionaryName));
            for (MetaField metaField : ModelUtils.sortMetaFieldsByOrdinal(metaFields)) {
                table.addRow(new Object[]{
                        metaField.getName(),
                        metaField.getDescription(),
                        metaField.isFlagEstablished(MetaField.FLAG_PRIMARY),
                        metaField.isFlagEstablished(MetaField.FLAG_UNIQUE),
                        metaField.isFlagEstablished(MetaField.FLAG_HIDDEN)
                });
            }
            return table;
        } catch (DataSetException ex) {
            throw new TranslateDataException(String.format("Can't convert dictionary '%s' to table. Cause by: %s", dictionary.getName(), ex.getMessage()), ex);
        }
    }
}
