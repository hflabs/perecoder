package ru.hflabs.rcd.service.document;

import ru.hflabs.rcd.RoleNames;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.path.DictionaryNamedPath;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;
import ru.hflabs.rcd.service.IDocumentService;
import ru.hflabs.rcd.service.IManyToOneService;
import ru.hflabs.rcd.service.INamedPathService;

import javax.annotation.security.RolesAllowed;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Интерфейс <class>IMetaFieldService</class> декларирует методы для работы с МЕТА-полями справочников
 *
 * @see MetaField
 */
public interface IMetaFieldService extends IDocumentService<MetaField>, INamedPathService<MetaFieldNamedPath, MetaField>, IManyToOneService<MetaField> {

    /**
     * @param path именованный путь справочника
     * @return Возвращает коллекцию МЕТА-полей справочника
     */
    @RolesAllowed(RoleNames.OPERATOR)
    Collection<MetaField> findMetaFieldsByNamedPath(DictionaryNamedPath path);

    /**
     * @param path именнованный путь МЕТА-поля
     * @param quietly флаг безопасной проверки существования поля
     * @return Возвращает МЕТА-поле или <code>NULL</code>, если установлен флаг <i>quietly</i> и такого поля не найдено
     * @see #findUniqueByNamedPath(Object, boolean)
     */
    @RolesAllowed(RoleNames.OPERATOR)
    MetaField findMetaFieldByNamedPath(MetaFieldNamedPath path, boolean quietly);

    /**
     * @param paths именованные пути МЕТА-полей
     * @param quietly флаг безопасной проверки существования поля
     * @return Возвращает коллекцию МЕТА-полей по их именованным путям
     */
    @RolesAllowed(RoleNames.OPERATOR)
    Map<MetaFieldNamedPath, MetaField> findMetaFieldByNamedPath(Set<MetaFieldNamedPath> paths, boolean quietly);

    /**
     * @param dictionaryId идентификатор справочника
     * @param fillTransitive флаг необходимости заполнения транзитивных зависимостей
     * @param quietly флаг безопасной проверки существования поля
     * @return Возвращает {@link MetaField#FLAG_PRIMARY первичное}-поле или <code>NULL</code>, если установлен флаг <i>quietly</i> и такого поля не найдено
     */
    @RolesAllowed(RoleNames.OPERATOR)
    MetaField findPrimaryMetaField(String dictionaryId, boolean fillTransitive, boolean quietly);

    /**
     * @param path именованный путь справочника
     * @param quietly флаг безопасной проверки существования поля
     * @return Возвращает {@link MetaField#FLAG_PRIMARY первичное}-поле или <code>NULL</code>, если установлен флаг <i>quietly</i> и такого поля не найдено
     */
    @RolesAllowed(RoleNames.OPERATOR)
    MetaField findPrimaryMetaFieldByNamedPath(DictionaryNamedPath path, boolean quietly);
}
