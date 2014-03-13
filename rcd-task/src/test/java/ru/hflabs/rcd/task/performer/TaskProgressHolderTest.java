package ru.hflabs.rcd.task.performer;

import org.testng.annotations.Test;

import java.util.Date;

import static org.testng.Assert.assertEquals;

@Test
public class TaskProgressHolderTest {

    private static void assertProgress(TaskProgressHolder actual, float expectedChild, float expectedRoot) {
        assertEquals(actual.currentProgress(), expectedChild);
        assertEquals(actual.totalProgress(), expectedRoot);
    }

    public void testRoot() {
        TaskProgressHolder holder = new TaskProgressHolder(new Date(), getClass().getSimpleName(), 2);
        assertProgress(holder, 0f, 0f);
        assertProgress(holder.nextStep(), 0.5f, 0.5f);
        assertProgress(holder.nextStep(), 1f, 1f);
    }

    public void testChild1() {
        TaskProgressHolder parent = new TaskProgressHolder(new Date(), getClass().getSimpleName(), 2);
        // sub task
        TaskProgressHolder child1 = new TaskProgressHolder(4, parent);
        {
            assertProgress(child1.nextStep(), 0.25f, 0.125f);
            assertProgress(child1.nextStep(), 0.5f, 0.25f);
            assertProgress(child1.nextStep(), 0.75f, 0.375f);
            assertProgress(child1.nextStep(), 1.0f, 0.5f);
        }
        // next step
        assertProgress(parent, 0.5f, 0.5f);
    }

    public void testChild2() {
        TaskProgressHolder parent = new TaskProgressHolder(new Date(), getClass().getSimpleName(), 2);
        assertProgress(parent.nextStep(), 0.5f, 0.5f);
        // first sub task
        TaskProgressHolder child1 = new TaskProgressHolder(4, parent);
        {
            assertProgress(child1.nextStep(), 0.25f, 0.625f);
        }
        // second sub task
        {

            TaskProgressHolder child2 = new TaskProgressHolder(4, child1);
            {
                assertProgress(child2.nextStep(), 0.25f, 0.65625f);
                assertProgress(child2.nextStep(), 0.5f, 0.6875f);
                assertProgress(child2.nextStep(), 0.75f, 0.71875f);
                assertProgress(child2.nextStep(), 1.0f, 0.75f);
            }
        }
        // first sub task
        {
            assertProgress(child1.nextStep(), 0.5f, 0.75f);
        }
        assertProgress(parent, 0.75f, 0.75f);
    }
}
