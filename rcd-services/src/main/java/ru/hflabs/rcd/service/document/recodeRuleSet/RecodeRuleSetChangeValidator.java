package ru.hflabs.rcd.service.document.recodeRuleSet;

import com.google.common.collect.ImmutableMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import ru.hflabs.rcd.accessor.RuleFieldAccessor;
import ru.hflabs.rcd.exception.constraint.DuplicateNameException;
import ru.hflabs.rcd.exception.constraint.IllegalPrimaryKeyException;
import ru.hflabs.rcd.exception.constraint.document.IllegalFieldException;
import ru.hflabs.rcd.exception.constraint.rule.IllegalChangeMetaFieldException;
import ru.hflabs.rcd.exception.constraint.rule.IllegalRecodeRuleSetException;
import ru.hflabs.rcd.exception.constraint.rule.SelfMappingException;
import ru.hflabs.rcd.exception.search.document.UnknownDictionaryException;
import ru.hflabs.rcd.exception.search.document.UnknownGroupException;
import ru.hflabs.rcd.exception.search.document.UnknownMetaFieldException;
import ru.hflabs.rcd.model.ModelUtils;
import ru.hflabs.rcd.model.criteria.FilterCriteriaValue;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;
import ru.hflabs.rcd.service.IFilterService;
import ru.hflabs.rcd.service.ServiceUtils;
import ru.hflabs.rcd.service.document.IMetaFieldService;
import ru.hflabs.rcd.service.document.RuleChangeValidator;
import ru.hflabs.rcd.service.rule.IRecodeRuleSetService;
import ru.hflabs.util.core.EqualsUtil;
import ru.hflabs.util.core.FormatUtil;

import javax.validation.constraints.NotNull;

import static org.springframework.validation.ValidationUtils.rejectIfEmptyOrWhitespace;
import static ru.hflabs.rcd.accessor.Accessors.FROM_SET_INJECTOR;
import static ru.hflabs.rcd.accessor.Accessors.TO_SET_INJECTOR;

/**
 * Класс <class>RecodeRuleSetValidator</class> реализует базовый валидатор для набора правил перекодирования
 *
 * @author Nazin Alexander
 */
public abstract class RecodeRuleSetChangeValidator extends RuleChangeValidator<MetaFieldNamedPath, MetaField, RecodeRuleSet, IMetaFieldService> {

    /** Сервис работы со значениями полей */
    private IFilterService<Field> fieldService;
    /** Сервис работы с наборами правил перекодирования */
    protected IRecodeRuleSetService recodeRuleSetService;

    public RecodeRuleSetChangeValidator(boolean mustExist) {
        super(mustExist, RecodeRuleSet.class, FROM_SET_INJECTOR, TO_SET_INJECTOR, IllegalRecodeRuleSetException.class);
    }

    public void setFieldService(IFilterService<Field> fieldService) {
        this.fieldService = fieldService;
    }

    public void setRecodeRuleSetService(IRecodeRuleSetService recodeRuleSetService) {
        this.recodeRuleSetService = recodeRuleSetService;
    }

    @Override
    protected RecodeRuleSet formatValue(RecodeRuleSet target) {
        target.setName(FormatUtil.parseString(target.getName()));
        return super.formatValue(target);
    }

    @Override
    protected void doValidateField(Errors errors, RecodeRuleSet target, RuleFieldAccessor<MetaFieldNamedPath, MetaField, RecodeRuleSet> accessor) {
        MetaFieldNamedPath path = accessor.applyNamedPath(target);
        try {
            accessor.inject(target, recodeFieldService.findUniqueByNamedPath(path, false));
        } catch (UnknownGroupException ex) {
            rejectValue(errors, MetaFieldNamedPath.GROUP_NAME, ex, path.getGroupName());
        } catch (UnknownDictionaryException ex) {
            rejectValue(errors, MetaFieldNamedPath.DICTIONARY_NAME, ex, path.getDictionaryName());
        } catch (UnknownMetaFieldException ex) {
            rejectValue(errors, MetaFieldNamedPath.FIELD_NAME, ex, path.getFieldName());
        }
    }

    @Override
    protected void doValidateFields(Errors errors, RecodeRuleSet target) {
        // Выполняем валидацию МЕТА-полей
        super.doValidateFields(errors, target);
        // Проверяем, что поля источника и назначения не равны
        if (!errors.hasErrors() && EqualsUtil.equals(target.getFromDictionaryId(), target.getToDictionaryId())) {
            reject(errors, new SelfMappingException(target.getFromNamedPath()), target.getFromNamedPath());
        }
    }

