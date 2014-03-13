package ru.hflabs.rcd.lucene.binder;

import org.apache.lucene.document.Document;
import org.testng.annotations.Test;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.rule.RecodeRule;
import ru.hflabs.util.lucene.LuceneBinderTransformer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static ru.hflabs.rcd.model.MockFactory.*;

public class RecodeRuleBinderTransformerTest extends BinderTransformerTest<RecodeRule> {

    public RecodeRuleBinderTransformerTest() {
        super(RecodeRule.class);
    }

    @Test(dependsOnMethods = "testReverseConvert")
    public void testConvert() {
        // generate from content
        Group fromGroup = createMockGroup();
        Dictionary fromDictionary = createMockDictionary(fromGroup);
        MetaField fromMetaField = createMockMetaField(fromDictionary);
        // generate to content
        Group toGroup = createMockGroup();
        Dictionary toDictionary = createMockDictionary(toGroup);
        MetaField toMetaField = createMockMetaField(toDictionary);

        RecodeRule rule = createMockRecodeRule(
                createMockRecodeRuleSet(fromMetaField, toMetaField, null),
                createMockField(fromMetaField),
                createMockField(toMetaField)
        );
        Document document = binderTransformer.reverseConvert(rule);
        RecodeRule result = binderTransformer.convert(document);
        assertNotNull(result);
        assertEqualsRecodeRule(result, rule);
    }

    @Test
    public void testReverseConvert() {
        // generate from content
        Group fromGroup = createMockGroup();
        Dictionary fromDictionary = createMockDictionary(fromGroup);
        MetaField fromMetaField = createMockMetaField(fromDictionary);
        // generate to content
        Group toGroup = createMockGroup();
        Dictionary toDictionary = createMockDictionary(toGroup);
        MetaField toMetaField = createMockMetaField(toDictionary);

        RecodeRule rule = createMockRecodeRule(
                createMockRecodeRuleSet(fromMetaField, toMetaField, null),
                createMockField(fromMetaField),
                createMockField(toMetaField)
        );
        Document document = binderTransformer.reverseConvert(rule);
        assertNotNull(document);
        int totalFieldsCount = 0;
        // check IDs
        {
            assertEquals(document.getField(RecodeRule.FROM_FIELD_ID).stringValue(), rule.getFromFieldId());
            assertEquals(document.getField(RecodeRule.TO_FIELD_ID).stringValue(), rule.getToFieldId());
            totalFieldsCount += 2;
        }
        totalFieldsCount *= 2;
        // check to content
        {
            assertEquals(document.getField(RecodeRule.VALUE).stringValue(), rule.getFrom().getValue());
            totalFieldsCount += 1;
        }
        // check ids
        {
            assertEquals(document.getField(RecodeRule.PRIMARY_KEY).stringValue(), rule.getId());
            assertEquals(document.getField(RecodeRule.HISTORY_ID).stringValue(), rule.getHistoryId());
            assertEquals(document.getField(RecodeRule.CHANGE_TYPE).stringValue(), rule.getChangeType().name());
            assertNotNull(document.getField(RecodeRule.CHANGE_DATE));
            assertEquals(document.getField(RecodeRule.RECODE_RULE_SET_ID).stringValue(), rule.getRecodeRuleSetId());
            totalFieldsCount += 5;
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
