package ru.hflabs.rcd.lucene.binder;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.testng.annotations.Test;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.util.lucene.LuceneBinderTransformer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static ru.hflabs.rcd.model.MockFactory.createMockGroup;

public class GroupBinderTransformerTest extends BinderTransformerTest<Group> {

    public GroupBinderTransformerTest() {
        super(Group.class);
    }

    @Test(dependsOnMethods = "testReverseConvert")
    public void testConvert() {
        Group group = createMockGroup();
        Document document = binderTransformer.reverseConvert(group);
        Group result = binderTransformer.convert(document);
        assertNotNull(result);
        assertEqualsGroup(result, group);
        assertEquals(binderTransformer.getPrimaryKey(document), group.getId());
    }

    @Test
    public void testReverseConvert() {
        Group group = createMockGroup();
        Document document = binderTransformer.reverseConvert(group);
        assertNotNull(document);
        {
            assertEquals(document.getFields().size(), 7 + 2);
            assertEquals(document.getField(Group.PRIMARY_KEY).stringValue(), group.getId());
            assertEquals(document.getField(Group.HISTORY_ID).stringValue(), group.getHistoryId());
            assertEquals(document.getField(Group.CHANGE_TYPE).stringValue(), group.getHistory().getEventType().name());
            assertNotNull(document.getField(Group.CHANGE_DATE).stringValue());
            assertEquals(document.getField(Group.NAME).stringValue(), group.getName());
            assertEquals(document.getField(Group.DESCRIPTION).stringValue(), group.getDescription());
            assertEquals(document.getField(Group.OWNER).stringValue(), group.getOwner());
            assertNotNull(document.getField(LuceneBinderTransformer.OBJECT_FIELD));
            assertNotNull(document.getField(LuceneBinderTransformer.DEFAULT_SEARCH_FIELD));
        }
        Term primaryTerm = binderTransformer.getPrimaryKey(group);
        assertNotNull(primaryTerm);
        {
            assertEquals(primaryTerm.field(), Group.PRIMARY_KEY);
            assertEquals(primaryTerm.text(), group.getId());
        }
    }
}
