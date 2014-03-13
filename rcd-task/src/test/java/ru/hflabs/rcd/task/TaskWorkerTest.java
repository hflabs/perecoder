package ru.hflabs.rcd.task;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.annotations.*;
import ru.hflabs.rcd.event.task.TaskEvent;
import ru.hflabs.rcd.event.task.TaskExecutionEvent;
import ru.hflabs.rcd.event.task.TaskProgressEvent;
import ru.hflabs.rcd.model.task.*;
import ru.hflabs.rcd.service.task.ITaskPerformer;
import ru.hflabs.rcd.service.task.ITaskProgress;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.testng.Assert.*;
import static ru.hflabs.rcd.model.MockFactory.createMockTaskDescriptor;

@Test
public class TaskWorkerTest {

    /** Пул выполнения задач */
    private ExecutorService executorService;
    /** Сервис выполнения задач */
    private ITaskPerformer performer;
    /** Сервис публикации событий */
    private ApplicationEventPublisher eventPublisher;

    @BeforeClass
    private void createExecutorService() {
        executorService = Executors.newSingleThreadExecutor();
        performer = Mockito.mock(ITaskPerformer.class);
        eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
    }

    @AfterClass
    private void disposeExecutorService() {
        executorService.shutdownNow();
        performer = null;
        eventPublisher = null;
    }

    @BeforeMethod
    @AfterMethod
    private void purgeMocks() {
        Mockito.reset(performer, eventPublisher);
    }

    private static void assertTaskProgressEvent(TaskEvent actual, TaskProgressEvent expected) {
        assertTrue(actual instanceof TaskProgressEvent, "Task event not instance of TaskProgressEvent");
        assertEquals(actual.getId(), expected.getId());
        assertEquals(((TaskProgressEvent) actual).getProgress(), expected.getProgress());
    }

    private static void assertTaskExecutionEvent(TaskEvent actual, TaskExecutionEvent expected) {
        assertTrue(actual instanceof TaskExecutionEvent, "Task event not instance of TaskExecutionEvent");
        assertEquals(actual.getId(), expected.getId());
        assertEquals(((TaskExecutionEvent) actual).getStatus(), expected.getStatus());
    }

    private static void assertTaskResult(TaskResult actual, TaskResultStatus expectedStatus) {
        assertNotNull(actual);
        assertNotNull(actual.getId());
        assertNotNull(actual.getDescriptorId());
        assertEquals(actual.getId(), actual.getDescriptorId());
        assertEquals(actual.getOwner(), "owner");
        assertEquals(actual.getAuthor(), "author");
        assertNotNull(actual.getRegistrationDate());
        assertNotNull(actual.getStartDate());
        assertNotNull(actual.getEndDate());
        assertEquals(actual.getStatus(), expectedStatus);
        assertNotNull(actual.getParameters());
    }

    public void testFinishedTaskStatus() throws Throwable {
        final TaskDescriptor descriptor = createMockTaskDescriptor();
        final TaskWorker worker = new TaskWorker("owner", "author", descriptor, performer, eventPublisher);
        executorService.submit(worker).get();
        // check events
        ArgumentCaptor<TaskEvent> eventCaptor = ArgumentCaptor.forClass(TaskEvent.class);
        Mockito.verify(eventPublisher, new Times(5)).publishEvent(eventCaptor.capture());
        List<TaskEvent> events = eventCaptor.getAllValues();
        assertTaskProgressEvent(events.get(0), new TaskProgressEvent(worker, descriptor.getId(), null, new TaskProgress(TaskProgress.PENDING_STEP)));
        assertTaskExecutionEvent(events.get(1), new TaskExecutionEvent(worker, descriptor.getId(), null, TaskExecutionStatus.RUNNING));
        assertTaskProgressEvent(events.get(2), new TaskProgressEvent(worker, descriptor.getId(), null, new TaskProgress(TaskProgress.EXECUTING_STEP)));
        assertTaskProgressEvent(events.get(3), new TaskProgressEvent(worker, descriptor.getId(), null, null));
        assertTaskExecutionEvent(events.get(4), new TaskExecutionEvent(worker, descriptor.getId(), null, TaskExecutionStatus.READY));
        // check result
        assertTaskResult(worker.get(), TaskResultStatus.FINISHED);
    }

