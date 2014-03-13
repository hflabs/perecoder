package ru.hflabs.rcd.backend.console;

/**
 * Интерфейс <class>Command</class> декларирует методы команды выполнения
 *
 * @author Nazin Alexander
 */
public interface Command {

    /**
     * @return Возвращает уникальное название команды
     */
    String getCommandName();
}
