package ru.hflabs.rcd.service.document;

import org.springframework.util.Assert;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import ru.hflabs.rcd.exception.ApplicationValidationException;
import ru.hflabs.rcd.exception.constraint.IllegalPermissionsException;
import ru.hflabs.rcd.model.Named;
import ru.hflabs.rcd.model.Permissioned;
import ru.hflabs.rcd.service.IValidateService;

import static ru.hflabs.rcd.model.ModelUtils.hasPermission;

/**
 * Класс <class>ValidatorService</class> реализует базовый сервис валидации сущностей
 *
 * @author Nazin Alexander
 */
public abstract class ValidatorService<T> implements IValidateService<T> {

    /** Класс отслеживаемой сущности */
    private final Class<T> targetClass;

    public ValidatorService(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public Class<T> retrieveTargetClass() {
        return targetClass;
    }

    /**
     * Выполняет отклонение валидации сущности
     *
     * @param errors контейнер ошибок
     * @param th исключение, которое привело к отклонению валидации
     * @param arguments дополнительные аргументы
     */
    protected static void reject(Errors errors, Throwable th, Object... arguments) {
        errors.reject(th.getClass().getSimpleName(), arguments, th.getMessage());
    }

    /**
     * Выполняет отклонение поля сущности
     *
     * @param errors контейнер ошибок
     * @param fieldName название поля
     * @param th исключение, которое привело к отклонению валидации
     * @param arguments дополнительные аргументы
     */
    protected static void rejectValue(Errors errors, String fieldName, Throwable th, Object... arguments) {
        errors.rejectValue(fieldName, th.getClass().getSimpleName(), arguments, th.getMessage());
    }

    /**
     * Проверяет права на редактирование объекта
     *
     * @param errors контейнер ошибок
     * @param target проверяемый документ
     */
    protected <T extends Permissioned & Named> void doValidatePermissions(Errors errors, T target) {
        Assert.notNull(target, "Permissioned document must not be NULL");
        if (!hasPermission(target, Permissioned.PERMISSION_WRITE)) {
            reject(
                    errors,
                    new IllegalPermissionsException.IllegalWritePermissionsException(
                            String.format("%s with name '%s' does not have write permission", retrieveTargetClass().getSimpleName(), target.getName())
                    ),
                    target.getName()
            );
        }
    }

    /**
     * Выполняет валидацию объекта
     *
     * @param errors контейнер ошибок валидации
     * @param target целевой объект
     */
    protected abstract void doValidate(Errors errors, T target);

    @Override
    public final Errors checkErrors(T object) {
        Errors errors = new BeanPropertyBindingResult(object, retrieveTargetClass().getSimpleName());
        doValidate(errors, object);
        return errors;
    }

    @Override
    public final T validate(T object) throws ApplicationValidationException {
        Errors errors = checkErrors(object);
        if (errors.hasErrors()) {
            throw new ApplicationValidationException(errors);
        }
        return object;
    }
}
