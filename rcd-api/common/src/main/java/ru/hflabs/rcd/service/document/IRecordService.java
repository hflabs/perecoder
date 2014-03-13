package ru.hflabs.rcd.service.document;

import ru.hflabs.rcd.RoleNames;
import ru.hflabs.rcd.model.criteria.FilterCriteria;
import ru.hflabs.rcd.model.criteria.FilterResult;
import ru.hflabs.rcd.model.document.Record;
import ru.hflabs.rcd.service.IChangeService;

import javax.annotation.security.RolesAllowed;
import java.util.Collection;
import java.util.Set;

/**
 * Интерфейс <class>IRecordService</class> декларирует методы для работы с записями справочника
 *
 * @see Record
 */
public interface IRecordService extends IChangeService<Record> {

    /**
     * @param dictionaryId идентификатор справочника
     * @param id идентификатор записи
     * @param fillTransitive флаг необходимости заполнения транзитивных зависимостей
     * @param quietly флаг безопасной проверки существования записи
     * @return Возвращает запись по ее идентификатору или <code>NULL</code>, если установлен флаг <i>quietly</i> и такой не найдено
     */
    @RolesAllowed(RoleNames.OPERATOR)
    Record findByID(String dictionaryId, String id, boolean fillTransitive, boolean quietly);

    /**
     * @param dictionaryId идентификатор справочника
     * @param ids коллекция идентификаторов записей
     * @param fillTransitive флаг необходимости заполнения транзитивных зависимостей
     * @param quietly флаг безопасной проверки существования записей
     * @return Возвращает записи по их идентификаторам или <code>NULL</code>, если установлен флаг <i>quietly</i> и таких записей не найдено
     */
    @RolesAllowed(RoleNames.OPERATOR)
    Collection<Record> findByIDs(String dictionaryId, Set<String> ids, boolean fillTransitive, boolean quietly);

    /**
     * @param dictionaryId идентификатор справочника
     * @param fillTransitive флаг необходимости заполнения транзитивных зависимостей
     * @return Возвращает коллекцию всех записей справочника
     */
    @RolesAllowed(RoleNames.OPERATOR)
    Collection<Record> findAllRecords(String dictionaryId, boolean fillTransitive);

    /**
     * @param dictionaryId идентификатор справочника
     * @param criteria критерий поиска
     * @param fillTransitive флаг необходимости заполнения транзитивных зависимостей
     * @return Возвращает результат фильтрации записей справочника
     */
    @RolesAllowed(RoleNames.OPERATOR)
    FilterResult<Record> findRecordsByCriteria(String dictionaryId, FilterCriteria criteria, boolean fillTransitive);

    /**
     * Закрывает записи справочника по их идентификаторам
     *
     * @param dictionaryId идентификатор справочника
     * @param ids коллекция идентификаторов записей
     */
    @RolesAllowed(RoleNames.ADMINISTRATOR)
    void closeByIDs(String dictionaryId, Set<String> ids);
}
