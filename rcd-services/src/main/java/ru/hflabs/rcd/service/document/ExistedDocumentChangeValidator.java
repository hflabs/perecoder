package ru.hflabs.rcd.service.document;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import ru.hflabs.rcd.exception.constraint.IllegalPrimaryKeyException;
import ru.hflabs.rcd.model.Identifying;

/**
 * Класс <class>ValidatorService</class> реализует базовый сервис валидации новых сущностей по отношению к существующим
 *
 * @author Nazin Alexander
 */
public abstract class ExistedDocumentChangeValidator<T extends Identifying> extends ChangeValidatorService<T> {

    /** Флаг необходимости существования сущности */
    protected final boolean mustExist;

    public ExistedDocumentChangeValidator(Class<T> targetClass, boolean mustExist) {
        super(targetClass);
        this.mustExist = mustExist;
    }

    /**
     * Выполняет базовую валидацию сущности
     *
     * @param errors контейнер ошибок
     * @param target проверяемая сущность
     */
    protected abstract void doValidateCommon(Errors errors, T target);

    /**
     * Возвращает существующий документ
     *
     * @param target проверяемый документ
     * @param exist флаг необходимости существования сущности
     * @return Возвращает существующий документ или <code>NULL</code>, если такого не существует
     */
    protected abstract T findExisted(T target, boolean exist);

    /**
     * Выполняет отклонение валидации на основании существующего объекта
     *
     * @param errors контейнер ошибок
     * @param target проверяемая сущность
     * @param existed существующая сущность
     * @param exist флаг необходимости существования сущности
     */
    protected abstract void rejectExisted(Errors errors, T target, T existed, boolean exist);

    /**
     * Выполняет валидацию нового документа относильно старого
     *
     * @param errors контейнер ошибок
     * @param newObject новый документ
     * @param oldObject существующий документ
     */
    protected void validateNewToOld(Errors errors, T newObject, T oldObject) {
        // do nothing
    }

    /**
     * Выполняет валидацию существующей сущности
     *
     * @param errors контейнер ошибок
     * @param target проверяемая сущность
     */
    protected final void doValidateExisted(Errors errors, T target) {
        if (mustExist && !StringUtils.hasText(target.getId())) {
            reject(errors, new IllegalPrimaryKeyException(String.format("'%s' ID must not be NULL or EMPTY", retrieveTargetClass().getSimpleName())));
        }
        // Получаем существующий документ
        T existed = findExisted(target, mustExist);
        // Проверяем валидность
        if (mustExist ^ (existed != null)) {
            rejectExisted(errors, target, existed, mustExist);
        }
        // Выполняем сопоставление нового документа к старому
        if (!errors.hasErrors() && existed != null) {
            validateNewToOld(errors, target, existed);
        }
    }

    @Override
    protected final void doValidate(Errors errors, T target) {
        super.doValidate(errors, target);
        // Выполняем общую валидацию
        if (!errors.hasErrors()) {
            doValidateCommon(errors, target);
        }
        // Если нет ошибок, то проверяем, существование документа
        if (!errors.hasErrors()) {
            doValidateExisted(errors, target);
        }
    }
}
