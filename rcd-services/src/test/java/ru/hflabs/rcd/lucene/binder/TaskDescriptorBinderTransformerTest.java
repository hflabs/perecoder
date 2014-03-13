package ru.hflabs.rcd.lucene.binder;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.testng.annotations.Test;
import ru.hflabs.rcd.model.task.TaskDescriptor;
import ru.hflabs.util.lucene.LuceneBinderTransformer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static ru.hflabs.rcd.model.MockFactory.createMockTaskDescriptor;

public class TaskDescriptorBinderTransformerTest extends BinderTransformerTest<TaskDescriptor> {

    public TaskDescriptorBinderTransformerTest() {
        super(TaskDescriptor.class);
    }

    private static void assertTaskDescriptor(TaskDescriptor actual, TaskDescriptor expected) {
        assertDocumentTemplate(actual, expected);
        assertNamed(actual, expected);
        assertEquals(actual.getDescription(), expected.getDescription());
        assertEquals(actual.getCron(), expected.getCron());
        assertEquals(actual.getParameters(), expected.getParameters());
    }

    @Test(dependsOnMethods = "testReverseConvert")
    public void testConvert() {
        TaskDescriptor taskDescriptor = createMockTaskDescriptor();
        Document document = binderTransformer.reverseConvert(taskDescriptor);
        TaskDescriptor result = binderTransformer.convert(document);
        assertNotNull(result);
        assertTaskDescriptor(result, taskDescriptor);
        assertEquals(binderTransformer.getPrimaryKey(document), taskDescriptor.getId());
    }

    @Test
    public void testReverseConvert() {
        TaskDescriptor taskDescriptor = createMockTaskDescriptor();
        Document document = binderTransformer.reverseConvert(taskDescriptor);
        assertNotNull(document);
        {
            assertEquals(document.getFields().size(), 6 + 2);
            assertEquals(document.getField(TaskDescriptor.PRIMARY_KEY).stringValue(), taskDescriptor.getId());
            assertEquals(document.getField(TaskDescriptor.HISTORY_ID).stringValue(), taskDescriptor.getHistoryId());
            assertEquals(document.getField(TaskDescriptor.CHANGE_TYPE).stringValue(), taskDescriptor.getHistory().getEventType().name());
            assertNotNull(document.getField(TaskDescriptor.CHANGE_DATE).stringValue());
            assertEquals(document.getField(TaskDescriptor.NAME).stringValue(), taskDescriptor.getName());
            assertEquals(document.getField(TaskDescriptor.CRON).stringValue(), taskDescriptor.getCron());
            assertNotNull(document.getField(LuceneBinderTransformer.OBJECT_FIELD));
            assertNotNull(document.getField(LuceneBinderTransformer.DEFAULT_SEARCH_FIELD));
        }
        Term primaryTerm = binderTransformer.getPrimaryKey(taskDescriptor);
        assertNotNull(primaryTerm);
        {
            assertEquals(primaryTerm.field(), TaskDescriptor.PRIMARY_KEY);
            assertEquals(primaryTerm.text(), taskDescriptor.getId());
        }
    }
}