    public void testErrorTaskStatus() throws Throwable {
        final TaskDescriptor descriptor = createMockTaskDescriptor();
        final TaskWorker worker = new TaskWorker("owner", "author", descriptor, performer, eventPublisher);

        Mockito.doThrow(
                new UnsupportedOperationException("test error")
        ).when(performer).performTask(Mockito.any(ITaskProgress.class), Mockito.anyMapOf(String.class, Object.class));

        executorService.submit(worker).get();
        // check events
        ArgumentCaptor<TaskExecutionEvent> eventCaptor = ArgumentCaptor.forClass(TaskExecutionEvent.class);
        Mockito.verify(eventPublisher, new Times(5)).publishEvent(eventCaptor.capture());
        List<TaskExecutionEvent> events = eventCaptor.getAllValues();
        assertTaskProgressEvent(events.get(0), new TaskProgressEvent(worker, descriptor.getId(), null, new TaskProgress(TaskProgress.PENDING_STEP)));
        assertTaskExecutionEvent(events.get(1), new TaskExecutionEvent(worker, descriptor.getId(), null, TaskExecutionStatus.RUNNING));
        assertTaskProgressEvent(events.get(2), new TaskProgressEvent(worker, descriptor.getId(), null, new TaskProgress(TaskProgress.EXECUTING_STEP)));
        assertTaskProgressEvent(events.get(3), new TaskProgressEvent(worker, descriptor.getId(), null, null));
        assertTaskExecutionEvent(events.get(4), new TaskExecutionEvent(worker, descriptor.getId(), null, TaskExecutionStatus.READY));
        // check result
        TaskResult result = worker.get();
        assertTaskResult(result, TaskResultStatus.ERROR);
        assertEquals(result.getErrorMessage(), "test error");
    }

    public void testCancelTaskStatus() throws Throwable {
        final TaskDescriptor descriptor = createMockTaskDescriptor();
        final TaskWorker worker = new TaskWorker("owner", "author", descriptor, performer, eventPublisher);

        Mockito.doAnswer(
                new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        worker.cancel(true);
                        return Collections.<String, Object>emptyMap();
                    }
                }
        ).when(performer).performTask(Mockito.any(ITaskProgress.class), Mockito.anyMapOf(String.class, Object.class));

        executorService.submit(worker).get();
        // check events
        ArgumentCaptor<TaskExecutionEvent> eventCaptor = ArgumentCaptor.forClass(TaskExecutionEvent.class);
        Mockito.verify(eventPublisher, new Times(6)).publishEvent(eventCaptor.capture());
        List<TaskExecutionEvent> events = eventCaptor.getAllValues();
        assertTaskProgressEvent(events.get(0), new TaskProgressEvent(worker, descriptor.getId(), null, new TaskProgress(TaskProgress.PENDING_STEP)));
        assertTaskExecutionEvent(events.get(1), new TaskExecutionEvent(worker, descriptor.getId(), null, TaskExecutionStatus.RUNNING));
        assertTaskProgressEvent(events.get(2), new TaskProgressEvent(worker, descriptor.getId(), null, new TaskProgress(TaskProgress.EXECUTING_STEP)));
        assertTaskExecutionEvent(events.get(3), new TaskExecutionEvent(worker, descriptor.getId(), null, TaskExecutionStatus.INTERRUPTING));
        assertTaskProgressEvent(events.get(4), new TaskProgressEvent(worker, descriptor.getId(), null, null));
        assertTaskExecutionEvent(events.get(5), new TaskExecutionEvent(worker, descriptor.getId(), null, TaskExecutionStatus.READY));
        // check result
        assertTaskResult(worker.get(), TaskResultStatus.CANCELED);
    }
}
