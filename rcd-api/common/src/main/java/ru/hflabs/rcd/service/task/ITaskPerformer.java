package ru.hflabs.rcd.service.task;

import java.util.Map;

/**
 * Интерфейс <class>ITaskPerformer</class> декларирует методы исполнителя задачи
 *
 * @see ru.hflabs.rcd.model.task.TaskDescriptor
 * @see ru.hflabs.rcd.model.task.TaskResult
 * @see ITaskProgress
 */
public interface ITaskPerformer {

    /**
     * Возвращает идентификатор исполнителя
     *
     * @return Возвращает идентификатор исполнителя
     */
    String retrieveName();

    /**
     * Выполняет задачу
     *
     * @param progress слушатель прогресса выполнения задачи
     * @param parameters параметры задачи
     * @return Возвращает результат выполнения задачи
     */
    Map<String, Object> performTask(ITaskProgress progress, Map<String, Object> parameters) throws Throwable;
}
