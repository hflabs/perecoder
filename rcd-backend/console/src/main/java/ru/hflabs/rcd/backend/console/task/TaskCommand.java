package ru.hflabs.rcd.backend.console.task;

import ru.hflabs.rcd.backend.console.Command;
import ru.hflabs.rcd.event.ContextEvent;

/**
 * Интерфейс <class>TaskCommand</class> декларирует методы команды выполнения задачи
 *
 * @author Nazin Alexander
 */
public interface TaskCommand extends Command {

    /**
     * Создает дескриптор выполнения задачи
     *
     * @return Возвращает созданный дескриптор
     */
    ContextEvent createTaskDescriptor();
}
