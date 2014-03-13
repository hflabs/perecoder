package ru.hflabs.rcd.connector.files.converter;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import ru.hflabs.rcd.model.connector.TransferRuleDescriptor;
import ru.hflabs.rcd.model.path.DictionaryNamedPath;
import ru.hflabs.rcd.model.path.FieldNamedPath;
import ru.hflabs.rcd.model.rule.RecodeRule;
import ru.hflabs.util.spring.core.convert.converter.ReverseConverter;
import ru.hflabs.util.spring.util.ReflectionUtil;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Класс <class>RecodeRulesOverDataSetConverter</class> реализует конвертацию правил перекодирования из {@link IDataSet}-ов
 *
 * @author Nazin Alexander
 * @see IDataSet
 */
public class RecodeRulesConverter implements ReverseConverter<IDataSet, TransferRuleDescriptor> {

    /** Минимальное количество колонок */
    private static final int MIN_COLUMN_COUNT = 2;
    /** Разделитель названия группы и названия поля */
    private static final int GROUP_SEPARATOR = '.';

    /**
     * Выделяет название поля с учетом разделителя группы
     *
     * @param fieldName сходное название поля
     * @return Возвращает название поля
     */
    private String formatFieldName(String fieldName) {
        int indexOfDot = fieldName.indexOf(GROUP_SEPARATOR);
        return (indexOfDot != -1 && indexOfDot != fieldName.length() - 1) ?
                fieldName.substring(indexOfDot + 1, fieldName.length()) :
                fieldName;
    }

    /**
     * Создает и возвращает именованную запись справочника
     *
     * @param namedPath именованный путь справочника
     * @param fieldName название поля
     * @param value значение поля
     * @return Возвращает именованную запись справочника
     */
    private FieldNamedPath createFieldNamedPath(DictionaryNamedPath namedPath, String fieldName, Object value) {
        return new FieldNamedPath(
                namedPath,
                formatFieldName(fieldName),
                value != null ? value.toString() : null
        );
    }

    /**
     * Создает коллекцию правил перекодирования на основе таблицы
     *
     * @param from именованный путь источника
     * @param to именованный путь назначения
     * @param table таблица
     * @return Возвращает коллекцию правил перекодирования
     */
    private Collection<RecodeRule> createRecodeRules(DictionaryNamedPath from, DictionaryNamedPath to, ITable table) throws DataSetException {
        Collection<RecodeRule> rules = new ArrayList<RecodeRule>(table.getRowCount());

        for (int row = 0; row < table.getRowCount(); row++) {
            final RecodeRule rule = new RecodeRule();
            Column[] columns = table.getTableMetaData().getColumns();
            // Поля источника
            int columnIndex = 0;
            {
                rule.injectFromNamedPath(
                        createFieldNamedPath(from, columns[columnIndex].getColumnName(), table.getValue(row, columns[columnIndex].getColumnName()))
                );
            }
            // Поля назначения
            columnIndex++;
            {
                rule.injectToNamedPath(
                        createFieldNamedPath(to, columns[columnIndex].getColumnName(), table.getValue(row, columns[columnIndex].getColumnName()))
                );
            }
            rules.add(rule);
        }

        return rules;
    }

    @Override
    public TransferRuleDescriptor convert(IDataSet source) {
        Collection<RecodeRule> result = new ArrayList<>();
        try {
            for (String tableName : source.getTableNames()) {
                ITable table = source.getTable(tableName);
                int columnCount = table.getTableMetaData().getColumns().length;
                if (columnCount >= MIN_COLUMN_COUNT) {
                    DictionaryNamedPath from = new DictionaryNamedPath(null, tableName);
                    DictionaryNamedPath to = new DictionaryNamedPath(null, tableName);
                    result.addAll(createRecodeRules(from, to, table));
                } else {
                    throw new IllegalArgumentException(
                            String.format(
                                    "Recode table with name '%s' does not have enough columns to create recode rules. Expected: %d, got: %d", tableName, MIN_COLUMN_COUNT, columnCount
                            )
                    );
                }
            }
        } catch (DataSetException ex) {
            ReflectionUtil.rethrowRuntimeException(ex);
        }
        return new TransferRuleDescriptor(result);
    }

    @Override
    public IDataSet reverseConvert(TransferRuleDescriptor target) {
        throw new UnsupportedOperationException("Not implement yet.");
    }
}
