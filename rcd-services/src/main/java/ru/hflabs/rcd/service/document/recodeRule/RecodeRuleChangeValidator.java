package ru.hflabs.rcd.service.document.recodeRule;

import org.springframework.validation.Errors;
import ru.hflabs.rcd.accessor.RuleFieldAccessor;
import ru.hflabs.rcd.exception.constraint.IllegalPrimaryKeyException;
import ru.hflabs.rcd.exception.constraint.document.IllegalFieldException;
import ru.hflabs.rcd.exception.constraint.rule.IllegalRecodeRuleException;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.path.FieldNamedPath;
import ru.hflabs.rcd.model.rule.RecodeRule;
import ru.hflabs.rcd.service.document.IFieldService;
import ru.hflabs.rcd.service.document.RuleChangeValidator;
import ru.hflabs.rcd.service.rule.IRecodeRuleService;
import ru.hflabs.rcd.service.rule.IRecodeRuleSetService;
import ru.hflabs.util.core.EqualsUtil;

import static ru.hflabs.rcd.accessor.Accessors.FROM_RULE_INJECTOR;
import static ru.hflabs.rcd.accessor.Accessors.TO_RULE_INJECTOR;

/**
 * Класс <class>RecodeRuleValidator</class> реализует базовый валидатор для правила перекодирования
 *
 * @author Nazin Alexander
 */
public class RecodeRuleChangeValidator extends RuleChangeValidator<FieldNamedPath, Field, RecodeRule, IFieldService> {

    /** Сервис работы с наборами правил перекодирования */
    private IRecodeRuleSetService recodeRuleSetService;
    /** Сервис работы с правилами перекодирования */
    private IRecodeRuleService recodeRuleService;

    public RecodeRuleChangeValidator(boolean mustExist) {
        super(mustExist, RecodeRule.class, FROM_RULE_INJECTOR, TO_RULE_INJECTOR, IllegalRecodeRuleException.class);
    }

    public void setRecodeRuleSetService(IRecodeRuleSetService recodeRuleSetService) {
        this.recodeRuleSetService = recodeRuleSetService;
    }

    public void setRecodeRuleService(IRecodeRuleService recodeRuleService) {
        this.recodeRuleService = recodeRuleService;
    }

    @Override
    protected void doValidateField(Errors errors, RecodeRule target, RuleFieldAccessor<FieldNamedPath, Field, RecodeRule> accessor) {
        // Проверяем поле
        super.doValidateField(errors, target, accessor);
        // Проверяем, что значение поля принадлежит МЕТА-полю, по которому производится перекодировка
        if (!errors.hasErrors()) {
            String rrsMetaFieldId = accessor.applyRelativeId(target.getRelative());
            String ruleMetaFieldId = accessor.apply(target).getMetaFieldId();
            if (!EqualsUtil.equals(rrsMetaFieldId, ruleMetaFieldId)) {
                reject(errors, new IllegalFieldException(String.format("Recode rule should belong to META field with ID '%s'", rrsMetaFieldId)));
            }
        }
    }

    /**
     * Выполняет валидацию набора правил перекодирования
     *
     * @param errors контейнер ошибок
     * @param target проверяемое правило
     */
    private void doValidateRuleSet(Errors errors, RecodeRule target) {
        String relativeId = target.getRelativeId();
        try {
            target.injectRecodeRuleSet(recodeRuleSetService.findByID(relativeId, false, false));
        } catch (IllegalPrimaryKeyException ex) {
            reject(errors, ex, relativeId);
        }
    }

    @Override
    protected void doValidateCommon(Errors errors, RecodeRule target) {
        // Проверяем существование набора правил перекодирования
        doValidateRuleSet(errors, target);
        // Проверяем существование значение полей
        if (!errors.hasErrors()) {
            super.doValidateCommon(errors, target);
        }
    }

    @Override
    protected RecodeRule findExisted(RecodeRule target, boolean exist) {
        return recodeRuleService.findUniqueByRelativeId(
                target.getRecodeRuleSetId(),
                target.getFromFieldId(),
                false,
                true);
    }
}
