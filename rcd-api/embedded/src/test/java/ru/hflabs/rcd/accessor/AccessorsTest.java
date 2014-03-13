package ru.hflabs.rcd.accessor;

import org.testng.annotations.Test;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.rule.RecodeRule;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static ru.hflabs.rcd.accessor.Accessors.*;
import static ru.hflabs.rcd.model.MockFactory.*;
import static ru.hflabs.rcd.model.ModelUtils.createFieldNamedPath;
import static ru.hflabs.rcd.model.ModelUtils.createMetaFieldNamedPath;

@Test
public class AccessorsTest {

    public void test_IDENTITY() {
        Group firstGroup = createMockGroup();
        Group secondGroup = createMockGroup();
        Group actual = Accessors.<Group>identity().inject(firstGroup, secondGroup);
        assertEquals(actual, secondGroup);
    }

    public void test_RULE_SET_INJECTOR() {
        Group fromGroup = createMockGroup();
        Dictionary fromDictionary = createMockDictionary(fromGroup);
        MetaField fromMetaField = createMockMetaField(fromDictionary);

        Group toGroup = createMockGroup();
        Dictionary toDictionary = createMockDictionary(toGroup);
        MetaField toMetaField = createMockMetaField(toDictionary);

        RecodeRuleSet ruleSet = createMockRecodeRuleSet(fromMetaField, toMetaField, null);
        // NamedPath
        assertEquals(FROM_SET_INJECTOR.applyNamedPath(ruleSet), createMetaFieldNamedPath(fromMetaField));
        assertEquals(TO_SET_INJECTOR.applyNamedPath(ruleSet), createMetaFieldNamedPath(toMetaField));
        // FieldID
        assertEquals(FROM_SET_INJECTOR.applyRelativeId(ruleSet), ruleSet.getFromFieldId());
        assertEquals(TO_SET_INJECTOR.applyRelativeId(ruleSet), ruleSet.getToFieldId());
        // Field
        assertEquals(FROM_SET_INJECTOR.apply(ruleSet), fromMetaField);
        assertEquals(TO_SET_INJECTOR.apply(ruleSet), toMetaField);
    }

    public void test_RULE_INJECTOR() {
        Group fromGroup = createMockGroup();
        Dictionary fromDictionary = createMockDictionary(fromGroup);
        MetaField fromMetaField = createMockMetaField(fromDictionary);
        Field fromField = createMockField(fromMetaField);

        Group toGroup = createMockGroup();
        Dictionary toDictionary = createMockDictionary(toGroup);
        MetaField toMetaField = createMockMetaField(toDictionary);
        Field toField = createMockField(toMetaField);

        RecodeRuleSet ruleSet = createMockRecodeRuleSet(fromMetaField, toMetaField, null);
        RecodeRule rule = createMockRecodeRule(ruleSet, fromField, toField);
        // NamedPath
        assertEquals(FROM_RULE_INJECTOR.applyNamedPath(rule), createFieldNamedPath(fromField));
        assertEquals(TO_RULE_INJECTOR.applyNamedPath(rule), createFieldNamedPath(toField));
        // FieldID
        assertEquals(FROM_RULE_INJECTOR.applyRelativeId(rule), rule.getFromFieldId());
        assertEquals(TO_RULE_INJECTOR.applyRelativeId(rule), rule.getToFieldId());
        // Field
        assertEquals(FROM_RULE_INJECTOR.apply(rule), fromField);
        assertEquals(TO_RULE_INJECTOR.apply(rule), toField);
    }

    public void test_GROUP_BY_DICTIONARY_INJECTOR() {
        Group group = createMockGroup();
        Dictionary dictionary = createMockDictionary(null);
        Dictionary actual = GROUP_TO_DICTIONARY_INJECTOR.inject(dictionary, group);
        assertNotNull(actual);
        assertEquals(GROUP_TO_DICTIONARY_INJECTOR.apply(actual), group);
    }

    public void test_DICTIONARY_BY_META_FIELD_INJECTOR() {
        Dictionary dictionary = createMockDictionary(createMockGroup());
        MetaField metaField = createMockMetaField(null);
        MetaField actual = DICTIONARY_TO_META_FIELD_INJECTOR.inject(metaField, dictionary);
        assertNotNull(actual);
        assertEquals(DICTIONARY_TO_META_FIELD_INJECTOR.apply(actual), dictionary);
    }

    public void test_GROUP_BY_META_FIELD_INJECTOR() {
        Group group = createMockGroup();
        MetaField metaField = createMockMetaField(createMockDictionary(createMockGroup()));
        MetaField actual = GROUP_TO_META_FIELD_INJECTOR.inject(metaField, group);
        assertNotNull(actual);
        assertEquals(GROUP_TO_META_FIELD_INJECTOR.apply(actual), group);
    }
}
