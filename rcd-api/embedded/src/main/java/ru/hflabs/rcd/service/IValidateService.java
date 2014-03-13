package ru.hflabs.rcd.service;

import org.springframework.validation.Errors;
import ru.hflabs.rcd.exception.ApplicationValidationException;

/**
 * Интерфейс <class>IValidateService</class> декларирует методы сервиса валидации консистентности объекта
 *
 * @see org.springframework.validation.Validator
 * @see @see <a href="http://jcp.org/en/jsr/detail?id=303">JSR-303</a>
 */
public interface IValidateService<T> extends ISingleClassObserver<T> {

    /**
     * Выполняет валидацию объекта с накапливанием ошибок в {@link Errors контейнере}
     *
     * @param object валидируемый объект
     * @return Возвращает контейнер ошибок валидации
     * @see #validate(Object)
     */
    Errors checkErrors(T object);

    /**
     * Выполняет валидацию объекта
     *
     * @param object валидируемый объект
     * @return Возвращает модифицированный объект, прошедший валидацию
     * @throws ApplicationValidationException Исключительная ситуация при валидации объекта
     */
    T validate(T object) throws ApplicationValidationException;
}
