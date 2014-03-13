package ru.hflabs.rcd.service;

import org.testng.annotations.Test;
import ru.hflabs.rcd.model.document.MetaField;

import static org.testng.Assert.*;
import static ru.hflabs.rcd.model.MockFactory.createMockMetaField;

@Test
public class MergeServicesTest {

    public void testMergeService() {
        MetaField newMetaField = createMockMetaField(null);
        newMetaField.establishFlags(MetaField.FLAG_PRIMARY);
        MetaField oldMetaField = createMockMetaField(null);
        oldMetaField.establishFlags(MetaField.FLAG_UNIQUE);
        IMergeService.Single<MetaField> service = MergeServices.chain(
                MergeServices.<MetaField>copyId(),
                MergeServices.<MetaField>copyName(),
                new MergeServices.MetaFieldFlagsMergeService()
        );
        MetaField result = service.merge(newMetaField, oldMetaField);
        assertNotNull(result);
        assertNotEquals(result, oldMetaField);
        assertEquals(result.getId(), oldMetaField.getId());
        assertEquals(result.getName(), oldMetaField.getName());
        assertEquals(result.getFlags(), oldMetaField.getFlags());
        assertNotEquals(result.getDescription(), oldMetaField.getDescription());
    }
}
