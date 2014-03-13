package ru.hflabs.rcd.service.document;

import ru.hflabs.rcd.RoleNames;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;
import ru.hflabs.rcd.service.IDocumentContextService;
import ru.hflabs.rcd.service.IDocumentService;
import ru.hflabs.rcd.service.IManyToOneService;

import javax.annotation.security.RolesAllowed;
import java.util.Collection;
import java.util.Set;

/**
 * Интерфейс <class>IFieldService</class> декларирует методы для работы со значениями полей справочников
 *
 * @see Field
 */
public interface IFieldService extends IDocumentService<Field>, IManyToOneService<Field>, IDocumentContextService<MetaFieldNamedPath> {

    /**
     * @param relativeId идентификатор МЕТА-поля
     * @param names коллекция имен
     * @param fillTransitive флаг необходимости заполнения транзитивных зависимостей
     * @return Возвращает коллекцию полей, которые содержат указанные {@link Field#name имена}
     */
    @RolesAllowed(RoleNames.OPERATOR)
    Collection<Field> findByNames(String relativeId, Set<String> names, boolean fillTransitive);

    /**
     * @param relativeId идентификатор МЕТА-поля
     * @param values коллекция значений
     * @param fillTransitive флаг необходимости заполнения транзитивных зависимостей
     * @return Возвращает коллекцию полей, которые содержат указанные {@link Field#value значения}
     */
    @RolesAllowed(RoleNames.OPERATOR)
    Collection<Field> findByValues(String relativeId, Set<String> values, boolean fillTransitive);

    /**
     * @param metaFieldIDs коллекция идентификаторов МЕТА-полей
     * @param fillTransitive флаг необходимости заполнения транзитивных зависимостей
     * @return Возвращает коллекцию полей, которые привязаны к переданным идентификаторам МЕТА-полей
     */
    @RolesAllowed(RoleNames.OPERATOR)
    Collection<Field> findAllByMetaFields(Collection<String> metaFieldIDs, boolean fillTransitive);

    /**
     * Проверяет и возвращает <code>TRUE</code>, если существует поле, привязанного к указанному МЕТА-полю
     *
     * @param metaFieldId идентификатор МЕТА-поля
     * @param value проверяемое значение
     */
    @RolesAllowed(RoleNames.OPERATOR)
    boolean isFieldExist(String metaFieldId, String value);

    /**
     * Проверяет и возвращает <code>TRUE</code>, если поле уникально
     *
     * @param field проверяемое поле
     */
    @RolesAllowed(RoleNames.OPERATOR)
    boolean isFieldUnique(Field field);

    /**
     * Проверяет и возвращает <code>TRUE</code>, если полей для указанного МЕТА-поля являются уникальными
     *
     * @param metaFieldId идентификатор МЕТА-поля
     */
    @RolesAllowed(RoleNames.OPERATOR)
    boolean isFieldsUnique(String metaFieldId);
}
