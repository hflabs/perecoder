package ru.hflabs.rcd.service.document.group;

import org.springframework.validation.Errors;
import ru.hflabs.rcd.exception.search.document.UnknownGroupException;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.service.document.IGroupService;
import ru.hflabs.rcd.service.document.NamedDocumentChangeValidator;

/**
 * Класс <class>GroupValidator</class> реализует сервис валидации групп справочников
 *
 * @author Nazin Alexander
 */
public class GroupChangeValidator extends NamedDocumentChangeValidator<Group, IGroupService> {

    public GroupChangeValidator(boolean mustExist) {
        super(Group.class, mustExist, UnknownGroupException.class);
    }

    @Override
    protected Group findUniqueByName(Group target) {
        return getDocumentService().findUniqueByNamedPath(target.getName(), true);
    }

    @Override
    protected void doValidateCommon(Errors errors, Group target) {
        // do nothing
    }

    @Override
    protected void validateNewToOld(Errors errors, Group newObject, Group oldObject) {
        super.validateNewToOld(errors, newObject, oldObject);
        {
            newObject.setOwner(oldObject.getOwner());
            newObject.setPermissions(oldObject.getPermissions());
        }
        doValidatePermissions(errors, oldObject);
    }
}
