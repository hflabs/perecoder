package ru.hflabs.rcd.service.document.field;

import org.springframework.validation.Errors;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.service.document.ChangeValidatorService;

/**
 * Класс <class>FieldValidator</class> реализует сервис валидации значений полей справочника
 *
 * @author Nazin Alexander
 */
public final class FieldChangeValidator extends ChangeValidatorService<Field> {

    public FieldChangeValidator() {
        super(Field.class);
    }

    @Override
    protected void doValidate(Errors errors, Field target) {
        // do nothing
    }
}
