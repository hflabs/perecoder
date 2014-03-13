package ru.hflabs.rcd.task.repository;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import ru.hflabs.rcd.event.index.IndexRebuildedEvent;
import ru.hflabs.rcd.event.modify.ChangeEvent;
import ru.hflabs.rcd.event.task.TaskScheduleEvent;
import ru.hflabs.rcd.model.change.Predicates;
import ru.hflabs.rcd.model.criteria.FilterCriteria;
import ru.hflabs.rcd.model.criteria.FilterCriteriaValue;
import ru.hflabs.rcd.model.task.TaskDescriptor;
import ru.hflabs.rcd.service.ITaskDefinitionRepository;
import ru.hflabs.rcd.service.document.DocumentServiceTemplate;

import java.util.Collection;

/**
 * Класс <class>TaskDescriptorRepository</class> реализует сервис работы с дескрипторами задач
 *
 * @author Nazin Alexander
 */
public class TaskDescriptorRepository extends DocumentServiceTemplate<TaskDescriptor> {

    /** Сервис работы с репозиторием предопределенных задач */
    private ITaskDefinitionRepository taskDefinitionRepository;
    /** Функция преобразования дескриптора задачи в событие обновления задачи в планировщике */
    private final Function<TaskDescriptor, TaskScheduleEvent> updateScheduleFunction = new Function<TaskDescriptor, TaskScheduleEvent>() {
        @Override
        public TaskScheduleEvent apply(TaskDescriptor input) {
            return new TaskScheduleEvent(TaskDescriptorRepository.this, input.getId(), input.getName(), input.getCron());
        }
    };
    /** Функция преобразования дескриптора задачи в событие отмены задачи в планировщике */
    private final Function<TaskDescriptor, TaskScheduleEvent> cancelScheduleFunction = new Function<TaskDescriptor, TaskScheduleEvent>() {
        @Override
        public TaskScheduleEvent apply(TaskDescriptor input) {
            return new TaskScheduleEvent(TaskDescriptorRepository.this, input.getId(), input.getName(), null);
        }
    };

    public TaskDescriptorRepository() {
        super(TaskDescriptor.class);
    }

    public void setTaskDefinitionRepository(ITaskDefinitionRepository taskDefinitionRepository) {
        this.taskDefinitionRepository = taskDefinitionRepository;
    }

    @Override
    protected Collection<TaskDescriptor> injectTransitiveDependencies(Collection<TaskDescriptor> objects) {
        return super.injectTransitiveDependencies(
                Collections2.transform(objects, new Function<TaskDescriptor, TaskDescriptor>() {
                    @Override
                    public TaskDescriptor apply(TaskDescriptor input) {
                        return taskDefinitionRepository.populate(input);
                    }
                })
        );
    }

    /**
     * Выполняет публикацию события постановки задачи в планировщик
     *
     * @param descriptors коллекция дескрипторов
     */
    private void doPublishTaskScheduleEvent(Collection<TaskDescriptor> descriptors, Function<TaskDescriptor, TaskScheduleEvent> function) {
        for (TaskDescriptor descriptor : descriptors) {
            eventPublisher.publishEvent(function.apply(descriptor));
        }
    }

    /**
     * Выполняем поиск запланированных задач и публикует событие постановки задачи в планировщик
     *
     * @see TaskDescriptor#cron
     */
    private void scheduleExistedTasks() {
        Collection<TaskDescriptor> descriptors = findAllByCriteria(
                new FilterCriteria()
                        .injectFilters(
                                ImmutableMap.<String, FilterCriteriaValue<?>>of(
                                        TaskDescriptor.CRON, FilterCriteriaValue.NOT_EMPTY_VALUE
                                )
                        ),
                false
        );
        doPublishTaskScheduleEvent(descriptors, updateScheduleFunction);
    }

    @Override
    protected void handleSelfChangeEvent(ChangeEvent event) {
        super.handleSelfChangeEvent(event);
        // Выполняем обновление запланированных задач
        switch (event.getChangeType()) {
            case IGNORE: {
                break;
            }
            case SKIP:
            case CREATE:
            case UPDATE:
            case RESTORE: {
                doPublishTaskScheduleEvent(event.getChangedByPredicate(retrieveTargetClass(), Predicates.CHANGE_CRON_PREDICATE), updateScheduleFunction);
                break;
            }
            case CLOSE: {
                doPublishTaskScheduleEvent(event.getChanged(retrieveTargetClass()), cancelScheduleFunction);
                break;
            }
            default: {
                throw new UnsupportedOperationException(
                        String.format("Change event '%s' not supported by '%s'", event.getChangeType(), getClass().getSimpleName())
                );
            }
        }
    }

    @Override
    protected void handleSelfIndexRebuildEvent(IndexRebuildedEvent event) {
        super.handleSelfIndexRebuildEvent(event);
        // Выполняем обновление всех запланированных задач
        scheduleExistedTasks();
    }
}
