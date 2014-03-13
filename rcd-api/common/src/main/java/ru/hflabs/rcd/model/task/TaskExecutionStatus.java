package ru.hflabs.rcd.model.task;

/**
 * Класс <class>TaskExecutionStatus</class> реализует перечисление статусов задачи
 *
 * @see TaskResultStatus
 */
public enum TaskExecutionStatus {

    /** Готова к следующему выполнению */
    READY,
    /** Ожидает свободного потока для выполнения */
    PENDING,
    /** Выполняется в штатном режиме */
    RUNNING,
    /** Выполняется до ближайшей точки останова */
    INTERRUPTING
}
