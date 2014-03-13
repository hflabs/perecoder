package ru.hflabs.rcd.service.document.group;

import org.springframework.util.StringUtils;
import ru.hflabs.rcd.exception.search.document.UnknownGroupException;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.service.document.DocumentServiceTemplate;
import ru.hflabs.rcd.service.document.IGroupService;
import ru.hflabs.util.spring.Assert;

import static ru.hflabs.rcd.model.CriteriaUtils.createCriteriaByIDs;
import static ru.hflabs.rcd.service.ServiceUtils.findUniqueDocumentBy;

/**
 * Класс <class>GroupService</class> реализует сервис работы группами справочников
 *
 * @author Nazin Alexander
 */
public class GroupService extends DocumentServiceTemplate<Group> implements IGroupService {

    public GroupService() {
        super(Group.class);
    }

    @Override
    public Group findUniqueByNamedPath(String path, boolean quietly) {
        Assert.isTrue(StringUtils.hasText(path), "Group name must not be NULL or EMPTY");
        Group result = findUniqueDocumentBy(this, createCriteriaByIDs(Group.NAME, path), true);
        if (result == null && !quietly) {
            throw new UnknownGroupException(path);
        }
        return result;
    }
}
