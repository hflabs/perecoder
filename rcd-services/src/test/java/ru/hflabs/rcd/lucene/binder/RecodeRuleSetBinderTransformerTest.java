package ru.hflabs.rcd.lucene.binder;

import com.google.common.collect.Lists;
import org.apache.lucene.document.Document;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;
import ru.hflabs.util.lucene.LuceneBinderTransformer;

import java.util.Iterator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static ru.hflabs.rcd.model.MockFactory.*;

public class RecodeRuleSetBinderTransformerTest extends BinderTransformerTest<RecodeRuleSet> {

    public RecodeRuleSetBinderTransformerTest() {
        super(RecodeRuleSet.class);
    }

    @DataProvider
    private Iterator<Object[]> createTestCases() {
        return Lists.newArrayList(
                new Object[]{Boolean.TRUE},
                new Object[]{Boolean.FALSE}
        ).iterator();
    }

    @Test(
            dataProvider = "createTestCases",
            dependsOnMethods = "testReverseConvert"
    )
    public void testConvert(boolean withDefaultField) {
        // generate from content
        Group fromGroup = createMockGroup();
        Dictionary fromDictionary = createMockDictionary(fromGroup);
        MetaField fromMetaField = createMockMetaField(fromDictionary);
        // generate to content
        Group toGroup = createMockGroup();
        Dictionary toDictionary = createMockDictionary(toGroup);
        MetaField toMetaField = createMockMetaField(toDictionary);

        RecodeRuleSet ruleSet = createMockRecodeRuleSet(
                fromMetaField,
                toMetaField,
                withDefaultField ? createMockField(toMetaField) : null
        );
        Document document = binderTransformer.reverseConvert(ruleSet);
        RecodeRuleSet result = binderTransformer.convert(document);
        assertNotNull(result);
        assertEqualsRecodeRuleSet(result, ruleSet);
    }

    @Test(dataProvider = "createTestCases")
    public void testReverseConvert(boolean withDefaultField) {
        // generate from content
        Group fromGroup = createMockGroup();
        Dictionary fromDictionary = createMockDictionary(fromGroup);
        MetaField fromMetaField = createMockMetaField(fromDictionary);
        // generate to content
        Group toGroup = createMockGroup();
        Dictionary toDictionary = createMockDictionary(toGroup);
        MetaField toMetaField = createMockMetaField(toDictionary);

        RecodeRuleSet ruleSet = createMockRecodeRuleSet(
                fromMetaField,
                toMetaField,
                withDefaultField ? createMockField(toMetaField) : null
        );
        Document document = binderTransformer.reverseConvert(ruleSet);
        assertNotNull(document);
        int totalFieldsCount = 0;
        // check from content
        {
            assertEquals(document.getField(RecodeRuleSet.FROM_GROUP_ID).stringValue(), fromGroup.getId());
            assertEquals(document.getField(RecodeRuleSet.FROM_GROUP_NAME).stringValue(), fromGroup.getName());
            assertEquals(document.getField(RecodeRuleSet.FROM_DICTIONARY_ID).stringValue(), fromDictionary.getId());
            assertEquals(document.getField(RecodeRuleSet.FROM_DICTIONARY_NAME).stringValue(), fromDictionary.getName());
            assertEquals(document.getField(RecodeRuleSet.FROM_FIELD_ID).stringValue(), ruleSet.getFromFieldId());
            assertEquals(document.getField(RecodeRuleSet.FROM_FIELD_NAME).stringValue(), fromMetaField.getName());
            totalFieldsCount += 6;
        }
        // check to content
        {
            assertEquals(document.getField(RecodeRuleSet.TO_GROUP_ID).stringValue(), toGroup.getId());
            assertEquals(document.getField(RecodeRuleSet.TO_GROUP_NAME).stringValue(), toGroup.getName());
            assertEquals(document.getField(RecodeRuleSet.TO_DICTIONARY_ID).stringValue(), toDictionary.getId());
            assertEquals(document.getField(RecodeRuleSet.TO_DICTIONARY_NAME).stringValue(), toDictionary.getName());
            assertEquals(document.getField(RecodeRuleSet.TO_FIELD_ID).stringValue(), ruleSet.getToFieldId());
            assertEquals(document.getField(RecodeRuleSet.TO_FIELD_NAME).stringValue(), toMetaField.getName());
            totalFieldsCount += 6;
        }
        totalFieldsCount *= 2;
        // check ids
        {
            assertEquals(document.getField(RecodeRuleSet.PRIMARY_KEY).stringValue(), ruleSet.getId());
            assertEquals(document.getField(RecodeRuleSet.HISTORY_ID).stringValue(), ruleSet.getHistoryId());
            assertEquals(document.getField(RecodeRuleSet.CHANGE_TYPE).stringValue(), ruleSet.getChangeType().name());
            if (withDefaultField) {
                assertEquals(document.getField(RecodeRuleSet.DEFAULT_FIELD_ID).stringValue(), ruleSet.getDefaultFieldId());
            } else {
                assertEquals(document.getField(RecodeRuleSet.DEFAULT_FIELD_ID).stringValue(), "");
            }
            assertNotNull(document.getField(RecodeRuleSet.CHANGE_DATE));
            assertEquals(document.getField(RecodeRuleSet.NAME).stringValue(), ruleSet.getName());
            totalFieldsCount += 6;
        }
        // commons
        {
            assertNotNull(document.getField(LuceneBinderTransformer.OBJECT_FIELD));
            assertNotNull(document.getField(LuceneBinderTransformer.DEFAULT_SEARCH_FIELD));
            totalFieldsCount += 2;
        }
        assertEquals(document.getFields().size(), totalFieldsCount);
    }
}
