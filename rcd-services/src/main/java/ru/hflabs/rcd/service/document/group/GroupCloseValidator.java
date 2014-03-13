package ru.hflabs.rcd.service.document.group;

import org.springframework.validation.Errors;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.service.document.ValidatorService;

/**
 * Класс <class>GroupCloseValidator</class> реализует сервис валидации закрытия групп справочников
 *
 * @author Nazin Alexander
 */
public class GroupCloseValidator extends ValidatorService<Group> {

    public GroupCloseValidator() {
        super(Group.class);
    }

    @Override
    protected void doValidate(Errors errors, Group target) {
        doValidatePermissions(errors, target);
    }
}
