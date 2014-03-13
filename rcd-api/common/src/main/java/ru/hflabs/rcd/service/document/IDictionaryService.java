package ru.hflabs.rcd.service.document;

import ru.hflabs.rcd.RoleNames;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.model.path.DictionaryNamedPath;
import ru.hflabs.rcd.service.IDocumentService;
import ru.hflabs.rcd.service.IManyToOneService;
import ru.hflabs.rcd.service.INamedPathService;
import ru.hflabs.util.core.Pair;

import javax.annotation.security.RolesAllowed;
import java.util.Collection;

/**
 * Интерфейс <class>IDictionaryService</class> декларирует методы для работы со справочниками
 *
 * @see Dictionary
 */
public interface IDictionaryService extends IDocumentService<Dictionary>, INamedPathService<DictionaryNamedPath, Dictionary>, IManyToOneService<Dictionary> {

    /**
     * Выполняет поиск поиск справочника по указанному пути и всех групп, которые содержат справочник с таким же именем
     *
     * @param path именованный путь справочника
     * @return Возвращает найденный справочник и коллекция групп
     */
    @RolesAllowed(RoleNames.OPERATOR)
    Pair<Dictionary, Collection<Group>> findMemberGroups(DictionaryNamedPath path, boolean quietly);
}
