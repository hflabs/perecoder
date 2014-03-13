package ru.hflabs.rcd.connector.files.converter;

import com.google.common.collect.Lists;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.hflabs.rcd.model.connector.TransferDictionaryDescriptor;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.document.Record;
import ru.hflabs.rcd.service.ServiceUtils;
import ru.hflabs.util.spring.core.convert.converter.ReverseConverter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;
import static ru.hflabs.rcd.accessor.Accessors.injectName;
import static ru.hflabs.rcd.accessor.Accessors.linkDescendants;
import static ru.hflabs.rcd.model.MockFactory.*;

@Test
public class DictionariesConverterTest extends DataSetConverterTest {

    /** Сервис конвертации */
    private ReverseConverter<IDataSet, TransferDictionaryDescriptor> converter;

    @BeforeClass
    private void createConverter() {
        converter = new DictionariesConverter();
    }

    public void testConvert() throws Exception {
        DefaultTable table = new DefaultTable(new DefaultTableMetaData(
                "testDictionary",
                new Column[]{new Column("column1", DataType.UNKNOWN), new Column("column2", DataType.UNKNOWN)}
        ));
        table.addRow(new Object[]{"r1c1", "r1c2_must_ignore"});
        table.addRow(new Object[]{"r1c1", "r1c2"});
        table.addRow(new Object[]{"r2c1", null});

        TransferDictionaryDescriptor descriptor = converter.convert(new DefaultDataSet(table));
        assertNotNull(descriptor);
        assertFalse(descriptor.isWithStructure());
        assertFalse(descriptor.isWithHidden());
        Collection<Dictionary> dictionaries = descriptor.getContent();
        assertNotNull(dictionaries);
        Dictionary dictionary = ServiceUtils.extractSingleDocument(dictionaries);
        assertNotNull(dictionary);
        // check META-fields
        {
            List<MetaField> metaFields = Lists.newArrayList(dictionary.getDescendants());
            assertEquals(metaFields.size(), 2);
            {
                assertEquals(metaFields.get(0).getName(), "column1");
                assertEquals(metaFields.get(0).getOrdinal(), new Integer(0));
                assertTrue(metaFields.get(0).isFlagEstablished(MetaField.FLAG_PRIMARY));
                assertTrue(metaFields.get(0).isFlagEstablished(MetaField.FLAG_UNIQUE));
            }
            {
                assertEquals(metaFields.get(1).getName(), "column2");
                assertEquals(metaFields.get(1).getOrdinal(), new Integer(1));
                assertFalse(metaFields.get(1).isFlagEstablished(MetaField.FLAG_PRIMARY));
                assertFalse(metaFields.get(1).isFlagEstablished(MetaField.FLAG_UNIQUE));
            }
        }
        // check records
        {
            List<Record> records = Lists.newArrayList(dictionary.getRecords());
            assertEquals(records.size(), 2);
            {
                Map<String, Field> fields = records.get(0).getFields();
                assertEquals(fields.get("column1").getValue(), "r1c1");
                assertEquals(fields.get("column2").getValue(), "r1c2");
            }
            {
                Map<String, Field> fields = records.get(1).getFields();
                assertEquals(fields.get("column1").getValue(), "r2c1");
                assertNull(fields.get("column2").getValue());
            }
        }
        // check serialize
        assertObjectSerialization(dictionaries);
    }

    public void testReverseConvert() throws Exception {
        Dictionary dictionary = createMockDictionary(null);
        // Prepare meta field 1
        MetaField metaField1 = createMockMetaField(dictionary);
        metaField1.setOrdinal(0);
        Field field11 = createMockField(metaField1);
        Field field21 = createMockField(metaField1);
        {
            field21.setValue(null);
        }
        metaField1 = linkDescendants(metaField1, Lists.newArrayList(field11, field21));

        // Prepare meta field 2
        MetaField metaField2 = createMockMetaField(dictionary);
        metaField2.setOrdinal(1);
        Field field12 = injectName(createMockField(metaField2), field11.getName());
        {
            field12.setValue("");
        }
        Field field22 = injectName(createMockField(metaField2), field21.getName());
        metaField2 = linkDescendants(metaField2, Lists.newArrayList(field12, field22));

        // Prepare dictionary
        dictionary = linkDescendants(dictionary, Lists.newArrayList(metaField1, metaField2));

        IDataSet dataSet = converter.reverseConvert(new TransferDictionaryDescriptor(Lists.newArrayList(dictionary), false, false));
        assertEquals(dataSet.getTableNames().length, 1);
        assertEquals(dataSet.getTableNames()[0], dictionary.getName());
        ITable table = dataSet.getTable(dictionary.getName());
        // check META-fields
        {
            ITableMetaData tableMetaData = table.getTableMetaData();
            assertEquals(tableMetaData.getColumns().length, 2);
            assertEquals(tableMetaData.getColumns()[0].getColumnName(), metaField1.getName());
            assertEquals(tableMetaData.getColumns()[1].getColumnName(), metaField2.getName());
        }
        // check records
        {
            assertEquals(table.getRowCount(), 2);
            assertEquals(table.getValue(0, metaField1.getName()), field11.getValue());
            assertEquals(table.getValue(0, metaField2.getName()), field12.getValue());
            assertEquals(table.getValue(1, metaField1.getName()), field21.getValue());
            assertEquals(table.getValue(1, metaField2.getName()), field22.getValue());
        }
    }
}
