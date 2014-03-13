package ru.hflabs.rcd.history;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.testng.annotations.Test;
import ru.hflabs.rcd.ServiceTest;
import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.change.Diff;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.model.task.TaskDescriptor;
import ru.hflabs.rcd.service.IDifferenceService;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Map;

import static org.testng.Assert.*;

@Test
public class DifferenceServiceTest extends ServiceTest {

    @Resource(name = "differenceService")
    private IDifferenceService<Identifying> differenceService;

    public void testGroup1() {
        Group group1 = new Group();
        {
            group1.setId("ID1");
            group1.setHistoryId("HID1");
            group1.setName("group1");
            group1.setDescription("group1 test");
        }
        Group group2 = new Group();
        {
            group2.setId("ID2");
            group2.setHistoryId("HID2");
            group2.setName("group2");
            group2.setDescription("group2 test");
        }

        assertNotEquals(
                differenceService.createHashCode(group1),
                differenceService.createHashCode(group2)
        );
        Collection<Diff> diffs = differenceService.createDiff(group1, group2);
        assertNotNull(diffs);
        assertEquals(diffs.size(), 2);
        Map<String, Diff> name2diff = Maps.newLinkedHashMap(Maps.uniqueIndex(diffs, new Function<Diff, String>() {
            @Override
            public String apply(Diff input) {
                return input.getField();
            }
        }));
        assertNotNull(name2diff.remove(Group.NAME));
        assertNotNull(name2diff.remove(Group.DESCRIPTION));
    }

    public void testGroup2() {
        Group group1 = new Group();
        group1.setId("ID1");
        group1.setHistoryId("HID1");
        group1.setName("group1");
        group1.setDescription("group1 test");

        Group group2 = group1.copy();
        group2.setId(null);
        group2.setHistoryId(null);

        assertEquals(
                differenceService.createHashCode(group1),
                differenceService.createHashCode(group2)
        );
        Collection<Diff> diffs = differenceService.createDiff(group1, group2);
        assertNull(diffs);
    }

    public void testTaskDescriptor() {
        TaskDescriptor taskDescriptor1 = new TaskDescriptor();
        taskDescriptor1.setParameters(
                ImmutableMap.<String, Object>builder()
                        .put("string", "1")
                        .put("integer", 1)
                        .build()
        );
        TaskDescriptor taskDescriptor2 = new TaskDescriptor();
        taskDescriptor2.setParameters(
                ImmutableMap.<String, Object>builder()
                        .put("string", "2")
                        .put("integer", 2)
                        .build()
        );
        assertNotEquals(
                differenceService.createHashCode(taskDescriptor1),
                differenceService.createHashCode(taskDescriptor2)
        );
        Collection<Diff> diffs = differenceService.createDiff(taskDescriptor1, taskDescriptor2);
        assertNotNull(diffs);
        assertEquals(diffs.size(), 1);
        Map<String, Diff> name2diff = Maps.newLinkedHashMap(Maps.uniqueIndex(diffs, new Function<Diff, String>() {
            @Override
            public String apply(Diff input) {
                return input.getField();
            }
        }));
        assertNotNull(name2diff.remove(TaskDescriptor.PARAMETERS));
    }
}
