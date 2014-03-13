package ru.hflabs.rcd.connector.files.converter;

import com.google.common.collect.Lists;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.hflabs.rcd.accessor.Accessors;
import ru.hflabs.rcd.model.ModelUtils;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.util.spring.core.convert.converter.ReverseConverter;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static ru.hflabs.rcd.model.MockFactory.createMockDictionary;
import static ru.hflabs.rcd.model.MockFactory.createMockMetaField;

@Test
public class DictionaryStructureConverterTest extends DataSetConverterTest {

    /** Сервис конвертации */
    private ReverseConverter<ITableMetaData, Dictionary> converter;

    @BeforeClass
    private void createConverter() {
        converter = new DictionaryStructureConverter();
    }

    private static void assertMetaField(MetaField actual, Column[] columns, int index, boolean isPrimary) throws Exception {
        assertEquals(actual.getName(), columns[index].getColumnName());
        assertEquals(actual.getOrdinal(), new Integer(index));
        assertEquals(actual.isFlagEstablished(MetaField.FLAG_PRIMARY), isPrimary);
        assertEquals(actual.isFlagEstablished(MetaField.FLAG_UNIQUE), isPrimary);
    }

    public void testConvert() throws Exception {
        ITableMetaData tableMetaData = new DefaultTableMetaData(
                "testDictionary",
                new Column[]{new Column("column1", DataType.UNKNOWN), new Column("column2", DataType.UNKNOWN)}
        );

        Dictionary dictionary = converter.convert(tableMetaData);
        assertNotNull(dictionary);
        assertEquals(dictionary.getName(), "testDictionary");
        // check META-fields
        List<MetaField> metaFields = ModelUtils.sortMetaFieldsByOrdinal(dictionary.getDescendants());
        assertEquals(metaFields.size(), 2);
        for (int i = 0; i < metaFields.size(); i++) {
            assertMetaField(metaFields.get(i), tableMetaData.getColumns(), i, i == 0);
        }
        // check serialize
        assertObjectSerialization(dictionary);
    }

    public void testReverseConvert() throws Exception {
        Dictionary dictionary = createMockDictionary(null);
        MetaField metaField1 = createMockMetaField(dictionary);
        metaField1.setOrdinal(0);
        MetaField metaField2 = createMockMetaField(dictionary);
        metaField2.setOrdinal(1);

        dictionary = Accessors.linkDescendants(dictionary, Lists.newArrayList(metaField1, metaField2));

        ITableMetaData tableMetaData = converter.reverseConvert(dictionary);
        assertEquals(tableMetaData.getColumns().length, 2);
        assertEquals(tableMetaData.getColumns()[0].getColumnName(), metaField1.getName());
        assertEquals(tableMetaData.getColumns()[1].getColumnName(), metaField2.getName());
    }
}
