package ru.hflabs.rcd.service.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hflabs.rcd.model.task.TaskProgress;

import java.text.MessageFormat;

/**
 * Интерфейс <class>ITaskProgress</class> декларирует методы обновления прогресса выполняемой задачи
 *
 * @see TaskProgress
 */
public interface ITaskProgress {

    /** Адаптер логгирования прогресса выполнения */
    ITaskProgress LOGGER_PROGRESS = new LoggerTaskProgressAdapter();

    /**
     * Возвращает флаг, указывающий, что задача прервана
     *
     * @return Возвращает <code>TRUE</code>, если задача прервана
     */
    boolean isTaskCanceled();

    /**
     * Устанавливает текущий прогресс выполняемой задачи
     *
     * @param performerName идентификатор исполнителя
     * @param progress прогресс задачи
     */
    void changeTaskProgress(String performerName, TaskProgress progress);

    /**
     * Класс <class>LoggerTaskProgressAdapter</class> реализует адаптер логирования прогресса выполнения задачи
     *
     * @see ITaskProgress
     */
    final class LoggerTaskProgressAdapter implements ITaskProgress {

        private static final Logger LOG = LoggerFactory.getLogger(ITaskProgress.class);

        @Override
        public boolean isTaskCanceled() {
            return false;
        }

        @Override
        public void changeTaskProgress(String performerName, TaskProgress progress) {
            LOG.info(
                    String.format("Task [%s]: %d - %s", performerName, progress.getPercent(), MessageFormat.format(progress.getStep(), progress.getArguments()))
            );
        }
    }
}
