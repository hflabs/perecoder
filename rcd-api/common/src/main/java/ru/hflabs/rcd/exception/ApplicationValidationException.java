package ru.hflabs.rcd.exception;

import lombok.Getter;
import org.springframework.validation.Errors;

/**
 * Класс <class>ApplicationValidationException</class> реализует исключительную ситуацию, возникающую, если сущность не прошла валидацию
 *
 * @see Errors
 */
@Getter
public class ApplicationValidationException extends ApplicationException {

    private static final long serialVersionUID = 6050007313412333012L;

    /** Контейнер ошибок валидации */
    private final Errors errors;

    public ApplicationValidationException(Errors errors) {
        super(String.format("Found %d errors during validation '%s'", errors.getErrorCount(), errors.getObjectName()));
        this.errors = errors;
    }
}
