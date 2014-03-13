package ru.hflabs.rcd.web.controller.task;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.hflabs.rcd.model.Comparators;
import ru.hflabs.rcd.model.criteria.FilterCriteria;
import ru.hflabs.rcd.model.task.*;
import ru.hflabs.rcd.service.IDocumentService;
import ru.hflabs.rcd.service.IFilterService;
import ru.hflabs.rcd.service.IStorageService;
import ru.hflabs.rcd.service.task.ITaskLauncher;
import ru.hflabs.rcd.web.controller.ControllerTemplate;

import javax.annotation.Resource;
import javax.swing.*;
import java.text.MessageFormat;
import java.util.*;

import static ru.hflabs.rcd.accessor.Accessors.injectId;
import static ru.hflabs.rcd.model.CriteriaUtils.createCriteriaByIDs;
import static ru.hflabs.rcd.model.ModelUtils.ID_FUNCTION;
import static ru.hflabs.rcd.service.ServiceUtils.extractSingleDocument;

/**
 * Класс <class>TaskController</class> реализует контроллер управления асинхронными задачами
 *
 * @see TaskDefinition
 * @see TaskDescriptor
 * @see TaskResult
 */
@Controller(TaskController.MAPPING_URI + TaskController.NAME_POSTFIX)
@RequestMapping(TaskController.MAPPING_URI + TaskController.DATA_URI)
public class TaskController extends ControllerTemplate {

    public static final String MAPPING_URI = "tasks";

    /** Репозиторий предопределенных дескрипторов */
    @Resource(name = "taskDefinitionRepository")
    private IStorageService<TaskDefinition> taskDefinitionRepository;
    /** Репозиторий сохраненных дескрипторов */
    @Resource(name = "taskDescriptorRepository")
    private IDocumentService<TaskDescriptor> taskDescriptorRepository;
    /** Репозиторий выполненных задач */
    @Resource(name = "taskResultRepository")
    private IFilterService<TaskResult> taskResultRepository;
    /** Сервис выполнения задач */
    @Resource(name = "taskLauncher")
    private ITaskLauncher taskLauncher;

    /**
     * Выполняет локализацию прогресса задачи
     *
     * @param progress прогресс выполнения задачи
     * @param locale целевая локализация
     * @return Возвращает локализованное значение прогресса
     */
    protected TaskProgress localizedTaskProgress(TaskProgress progress, Locale locale) {
        return progress != null ?
                new TaskProgress(
                        progress.getPercent(),
                        messageSource.getMessage(progress.getCode(), progress.getArguments(), MessageFormat.format(progress.getStep(), progress.getArguments()), locale),
                        progress.getCode(),
                        progress.getArguments()
                ) :
                null;
    }

    /**
     * Выполняет локализацию прогресса задачи
     *
     * @param execution декоратор выполнения задачи
     * @param locale целевая локализация
     * @return Возвращает локализованное значение декоратора
     */
    protected TaskExecution localizedTaskProgress(TaskExecution execution, Locale locale) {
        execution.setProgress(localizedTaskProgress(execution.getProgress(), locale));
        return execution;
    }

