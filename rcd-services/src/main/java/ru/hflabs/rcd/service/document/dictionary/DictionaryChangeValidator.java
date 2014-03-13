package ru.hflabs.rcd.service.document.dictionary;

import org.springframework.validation.Errors;
import ru.hflabs.rcd.exception.constraint.IllegalPrimaryKeyException;
import ru.hflabs.rcd.exception.search.document.UnknownDictionaryException;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.service.document.IDictionaryService;
import ru.hflabs.rcd.service.document.IGroupService;
import ru.hflabs.rcd.service.document.NamedDocumentChangeValidator;

import static ru.hflabs.rcd.accessor.Accessors.GROUP_TO_DICTIONARY_INJECTOR;

/**
 * Класс <class>DictionaryValidator</class> реализует базовый сервис валидации справочников
 *
 * @author Nazin Alexander
 */
public class DictionaryChangeValidator extends NamedDocumentChangeValidator<Dictionary, IDictionaryService> {

    /** Сервис работы с группами справочников */
    private IGroupService groupService;

    public DictionaryChangeValidator(boolean mustExist) {
        super(Dictionary.class, mustExist, UnknownDictionaryException.class);
    }

    public void setGroupService(IGroupService groupService) {
        this.groupService = groupService;
    }

    @Override
    protected Dictionary findUniqueByName(Dictionary target) {
        return getDocumentService().findUniqueByRelativeId(target.getGroupId(), target.getName(), false, true);
    }

    @Override
    protected void doValidateCommon(Errors errors, Dictionary target) {
        String relativeId = GROUP_TO_DICTIONARY_INJECTOR.applyRelativeId(target);
        try {
            GROUP_TO_DICTIONARY_INJECTOR.inject(target, groupService.findByID(relativeId, true, false));
            doValidatePermissions(errors, GROUP_TO_DICTIONARY_INJECTOR.apply(target));
        } catch (IllegalPrimaryKeyException ex) {
            reject(errors, ex, relativeId);
        }
    }
}
