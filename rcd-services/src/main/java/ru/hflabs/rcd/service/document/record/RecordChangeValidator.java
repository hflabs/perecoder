package ru.hflabs.rcd.service.document.record;

import com.google.common.collect.Maps;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import ru.hflabs.rcd.exception.constraint.document.IllegalRecordException;
import ru.hflabs.rcd.exception.constraint.document.IncompleteFieldsException;
import ru.hflabs.rcd.exception.constraint.document.NotUniqueFieldsException;
import ru.hflabs.rcd.exception.search.document.UnknownMetaFieldException;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.document.Record;
import ru.hflabs.rcd.service.document.ChangeValidatorService;
import ru.hflabs.rcd.service.document.IFieldService;
import ru.hflabs.rcd.service.document.IMetaFieldService;

import java.util.Collection;
import java.util.Map;

import static ru.hflabs.rcd.accessor.Accessors.*;
import static ru.hflabs.rcd.model.ModelUtils.*;

/**
 * Класс <class>RecordValidator</class> реализует сервис валидации записи справочника
 *
 * @author Nazin Alexander
 */
public class RecordChangeValidator extends ChangeValidatorService<Record> {

    /** Сервис работы с МЕТА-полями */
    private IMetaFieldService metaFieldService;
    /** Сервис работы с значениями полей */
    private IFieldService fieldService;

    public RecordChangeValidator() {
        super(Record.class);
    }

    public void setMetaFieldService(IMetaFieldService metaFieldService) {
        this.metaFieldService = metaFieldService;
    }

    public void setFieldService(IFieldService fieldService) {
        this.fieldService = fieldService;
    }

    /**
     * Выполняет валидацию МЕТА-полей записи
     *
     * @param errors контейнер ошибок
     * @param target проверяемая запись справочника
     * @param metaFields коллекция существующих МЕТА-полей справочника
     */
    private void doValidateMetaFields(Errors errors, Record target, Collection<MetaField> metaFields) {
        if (!metaFields.isEmpty()) {
            // Получаем первичное МЕТА-поле
            MetaField primaryMetaField = retrievePrimaryMetaField(metaFields);
            // Проверяем права на редактирование
            doValidatePermissions(errors, GROUP_TO_META_FIELD_INJECTOR.apply(primaryMetaField));
            // Проверяем, что значение первичного МЕТА-поля установлено
            Field primaryField = target.retrieveFieldByName(primaryMetaField.getId());
            if (primaryField != null) {
                target.setId(createRecordId(primaryField));
            } else {
                reject(errors, new IncompleteFieldsException(createMetaFieldNamedPath(primaryMetaField)), primaryMetaField.getName());
            }
        } else {
            reject(errors, new IllegalRecordException(String.format("Can't find MetaFields for record in dictionary with ID '%s'", target.getDictionaryId())));
        }
    }

    /**
     * Выполняет валидацию значений полей
     *
     * @param errors контейнер ошибок
     * @param target проверяемая запись
     * @param metaFields коллекция существующих МЕТА-полей справочника
     */
    private void doValidateFields(Errors errors, Record target, Collection<MetaField> metaFields) {
        Map<String, Field> currentFields = Maps.newLinkedHashMap(target.getFields());
        // Проверяем существующие поля
        for (MetaField metaField : metaFields) {
            Field field = currentFields.remove(metaField.getId());
            if (field != null) {
                // Устанавливаем системные атрибуты
                field = linkRelative(metaField, field);
                field = injectName(field, target.getId());
                // Заменяем пустую строку на NULL
                field.setValue(formatFieldValue(field));
                // Выполняем валидацию поля
                doValidateAnnotations(errors, field);
                // Проверяем уникальность значения поля
                if (metaField.isFlagEstablished(MetaField.FLAG_UNIQUE)) {
                    if (!fieldService.isFieldUnique(field)) {
                        rejectValue(errors, metaField.getId(), new NotUniqueFieldsException(createMetaFieldNamedPath(metaField)), metaField.getName(), field.getValue());
                    }
                }
            } else {
                reject(errors, new IncompleteFieldsException(createMetaFieldNamedPath(metaField)), metaField.getName());
            }
        }
        // Проверяем оставшие поля
        for (String unknownField : currentFields.keySet()) {
            reject(errors, new UnknownMetaFieldException(unknownField), unknownField);
        }
    }

    @Override
    protected void doValidate(Errors errors, Record target) {
        super.doValidate(errors, target);
        if (!errors.hasErrors()) {
            // Получаем существующие МЕТА-поля справочника
            Collection<MetaField> metaFields = metaFieldService.findAllByRelativeId(target.getDictionaryId(), null, true);
            // Проверяем значение первичного ключа
            doValidateMetaFields(errors, target, metaFields);
            // Если нет ошибок, то проверяем значение полей
            if (!errors.hasErrors()) {
                Errors fieldErrors = new MapBindingResult(target.getFields(), retrieveTargetClass().getSimpleName());
                doValidateFields(fieldErrors, target, metaFields);
                errors.addAllErrors(fieldErrors);
            }
        }
    }
}