    /**
     * Выполняет валидацию значения поля по умолчанию
     *
     * @param errors контейнер ошибок
     * @param target проверяемый набор
     */
    private void doValidateDefaultField(Errors errors, RecodeRuleSet target) {
        try {
            // Получаем значение поля по умолчанию по его идентификатору
            Field defaultField = fieldService.findByID(target.getDefaultFieldId(), false, false);
            // Проверяем, что значение поля относится к МЕТА-полю назначения
            if (!EqualsUtil.equals(defaultField.getMetaFieldId(), target.getToFieldId())) {
                rejectValue(
                        errors,
                        RecodeRuleSet.DEFAULT_FIELD_ID,
                        new IllegalFieldException(String.format("Default field '%s' does not belong to meta field %s", defaultField.getValue(), target.getToNamedPath())),
                        defaultField.getValue(),
                        target.getToNamedPath()
                );
            }
        } catch (IllegalPrimaryKeyException ex) {
            reject(errors, ex, target.getDefaultFieldId());
        }
    }

    /**
     * Выполняет валидацию уникальности названия набора правил
     *
     * @param errors контейнер ошибок
     * @param target проверяемый набор
     */
    private void doValidateName(Errors errors, RecodeRuleSet target) {
        // Если набор правил должен существовать, то его имя должно быть задано
        if (mustExist) {
            rejectIfEmptyOrWhitespace(
                    errors,
                    RecodeRuleSet.NAME,
                    NotNull.class.getSimpleName(),
                    String.format("Name for %s must not be empty", retrieveTargetClass().getSimpleName())
            );
        }
        // Выполняем поиск существующего набора с таким же именем
        if (StringUtils.hasText(target.getName())) {
            String targetName = target.getName();
            // Выполняем поиск существующего набора с таким же именем
            RecodeRuleSet existedRuleSet = recodeRuleSetService.findUniqueByNamedPath(targetName, true);
            // Если существует набор с таким же именем, то проверяем, что это тот же самый, что и проверяемый
            if (existedRuleSet != null && !EqualsUtil.equals(target.getId(), existedRuleSet.getId())) {
                rejectValue(
                        errors,
                        RecodeRuleSet.NAME,
                        new DuplicateNameException(String.format("%s with name '%s' already exist", retrieveTargetClass().getSimpleName(), targetName)),
                        targetName
                );
            }
        }
    }

    @Override
    protected void doValidateCommon(Errors errors, RecodeRuleSet target) {
        // Проверяем название набора
        doValidateName(errors, target);
        // Проверяем МЕТА-поля
        super.doValidateCommon(errors, target);
        // Проверяем значение перекодирования по умолчанию
        if (!errors.hasErrors() && target.getDefaultFieldId() != null) {
            doValidateDefaultField(errors, target);
        }
    }

    /**
     * Класс <class>Create</class> реализует сервис валидации набора правил перекодирования при создании
     *
     * @author Nazin Alexander
     */
    public static class Create extends RecodeRuleSetChangeValidator {

        public Create() {
            super(false);
        }

        @Override
        protected RecodeRuleSet findExisted(RecodeRuleSet target, boolean exist) {
            return ServiceUtils.findUniqueDocumentBy(
                    recodeRuleSetService,
                    ImmutableMap.<String, FilterCriteriaValue<?>>of(
                            RecodeRuleSet.FROM_DICTIONARY_ID, new FilterCriteriaValue.StringValue(target.getFromDictionaryId()),
                            RecodeRuleSet.TO_DICTIONARY_ID, new FilterCriteriaValue.StringValue(target.getToDictionaryId())
                    ),
                    false
            );
        }
    }

    /**
     * Класс <class>Update</class> реализует сервис валидации набора правил перекодирования при обновлении
     *
     * @author Nazin Alexander
     */
    public static class Update extends RecodeRuleSetChangeValidator {

        public Update() {
            super(true);
        }

        @Override
        protected RecodeRuleSet findExisted(RecodeRuleSet target, boolean exist) {
            return recodeRuleSetService.findByID(target.getId(), true, true);
        }

        /**
         * Выполняет валидацию смены МЕТА-поля
         *
         * @param errors контейнер ошибок
         * @param newField новое поле
         * @param oldField текущее поле
         */
        private void doValidateChangeMetaField(Errors errors, MetaField newField, MetaField oldField) {
            if (!EqualsUtil.equals(newField.getDictionaryId(), oldField.getDictionaryId())) {
                MetaFieldNamedPath oldPath = ModelUtils.createMetaFieldNamedPath(oldField);
                MetaFieldNamedPath newPath = ModelUtils.createMetaFieldNamedPath(newField);
                reject(errors, new IllegalChangeMetaFieldException(oldPath, newPath), oldPath, newPath);
            }
        }

        @Override
        protected void validateNewToOld(Errors errors, RecodeRuleSet newObject, RecodeRuleSet oldObject) {
            doValidateChangeMetaField(errors, newObject.getFrom(), oldObject.getFrom());
            doValidateChangeMetaField(errors, newObject.getTo(), oldObject.getTo());
        }
    }
}
