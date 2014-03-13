package ru.hflabs.rcd.event.task;

import lombok.Getter;
import ru.hflabs.rcd.model.task.TaskProgress;

import java.text.MessageFormat;

/**
 * Класс <class>TaskProgressEvent</class> содержит информацию о событии прогресса выполнения задачи
 *
 * @see TaskEvent
 * @see TaskProgress
 */
@Getter
public class TaskProgressEvent extends TaskEvent {

    private static final long serialVersionUID = 4828663967675062763L;

    /** Прогресс выполняемой задачи */
    private final TaskProgress progress;

    public TaskProgressEvent(Object source, String executionId, String performerName, TaskProgress progress) {
        super(source, executionId, performerName);
        this.progress = progress;
    }

    @Override
    public String identity() {
        String result = super.identity();
        if (progress != null) {
            result = result + ": " + progress.getPercent() + " - " + MessageFormat.format(progress.getStep(), progress.getArguments());
        }
        return result;
    }
}
