package ru.hflabs.rcd.model.path;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

@Test
public class EqualsNamedPathsTest {

    public void testDictionaryNamedPath() {
        assertEquals(new DictionaryNamedPath("group", "DICTIONARY"), new DictionaryNamedPath("GROUP", "dictionary"));
        assertNotEquals(new DictionaryNamedPath("group_1", "DICTIONARY"), new DictionaryNamedPath("GROUP", "dictionary"));
        assertNotEquals(new DictionaryNamedPath("group", "DICTIONARY_1"), new DictionaryNamedPath("GROUP", "dictionary"));
    }

    public void testMetaFieldNamedPath() {
        assertEquals(new MetaFieldNamedPath("group", "DICTIONARY", "metaField"), new MetaFieldNamedPath("GROUP", "dictionary", "METAFIELD"));
        assertNotEquals(new MetaFieldNamedPath("group", "DICTIONARY", "metaField_1"), new MetaFieldNamedPath("GROUP", "dictionary", "METAFIELD"));
    }

    public void testFieldNamedPath() {
        assertEquals(new FieldNamedPath("group", "DICTIONARY", "metaField", "value"), new FieldNamedPath("GROUP", "dictionary", "METAFIELD", "value"));
        assertEquals(new FieldNamedPath("group", "DICTIONARY", "metaField", null), new FieldNamedPath("GROUP", "dictionary", "METAFIELD", null));
        assertNotEquals(new FieldNamedPath("group", "DICTIONARY", "metaField", "value"), new FieldNamedPath("GROUP", "dictionary", "METAFIELD", "VALUE"));
        assertNotEquals(new FieldNamedPath("group", "DICTIONARY", "metaField", null), new FieldNamedPath("GROUP", "dictionary", "METAFIELD", ""));
    }
}
