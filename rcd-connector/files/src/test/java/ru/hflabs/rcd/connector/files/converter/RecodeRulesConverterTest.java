package ru.hflabs.rcd.connector.files.converter;

import com.beust.jcommander.internal.Lists;
import org.dbunit.dataset.*;
import org.dbunit.dataset.datatype.DataType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.hflabs.rcd.model.connector.TransferRuleDescriptor;
import ru.hflabs.rcd.model.path.DictionaryNamedPath;
import ru.hflabs.rcd.model.path.FieldNamedPath;
import ru.hflabs.rcd.model.rule.RecodeRule;
import ru.hflabs.util.spring.core.convert.converter.ReverseConverter;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class RecodeRulesConverterTest extends DataSetConverterTest {

    /** Сервис конвертации */
    private ReverseConverter<IDataSet, TransferRuleDescriptor> converter;

    @BeforeClass
    private void createConverter() {
        converter = new RecodeRulesConverter();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConvertFailed() throws Exception {
        DefaultTable table = new DefaultTable(new DefaultTableMetaData(
                "testDictionary",
                new Column[]{new Column("column", DataType.UNKNOWN)}
        ));
        DefaultDataSet dataSet = new DefaultDataSet(table);
        converter.convert(dataSet);
    }

    @Test
    public void testConvertSuccess() throws Exception {
        DictionaryNamedPath dictionaryNamedPath = new DictionaryNamedPath(null, "testDictionary");
        DefaultTable table = new DefaultTable(new DefaultTableMetaData(
                dictionaryNamedPath.getDictionaryName(),
                new Column[]{new Column("from.c1", DataType.UNKNOWN), new Column("c2", DataType.UNKNOWN), new Column("c3", DataType.UNKNOWN)}
        ));
        table.addRow(new Object[]{"1", "one", "001"});
        table.addRow(new Object[]{"2", "two", "002"});
        table.addRow(new Object[]{"3", null, "002"});
        DefaultDataSet dataSet = new DefaultDataSet(table);
        TransferRuleDescriptor descriptor = converter.convert(dataSet);
        assertNotNull(descriptor);
        List<RecodeRule> recodeRules = Lists.newArrayList(descriptor.getContent());
        assertEquals(recodeRules.size(), 3);
        // check rules
        {
            assertEquals(recodeRules.get(0).getFromNamedPath(), new FieldNamedPath(dictionaryNamedPath, "c1", "1"));
            assertEquals(recodeRules.get(0).getToNamedPath(), new FieldNamedPath(dictionaryNamedPath, "c2", "one"));
        }
        {
            assertEquals(recodeRules.get(1).getFromNamedPath(), new FieldNamedPath(dictionaryNamedPath, "c1", "2"));
            assertEquals(recodeRules.get(1).getToNamedPath(), new FieldNamedPath(dictionaryNamedPath, "c2", "two"));
        }
        {
            assertEquals(recodeRules.get(2).getFromNamedPath(), new FieldNamedPath(dictionaryNamedPath, "c1", "3"));
            assertEquals(recodeRules.get(2).getToNamedPath(), new FieldNamedPath(dictionaryNamedPath, "c2", null));
        }
        // check serialization
        assertObjectSerialization(recodeRules);
    }
}
