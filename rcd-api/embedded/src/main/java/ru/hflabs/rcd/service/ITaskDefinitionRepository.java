package ru.hflabs.rcd.service;

import ru.hflabs.rcd.model.task.TaskDefinition;
import ru.hflabs.rcd.model.task.TaskDescriptor;

import java.util.Map;

/**
 * Интерфейс <class>ITaskDefinitionRepository</class> декларирует методы репозитория предопределенных задач
 *
 * @see TaskDefinition
 */
public interface ITaskDefinitionRepository extends IStorageService<TaskDefinition>, INamedPathService<String, TaskDefinition> {

    /**
     * Выполняет конвертацию параметров задач
     *
     * @param parametersClass ожидаемый класс задач
     * @param parameters параметры
     * @return Возвращает конвертированные параметры
     */
    <T extends Map<String, Object>> T convertTaskParameters(Class<T> parametersClass, Map<String, Object> parameters);

    /**
     * Выполняет конвертацию результатов задач
     *
     * @param results результаты задачи
     * @return Возвращает конвертированные результаты
     */
    Map<String, Object> convertTaskResults(Map<String, Object> results);

    /**
     * Выполняет установку параметров дескриптора на основе предопределенных
     *
     * @param descriptor дескриптор задачи
     * @return Возвращает модифицированный дескриптор
     */
    TaskDescriptor populateParameters(TaskDescriptor descriptor);

    /**
     * Выполняет расчет следующей даты запуска для дескриптора
     *
     * @param descriptor дескриптор задачи
     * @return Возвращает модифицированный дескриптор с установленной датой следующего запуска или <code>NULL</code>,
     *         если установлен флаг <i>quietly</i> и дату не удалось расчитать
     */
    TaskDescriptor populateScheduleDate(TaskDescriptor descriptor);

    /**
     * Выполняет рассчет и установку динамических параметров
     *
     * @param descriptor дескриптор задачи
     * @return Возвращает модифицированный дескриптор
     */
    TaskDescriptor populate(TaskDescriptor descriptor);
}
