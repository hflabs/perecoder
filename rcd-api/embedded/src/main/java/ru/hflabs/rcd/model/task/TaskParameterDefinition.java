package ru.hflabs.rcd.model.task;

/**
 * Класс <class>TaskParameterDefinition</class> описывает параметр задачи
 */
public class TaskParameterDefinition<T> {

    /** Название параметра */
    public final String name;
    /** Значение параметра */
    public final T value;

    public TaskParameterDefinition(String name, T value) {
        this.name = name;
        this.value = value;
    }
}
