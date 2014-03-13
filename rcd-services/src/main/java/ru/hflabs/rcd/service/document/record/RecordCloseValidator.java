package ru.hflabs.rcd.service.document.record;

import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Record;
import ru.hflabs.rcd.service.document.ValidatorService;

/**
 * Класс <class>RecordCloseValidator</class> реализует сервис валидации закрытия записи справочника
 *
 * @author Nazin Alexander
 */
public class RecordCloseValidator extends ValidatorService<Record> {

    public RecordCloseValidator() {
        super(Record.class);
    }

    @Override
    protected void doValidate(Errors errors, Record target) {
        Dictionary dictionary = target.getRelative();
        Assert.notNull(dictionary, "Dictionary must not be NULL");
        doValidatePermissions(errors, dictionary.getRelative());
    }
}
