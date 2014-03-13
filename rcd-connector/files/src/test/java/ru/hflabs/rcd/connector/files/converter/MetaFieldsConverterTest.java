package ru.hflabs.rcd.connector.files.converter;

import com.google.common.collect.Lists;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.hflabs.rcd.accessor.Accessors;
import ru.hflabs.rcd.model.MockFactory;
import ru.hflabs.rcd.model.ModelUtils;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.util.spring.core.convert.converter.ReverseConverter;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Test
public class MetaFieldsConverterTest extends DataSetConverterTest {

    /** Сервис конвертации */
    private ReverseConverter<ITable, Dictionary> converter;

    @BeforeClass
    private void createConverter() {
        converter = new MetaFieldsConverter();
    }

    private static void assertMetaField(MetaField actual, ITable table, int index) throws Exception {
        assertEquals(actual.getName(), table.getValue(index, MetaField.NAME));
        assertEquals(actual.getDescription(), table.getValue(index, MetaField.DESCRIPTION));
        assertEquals(actual.getOrdinal(), new Integer(index));
        assertEquals(actual.isFlagEstablished(MetaField.FLAG_PRIMARY), table.getValue(index, MetaField.FLAG_PRIMARY_NAME));
        assertEquals(actual.isFlagEstablished(MetaField.FLAG_UNIQUE), table.getValue(index, MetaField.FLAG_UNIQUE_NAME));
        assertEquals(actual.isFlagEstablished(MetaField.FLAG_HIDDEN), table.getValue(index, MetaField.FLAG_HIDDEN_NAME));
    }

    private static void assertRow(ITable table, int index, MetaField expected) throws Exception {
        assertEquals(table.getValue(index, MetaField.NAME), expected.getName());
        assertEquals(table.getValue(index, MetaField.DESCRIPTION), expected.getDescription());
        assertEquals(table.getValue(index, MetaField.FLAG_PRIMARY_NAME), expected.isFlagEstablished(MetaField.FLAG_PRIMARY));
        assertEquals(table.getValue(index, MetaField.FLAG_UNIQUE_NAME), expected.isFlagEstablished(MetaField.FLAG_UNIQUE));
    }

    public void testConvert() throws Exception {
        DefaultTable table = new DefaultTable(new DefaultTableMetaData(
                "testDictionary" + MetaFieldsConverter.DEFAULT_TABLE_POSTFIX,
                new Column[]{
                        new Column(MetaField.NAME, DataType.UNKNOWN),
                        new Column(MetaField.DESCRIPTION, DataType.UNKNOWN),
                        new Column(MetaField.FLAG_PRIMARY_NAME, DataType.UNKNOWN),
                        new Column(MetaField.FLAG_UNIQUE_NAME, DataType.UNKNOWN),
                        new Column(MetaField.FLAG_HIDDEN_NAME, DataType.UNKNOWN)
                }
        ));
        table.addRow(new Object[]{"mf1", "meta_field_1", true, true, false});
        table.addRow(new Object[]{"mf2", "meta_field_2", false, true, false});
        table.addRow(new Object[]{"mf3", "meta_field_3", false, false, true});

        Dictionary dictionary = converter.convert(table);
        assertEquals(dictionary.getName(), "testDictionary");
        List<MetaField> metaFields = ModelUtils.sortMetaFieldsByOrdinal(dictionary.getDescendants());
        // check meta fields
        assertEquals(metaFields.size(), 3);
        for (int i = 0; i < table.getRowCount(); i++) {
            assertMetaField(metaFields.get(i), table, i);
        }
        // check serialize
        assertObjectSerialization(dictionary);
    }

    public void testReverseConvert() throws Exception {
        Dictionary dictionary = MockFactory.createMockDictionary(null);
        MetaField metaField1 = MockFactory.createMockMetaField(dictionary);
        {
            metaField1.changeFlag(true, MetaField.FLAG_PRIMARY);
            metaField1.setOrdinal(0);
        }
        MetaField metaField2 = MockFactory.createMockMetaField(dictionary);
        {
            metaField2.changeFlag(true, MetaField.FLAG_UNIQUE);
            metaField2.setOrdinal(1);
        }
        MetaField metaField3 = MockFactory.createMockMetaField(dictionary);
        {
            metaField3.setDescription(null);
            metaField3.setOrdinal(2);
        }
        dictionary = Accessors.linkDescendants(dictionary, Lists.newArrayList(metaField2, metaField1, metaField3));

        ITable table = converter.reverseConvert(dictionary);
        assertNotNull(table);
        ITableMetaData tableMetaData = table.getTableMetaData();
        assertEquals(tableMetaData.getTableName(), dictionary.getName() + MetaFieldsConverter.DEFAULT_TABLE_POSTFIX);
        // check columns
        {
            assertEquals(tableMetaData.getColumns().length, 5);
            assertEquals(tableMetaData.getColumns()[0].getColumnName(), MetaField.NAME);
            assertEquals(tableMetaData.getColumns()[1].getColumnName(), MetaField.DESCRIPTION);
            assertEquals(tableMetaData.getColumns()[2].getColumnName(), MetaField.FLAG_PRIMARY_NAME);
            assertEquals(tableMetaData.getColumns()[3].getColumnName(), MetaField.FLAG_UNIQUE_NAME);
            assertEquals(tableMetaData.getColumns()[4].getColumnName(), MetaField.FLAG_HIDDEN_NAME);
        }
        // check rows
        {
            assertEquals(table.getRowCount(), 3);
            assertRow(table, 0, metaField1);
            assertRow(table, 1, metaField2);
            assertRow(table, 2, metaField3);
        }
    }
}
