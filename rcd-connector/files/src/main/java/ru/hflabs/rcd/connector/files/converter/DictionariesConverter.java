package ru.hflabs.rcd.connector.files.converter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.dbunit.dataset.*;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import ru.hflabs.rcd.exception.transfer.TranslateDataException;
import ru.hflabs.rcd.model.ModelUtils;
import ru.hflabs.rcd.model.connector.TransferDictionaryDescriptor;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.document.Record;
import ru.hflabs.util.core.FormatUtil;
import ru.hflabs.util.core.collection.ArrayUtil;
import ru.hflabs.util.spring.core.convert.converter.ReverseConverter;

import java.util.*;

import static ru.hflabs.rcd.accessor.Accessors.injectId;
import static ru.hflabs.rcd.model.ModelUtils.*;

/**
 * Класс <class>DictionariesConverter</class> реализует конвертацию справочников и их записей в {@link IDataSet}
 *
 * @author Nazin Alexander
 * @see IDataSet
 */
public class DictionariesConverter implements ReverseConverter<IDataSet, TransferDictionaryDescriptor> {

    /** Сервис конвертации справочника из структуры таблицы */
    private final ReverseConverter<ITableMetaData, Dictionary> dictionaryStructureConverter;
    /** Сервис конвертации структуры справочника в таблицу */
    private final ReverseConverter<ITable, Dictionary> metaFieldsConverter;

    public DictionariesConverter() {
        this.dictionaryStructureConverter = new DictionaryStructureConverter();
        this.metaFieldsConverter = new MetaFieldsConverter();
    }

    /**
     * Создает коллекцию записей справочников на основе таблицы
     *
     * @param table таблица записей
     * @return Возвращает коллекцию записей справочников
     */
    private Collection<Record> createRecords(Collection<MetaField> metaFields, ITable table) throws DataSetException {
        final Map<String, MetaField> name2metaFields = Maps.uniqueIndex(metaFields, NAME_FUNCTION);
        final MetaField primaryMetaField = retrievePrimaryMetaField(metaFields);

        final Map<String, Record> result = new LinkedHashMap<>(table.getRowCount());
        for (int row = 0; row < table.getRowCount(); row++) {
            Map<String, Field> fields = Maps.newLinkedHashMap();
            for (Column column : table.getTableMetaData().getColumns()) {
                String columnName = column.getColumnName();
                Field field = new Field();
                {
                    Object value = table.getValue(row, columnName);
                    field.setValue(value != null ? String.valueOf(value) : null);
                }
                MetaField metaField = name2metaFields.get(FormatUtil.parseString(columnName));
                Assert.notNull(
                        metaField,
                        String.format("Can't find meta field for column '%s'", columnName)
                );
                fields.put(metaField.getName(), field);
            }

            Record record = new Record();
            record = record.injectFields(fields);
            record = injectId(record, createRecordId(fields.get(primaryMetaField.getName())));

            result.put(record.getId(), record);
        }

        return Lists.newArrayList(result.values());
    }

    @Override
    public TransferDictionaryDescriptor convert(IDataSet source) {
        Assert.notNull(source, "Source data set must not be NULL");
        try {
            ITableIterator tableIterator = source.iterator();

            ImmutableList.Builder<Dictionary> result = ImmutableList.builder();
            while (tableIterator.next()) {
                ITable table = tableIterator.getTable();
                Dictionary dictionary = dictionaryStructureConverter.convert(table.getTableMetaData());
                dictionary.setRecords(createRecords(dictionary.getDescendants(), table));
                result.add(dictionary);
            }

            return new TransferDictionaryDescriptor(result.build(), false, false);
        } catch (DataSetException ex) {
            throw new TranslateDataException(String.format("Can't convert data set to dictionary. Cause by: %s", ex.getMessage()), ex);
        }
    }

    /**
     * Создает коллекцию строк таблицы на основе записей
     *
     * @param records коллекция записей
     * @param tableMetaData МЕТА-данные таблицы
     * @return Возвращает коллекцию строк таблицы
     */
    private Collection<Object[]> createTableRows(Collection<Record> records, ITableMetaData tableMetaData) throws DataSetException {
        ImmutableList.Builder<Object[]> rowsBuilder = ImmutableList.builder();
        for (Record record : records) {
            List<Object> values = new ArrayList<>(tableMetaData.getColumns().length);
            for (Column column : tableMetaData.getColumns()) {
                Field field = record.retrieveFieldByName(column.getColumnName());
                values.add(field != null ? field.getValue() : null);
            }
            rowsBuilder.add(ArrayUtil.toArray(Object.class, values));
        }
        return rowsBuilder.build();
    }

    @Override
    public IDataSet reverseConvert(TransferDictionaryDescriptor descriptor) {
        Assert.notNull(descriptor, "Target descriptor must not be NULL");
        DefaultDataSet dataSet = new DefaultDataSet();

        for (Dictionary dictionary : descriptor.getContent()) {
            try {
                // Формируем контент справочника
                ITableMetaData tableMetaData = dictionaryStructureConverter.reverseConvert(dictionary);
                DefaultTable table = new DefaultTable(tableMetaData);
                Collection<Record> records = ModelUtils.createRecords(dictionary.getId(), dictionary.getDescendants());
                if (!CollectionUtils.isEmpty(records)) {
                    for (Object[] row : createTableRows(records, tableMetaData)) {
                        table.addRow(row);
                    }
                }
                dataSet.addTable(table);

                // Формируем структуру справочника
                if (descriptor.isWithStructure()) {
                    dataSet.addTable(metaFieldsConverter.reverseConvert(dictionary));
                }
            } catch (DataSetException ex) {
                throw new TranslateDataException(String.format("Can't convert dictionary '%s' to table. Cause by: %s", dictionary.getName(), ex.getMessage()), ex);
            }
        }

        return dataSet;
    }
}
