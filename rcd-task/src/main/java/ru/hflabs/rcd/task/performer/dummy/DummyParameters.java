package ru.hflabs.rcd.task.performer.dummy;

import ru.hflabs.rcd.model.task.TaskParameterDefinition;
import ru.hflabs.rcd.task.performer.ParametersHolder;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Класс <class>DummyTaskParameters</class> параметры декоратор параметоров пустой задачи
 *
 * @see ParametersHolder
 */
public class DummyParameters extends ParametersHolder {

    /** Количество итераций */
    public static final transient TaskParameterDefinition<Integer> COUNT = new TaskParameterDefinition<>("count", 1);
    /** Задержка между итерациями в мс */
    public static final transient TaskParameterDefinition<Integer> DELAY = new TaskParameterDefinition<>("delay", 1000);
    /** Прогнозируемая ошибка */
    public static final transient TaskParameterDefinition<String> ERROR_MESSAGE = new TaskParameterDefinition<>("errorMessage", null);

    @Min(1)
    @Max(Integer.MAX_VALUE)
    public int getCount() {
        return retrieveParameter(COUNT.name, Integer.class, COUNT.value);
    }

    public void setCount(int count) {
        injectParameter(COUNT.name, count);
    }

    @Min(1)
    @Max(Integer.MAX_VALUE)
    public int getDelay() {
        return retrieveParameter(DELAY.name, Integer.class, DELAY.value);
    }

    public void setDelay(int delay) {
        injectParameter(DELAY.name, delay);
    }

    public String getErrorMessage() {
        return retrieveParameter(ERROR_MESSAGE.name, String.class, ERROR_MESSAGE.value);
    }

    public void setErrorMessage(String error) {
        injectParameter(ERROR_MESSAGE.name, error);
    }
}
