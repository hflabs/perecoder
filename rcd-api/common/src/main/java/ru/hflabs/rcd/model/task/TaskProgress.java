package ru.hflabs.rcd.model.task;

import lombok.Getter;

import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Класс <class>TaskProgress</class> описывает прогресс выполнения задачи
 */
@Getter
public class TaskProgress implements Serializable {

    private static final long serialVersionUID = 6116292542038279743L;

    /** Неопределенный прогресс выпонения */
    public static final int MIN_PROGRESS = 0;
    public static final int MAX_PROGRESS = 100;
    public static final int INFINITE_PROGRESS = -1;
    /** Шаги выполения по умолчанию */
    public static final String PENDING_STEP = "TaskProgress.pending";
    public static final String EXECUTING_STEP = "TaskProgress.executing";

    /** Процент выполнения */
    private int percent;
    /** Текущий шаг выполнения */
    private String step;
    /** Код текущего шага */
    private String code;
    /** Аргумерты локализации */
    private Object[] arguments;

    public TaskProgress(String step) {
        this(INFINITE_PROGRESS, step, step);
    }

    public TaskProgress(int percent, String step, String code, Object... arguments) {
        this.percent = percent;
        this.step = step;
        this.code = code;
        this.arguments = arguments;
    }

    @XmlTransient
    public String getCode() {
        return code;
    }

    @XmlTransient
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public int hashCode() {
        int result = percent;
        result = 31 * result + step.hashCode();
        result = 31 * result + code.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TaskProgress progress = (TaskProgress) o;

        if (percent != progress.percent) {
            return false;
        }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(arguments, progress.arguments)) {
            return false;
        }
        if (!code.equals(progress.code)) {
            return false;
        }
        if (!step.equals(progress.step)) {
            return false;
        }

        return true;
    }
}
