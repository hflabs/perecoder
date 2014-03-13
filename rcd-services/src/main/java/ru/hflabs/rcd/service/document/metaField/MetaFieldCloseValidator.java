package ru.hflabs.rcd.service.document.metaField;

import org.springframework.validation.Errors;
import ru.hflabs.rcd.exception.constraint.document.PrimaryMetaFieldException;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.service.document.IDictionaryService;
import ru.hflabs.rcd.service.document.ValidatorService;

/**
 * Класс <class>MetaFieldCloseValidator</class> реализует сервис валидации закрытия МЕТА-полей справочника
 *
 * @author Nazin Alexander
 */
public class MetaFieldCloseValidator extends ValidatorService<MetaField> {

    /** Сервис работы со справочниками */
    private IDictionaryService dictionaryService;

    public MetaFieldCloseValidator() {
        super(MetaField.class);
    }

    public void setDictionaryService(IDictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Override
    protected void doValidate(Errors errors, MetaField target) {
        Dictionary dictionary = dictionaryService.findByID(target.getDictionaryId(), true, true);
        if (dictionary != null) {
            doValidatePermissions(errors, dictionary.getRelative());
        }
        if (target.isFlagEstablished(MetaField.FLAG_PRIMARY)) {
            reject(errors, new PrimaryMetaFieldException(target.getName()), target.getName());
        }
    }
}
