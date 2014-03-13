package ru.hflabs.rcd.service.document;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Класс <class>ValidatorService</class> реализует базовый сервис валидации сущностей
 *
 * @author Nazin Alexander
 */
public class ChangeValidatorService<T> extends ValidatorService<T> implements Validator {

    /** Делегат валидатора по JSR-303 */
    private Validator delegate;

    public ChangeValidatorService(Class<T> targetClass) {
        super(targetClass);
    }

    public void setDelegate(Validator delegate) {
        this.delegate = delegate;
    }

    /**
     * Выполняет форматирование документа
     *
     * @param target целевой документ
     * @return Возвращает модифицированный документ
     */
    protected T formatValue(T target) {
        return target;
    }

    /**
     * Выполняет валидацию документа по аннотациям
     *
     * @param errors контейнер ошибок
     * @param target проверяемый документ
     * @return Возвращает флаг наличия ошибок
     */
    protected boolean doValidateAnnotations(Errors errors, Object target) {
        delegate.validate(target, errors);
        return errors.hasErrors();
    }

    @Override
    protected void doValidate(Errors errors, T target) {
        T targetDocument = formatValue(target);
        doValidateAnnotations(errors, targetDocument);
    }

    @Override
    public final boolean supports(Class<?> clazz) {
        return retrieveTargetClass().isAssignableFrom(clazz);
    }

    @Override
    public final void validate(Object target, Errors errors) {
        doValidate(errors, retrieveTargetClass().cast(target));
    }
}
