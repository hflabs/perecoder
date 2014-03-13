package ru.hflabs.rcd.service.task;

import ru.hflabs.rcd.RoleNames;
import ru.hflabs.rcd.model.task.TaskDescriptor;
import ru.hflabs.rcd.model.task.TaskExecution;
import ru.hflabs.rcd.model.task.TaskResult;
import ru.hflabs.rcd.service.IFindService;

import javax.annotation.security.RolesAllowed;
import java.util.Collection;
import java.util.Set;

/**
 * Интерфейс <class>ITaskLauncher</class> декларирует методы исполнения задач
 *
 * @see TaskResult
 */
public interface ITaskLauncher extends IFindService<TaskExecution> {

    /**
     * @return Возвращает коллекцию текущих выполняемых задач
     */
    @RolesAllowed(RoleNames.OPERATOR)
    Collection<TaskExecution> findExecuted();

    /**
     * Отправляет задачу в очередь на выполнение
     *
     * @param descriptorId идентификатор параметров задачи
     * @return Возвращает задачу поставленную в очередь или предыдущую выполняемую задачу
     */
    @RolesAllowed(RoleNames.ADMINISTRATOR)
    TaskExecution submitAsyncTask(String descriptorId);

    /**
     * Отправляет задачу в очередь на выполнение
     *
     * @param descriptor параметры задачи
     * @return Возвращает задачу поставленную в очередь или предыдущую выполняемую задачу
     */
    @RolesAllowed(RoleNames.ADMINISTRATOR)
    TaskExecution submitAsyncTask(TaskDescriptor descriptor);

    /**
     * Отправляет задачу в очередь на выполнение и дожидается ее результата
     *
     * @param descriptor параметры задачи
     * @return Возвращает выполненную задачу
     */
    @RolesAllowed(RoleNames.ADMINISTRATOR)
    TaskResult submitSyncTask(TaskDescriptor descriptor);

    /**
     * Выполняет отмену выполняемых задач
     *
     * @param resultIDs идентификаторы выполняемых задачи
     * @return Возвращает отмененные задачи
     */
    @RolesAllowed(RoleNames.ADMINISTRATOR)
    Collection<TaskExecution> cancelTask(Set<String> resultIDs);
}
