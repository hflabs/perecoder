package ru.hflabs.rcd.service.document.dictionary;

import org.springframework.validation.Errors;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.service.document.IGroupService;
import ru.hflabs.rcd.service.document.ValidatorService;

/**
 * Класс <class>DictionaryCloseValidator</class> реализует сервис валидации закрытия справочников
 *
 * @author Nazin Alexander
 */
public class DictionaryCloseValidator extends ValidatorService<Dictionary> {

    /** Сервис работы с группами справочников */
    private IGroupService groupService;

    public DictionaryCloseValidator() {
        super(Dictionary.class);
    }

    public void setGroupService(IGroupService groupService) {
        this.groupService = groupService;
    }

    @Override
    protected void doValidate(Errors errors, Dictionary target) {
        Group group = groupService.findByID(target.getGroupId(), false, true);
        if (group != null) {
            doValidatePermissions(errors, group);
        }
    }
}
