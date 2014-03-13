package ru.hflabs.rcd.service.document;

import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.service.IDocumentService;
import ru.hflabs.rcd.service.INamedPathService;

/**
 * Интерфейс <class>IGroupService</class> декларирует методы для работы с группами справочников
 *
 * @see Group
 */
public interface IGroupService extends IDocumentService<Group>, INamedPathService<String, Group> {
}
