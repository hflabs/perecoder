package ru.hflabs.rcd.backend.console;

import java.util.Collection;

/**
 * Класс <class>RunDescriptor</class> дескриптор выполнения приложения
 *
 * @author Nazin Alexander
 */
public class RunDescriptor {

    /** Коллекция ошибок */
    private Collection<Throwable> errors;

    public RunDescriptor(Collection<Throwable> errors) {
        this.errors = errors;
    }

    public Collection<Throwable> getErrors() {
        return errors;
    }

    public void setErrors(Collection<Throwable> errors) {
        this.errors = errors;
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * Формирует информационное сообщение
     *
     * @return Возвращает информационное сообщение
     */
    public String describe() {
        return String.format("Done. Errors %d", getErrors().size());
    }
}
