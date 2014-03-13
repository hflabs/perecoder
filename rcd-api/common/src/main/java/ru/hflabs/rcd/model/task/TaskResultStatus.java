package ru.hflabs.rcd.model.task;

/**
 * Класс <class>TaskStatus</class> реализует перечисление статусов результата выполнения задачи
 *
 * @see TaskExecutionStatus
 */
public enum TaskResultStatus {

    /** Неизвестно */
    UNKNOWN,
    /** Завершена без ошибок, никаких действий не произведено */
    SKIPPED,
    /** Завершена без ошибок */
    FINISHED,
    /** Завершена с ошибками */
    ERROR,
    /** Прервана */
    CANCELED
}