    @RequestMapping(value = "/model", method = RequestMethod.GET)
    @ResponseBody
    public Collection<TaskDefinition> createModel() {
        return taskDefinitionRepository.getAll();
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ResponseBody
    public TaskDescriptor createTask(@RequestBody TaskDescriptor descriptor) {
        return createSingleDocument(taskDescriptorRepository, descriptor, true);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public TaskDescriptor updateTask(@PathVariable String id, @RequestBody TaskDescriptor descriptor) {
        return updateSingleDocument(taskDescriptorRepository, injectId(descriptor, id), true);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public void closeTask(@PathVariable String id) {
        taskDescriptorRepository.closeByIDs(Sets.newHashSet(id));
    }

    @RequestMapping(value = "/{id}/execute", method = RequestMethod.PUT)
    @ResponseBody
    public TaskExecution executeTask(@PathVariable String id, Locale locale) {
        return localizedTaskProgress(taskLauncher.submitAsyncTask(id), locale);
    }

    @RequestMapping(value = "/{id}/cancel", method = RequestMethod.PUT)
    @ResponseBody
    public TaskExecution cancelTask(@PathVariable String id, Locale locale) {
        // Выполняем отмену
        TaskExecution execution = extractSingleDocument(taskLauncher.cancelTask(Sets.newHashSet(id)), null);
        // Если задача еще выполнялась, то возвращаем результат
        if (execution != null) {
            return localizedTaskProgress(execution, locale);
        }
        // Если задачи не найдено, то пытаемся определить ее последнее выполнение
        return findLatestTaskExecution(id);
    }

    /**
     * Возвращает последний результат выполнения задачи по ее идентификатору
     *
     * @param id идентификатор выполнения задачи
     * @return Возвращает последний результат выполнения задачи по ее идентификатору или <code>NULL</code>, если задача не выполнялась
     */
    private TaskResult findLatestTaskResult(String id) {
        return extractSingleDocument(
                taskResultRepository.findByCriteria(
                        createCriteriaByIDs(TaskResult.DESCRIPTOR_ID, id)
                                .injectSort(TaskResult.REGISTRATION_DATE, SortOrder.DESCENDING)
                                .injectCount(1),
                        false
                ).getResult(),
                null);
    }

    /**
     * Возвращает декоратор задачи с привязкой к ее последнему выполнению
     *
     * @param id идентификатор выполнения задачи
     * @return Возвращает последний результат выполнения задачи по ее идентификатору или <code>NULL</code>, если задача не выполнялась
     */
    private TaskExecution findLatestTaskExecution(String id) {
        // Если задачи не найдено, то пытаемся определить дескриптор задачи
        TaskDescriptor taskDescriptor = taskDescriptorRepository.findByID(id, true, false);
        // Получаем последнее выпонение задачи
        TaskResult taskResult = findLatestTaskResult(id);
        // Возвращаем результат выполнения задачи
        return new TaskExecution(taskDescriptor, taskResult, TaskExecutionStatus.READY, null);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public TaskExecution getTask(@PathVariable String id, Locale locale) {
        // Получаем существующую выполняемую задачу
        TaskExecution execution = taskLauncher.findByID(id, true, true);
        // Проверяем, что задача выполняется
        if (execution != null) {
            return localizedTaskProgress(execution, locale);
        }
        // Если задачи не найдено, то пытаемся определить ее последнее выполнение
        return findLatestTaskExecution(id);
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public Collection<TaskExecution> getTasks(Locale locale) {
        // Получаем сохраненные дескрипторы задач
        final Map<String, TaskDescriptor> descriptors = Maps.newLinkedHashMap(
                Maps.uniqueIndex(taskDescriptorRepository.findAllByCriteria(new FilterCriteria(), true), ID_FUNCTION)
        );

        // Убираем дескрипторы выполняющихся задач
        final Collection<TaskExecution> currentExecutions = getExecutingTasks(locale);
        for (TaskExecution execution : currentExecutions) {
            descriptors.remove(execution.getId());
        }

        // Выполняем формирование декораторов для выполненных задач
        final List<TaskExecution> result = new LinkedList<>(currentExecutions);
        for (TaskDescriptor taskDescriptor : descriptors.values()) {
            TaskResult executed = findLatestTaskResult(taskDescriptor.getId());
            result.add(new TaskExecution(taskDescriptor, executed, TaskExecutionStatus.READY, null));
        }

        Collections.sort(result, Comparators.IDENTIFYING_COMPARATOR);
        return result;
    }

    @RequestMapping(value = "/executing", method = RequestMethod.GET)
    @ResponseBody
    public Collection<TaskExecution> getExecutingTasks(final Locale locale) {
        List<TaskExecution> result = Lists.newArrayList(
                Collections2.transform(taskLauncher.findExecuted(), new Function<TaskExecution, TaskExecution>() {
                    @Override
                    public TaskExecution apply(TaskExecution input) {
                        return localizedTaskProgress(input, locale);
                    }
                })
        );
        Collections.sort(result, Comparators.IDENTIFYING_COMPARATOR);
        return result;
    }
}
