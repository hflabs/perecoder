package ru.hflabs.rcd.service.document;

import org.springframework.validation.Errors;
import ru.hflabs.rcd.accessor.RuleFieldAccessor;
import ru.hflabs.rcd.exception.constraint.IllegalPrimaryKeyException;
import ru.hflabs.rcd.model.Essence;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;
import ru.hflabs.rcd.model.rule.RecodeRule;
import ru.hflabs.rcd.model.rule.Rule;
import ru.hflabs.rcd.service.IFilterService;
import ru.hflabs.util.core.FormatUtil;

/**
 * Класс <class>RuleValidator</class> реализует базовый валидатор для правил
 *
 * @author Nazin Alexander
 */
public abstract class RuleChangeValidator<NP extends MetaFieldNamedPath, T extends Essence, R extends Rule<NP, T, R>, RFS extends IFilterService<T>> extends ExistedDocumentChangeValidator<R> {

    /** Сервис установки значения источника поля в правило */
    private final RuleFieldAccessor<NP, T, R> fromInjector;
    /** Сервис установки значения назначения поля в правило */
    private final RuleFieldAccessor<NP, T, R> toInjector;
    /** Класс исключения при некорректном правиле */
    private final Class<? extends Throwable> illegalRuleClass;
    /** Сервис поиска полей */
    protected RFS recodeFieldService;

    protected RuleChangeValidator(
            boolean mustExist,
            Class<R> targetClass,
            RuleFieldAccessor<NP, T, R> fromInjector,
            RuleFieldAccessor<NP, T, R> toInjector,
            Class<? extends Throwable> illegalRuleClass) {
        super(targetClass, mustExist);
        this.fromInjector = fromInjector;
        this.toInjector = toInjector;
        this.illegalRuleClass = illegalRuleClass;
    }

    public void setRecodeFieldService(RFS recodeFieldService) {
        this.recodeFieldService = recodeFieldService;
    }

    /**
     * Выполняет форматирование именованного пути
     *
     * @param target именованный путь
     * @return Возвращает модифицированный именованный путь
     */
    protected NP formatNamedPath(NP target) {
        target.setGroupName(FormatUtil.parseString(target.getGroupName()));
        target.setDictionaryName(FormatUtil.parseString(target.getDictionaryName()));
        target.setFieldName(FormatUtil.parseString(target.getFieldName()));
        return target;
    }

    @Override
    protected boolean doValidateAnnotations(Errors errors, Object target) {
        super.doValidateAnnotations(errors, target);
        R rule = retrieveTargetClass().cast(target);

        // Проверяем поле источника
        errors.pushNestedPath(R.FROM_PATH);
        try {
            rule = rule.injectFromNamedPath(formatNamedPath(rule.getFromNamedPath()));
            super.doValidateAnnotations(errors, rule.getFromNamedPath());
        } finally {
            errors.popNestedPath();
        }
        // Проверяем поле назначения
        errors.pushNestedPath(R.TO_PATH);
        try {
            rule = rule.injectToNamedPath(formatNamedPath(rule.getToNamedPath()));
            super.doValidateAnnotations(errors, rule.getToNamedPath());
        } finally {
            errors.popNestedPath();
        }

        return errors.hasErrors();
    }

    /**
     * Выполняет валидацию поля для правила
     *
     * @param errors контейнер ошибок
     * @param target проверяемое правило
     * @param accessor серис доступа к полям правила
     */
    protected void doValidateField(Errors errors, R target, RuleFieldAccessor<NP, T, R> accessor) {
        String fieldId = accessor.applyRelativeId(target);
        try {
            accessor.inject(target, recodeFieldService.findByID(fieldId, true, false));
        } catch (IllegalPrimaryKeyException ex) {
            rejectValue(errors, RecodeRule.VALUE, ex, fieldId);
        }
    }

    /**
     * Выполняет валидацию полей источника и назначения
     *
     * @param errors контейнер ошибок
     * @param target проверяемое правило
     */
    protected void doValidateFields(Errors errors, R target) {
        // Проверяем поле источника
        errors.pushNestedPath(R.FROM_PATH);
        try {
            doValidateField(errors, target, fromInjector);
        } finally {
            errors.popNestedPath();
        }
        // Проверяем поле назначения
        errors.pushNestedPath(R.TO_PATH);
        try {
            doValidateField(errors, target, toInjector);
        } finally {
            errors.popNestedPath();
        }
    }

    @Override
    protected void doValidateCommon(Errors errors, R target) {
        doValidateFields(errors, target);
    }

    @Override
    protected void rejectExisted(Errors errors, R target, R existed, boolean mustExist) {
        errors.reject(
                illegalRuleClass.getSimpleName(),
                new Object[]{target.getFromNamedPath(), target.getToNamedPath()},
                String.format("%s '%s' to '%s' %s exist",
                        retrieveTargetClass().getSimpleName(),
                        target.getFromNamedPath(), target.getToNamedPath(),
                        mustExist ? "not" : "already"
                )
        );
    }
}
