package ru.hflabs.rcd.task.performer;

import ru.hflabs.rcd.model.task.TaskParameterDefinition;
import ru.hflabs.rcd.model.task.TaskResultStatus;

/**
 * Класс <class>TaskResultDetails</class> детали выполнения задачи
 *
 * @author Nazin Alexander
 */
public class TaskResultDetails extends ParametersHolder {

    /** Количество итераций */
    public static final transient TaskParameterDefinition<TaskResultStatus> STATUS = new TaskParameterDefinition<>("status", TaskResultStatus.UNKNOWN);
    /** Сообщение об ошибке */
    public static final transient TaskParameterDefinition<String> ERROR_MESSAGE = new TaskParameterDefinition<>("errorMessage", null);

    /** Ошибка выполнения */
    private transient Throwable throwable;

    public TaskResultStatus getStatus() {
        return retrieveParameter(STATUS.name, TaskResultStatus.class, STATUS.value);
    }

    public void setStatus(TaskResultStatus status) {
        injectParameter(STATUS.name, status);
    }

    public String getErrorMessage() {
        return retrieveParameter(ERROR_MESSAGE.name, String.class, ERROR_MESSAGE.value);
    }

    public void setErrorMessage(String errorMessage) {
        injectParameter(ERROR_MESSAGE.name, errorMessage);
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public void injectThrowable(Throwable targetThrowable) {
        setThrowable(targetThrowable);
        setErrorMessage(targetThrowable != null ? targetThrowable.getMessage() : null);
    }
}
