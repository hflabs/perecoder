package ru.hflabs.rcd.connector.files.converter;

import com.google.common.collect.ImmutableList;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.exception.constraint.DuplicateNameException;
import ru.hflabs.rcd.exception.constraint.IllegalNameException;
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
 * Класс <class>DictionaryStructureConverter</class> реализует конвертацию структуры справочника в {@link ITableMetaData}
 *
 * @author Nazin Alexander
 * @see Dictionary
 */
public class DictionaryStructureConverter implements ReverseConverter<ITableMetaData, Dictionary> {

    @Override
    public Dictionary convert(ITableMetaData source) {
        // Формируем справочник
        Assert.notNull(source, "Source table meta data must not be NULL");
        String dictionaryName = FormatUtil.parseString(source.getTableName());
        Assert.isFalse(
                StringUtils.isEmpty(dictionaryName),
                String.format("Dictionary name must not be empty"),
                IllegalNameException.class
        );
        Dictionary dictionary = injectTrimmedName(new Dictionary(), dictionaryName);

        // Формируем коллекцию МЕТА-полей
        Map<String, MetaField> metaFields = new LinkedHashMap<>();
        try {
            Column[] columns = source.getColumns();
            for (int i = 0; i < columns.length; i++) {
                Column column = columns[i];
                MetaField metaField = injectTrimmedName(new MetaField(), column.getColumnName());
                {
                    metaField.setOrdinal(i);
                    metaField.establishFlags(i == 0 ? MetaField.FLAG_PRIMARY : 0);
                }
                Assert.isNull(
                        metaFields.put(ModelUtils.LOWER_CASE_FUNCTION.apply(metaField.getName()), metaField),
                        String.format("Duplicate meta field name '%s'", metaField.getName()),
                        DuplicateNameException.class
                );
            }
            // Устаналиваем сформированные МЕТА-поля в справочник
            return linkDescendants(dictionary, metaFields.values());
        } catch (DataSetException ex) {
            throw new TranslateDataException(String.format("Can't convert table to dictionary '%s'. Cause by: %s", dictionary.getName(), ex.getMessage()), ex);
        }
    }

    @Override
    public ITableMetaData reverseConvert(Dictionary target) {
        Assert.notNull(target, "Target dictionary must not be NULL");
        Collection<MetaField> metaFields = target.getDescendants();
        Assert.notNull(metaFields, "Target meta fields must not be NULL");

        ImmutableList.Builder<Column> columnBuilder = ImmutableList.builder();
        for (MetaField metaField : metaFields) {
            columnBuilder.add(new Column(metaField.getName(), DataType.VARCHAR));
        }

        return new DefaultTableMetaData(target.getName(), ArrayUtil.toArray(Column.class, columnBuilder.build()));
    }
}
