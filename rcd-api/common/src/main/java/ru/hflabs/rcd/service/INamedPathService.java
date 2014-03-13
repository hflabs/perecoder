package ru.hflabs.rcd.service;

import ru.hflabs.rcd.RoleNames;
import ru.hflabs.rcd.model.Identifying;

import javax.annotation.security.RolesAllowed;

/**
 * Интерфейс <class>INamedPathService</class> декларирует методы получения сущностей по их именованным путям
 *
 * @see ru.hflabs.rcd.model.path.DictionaryNamedPath
 * @see ru.hflabs.rcd.model.Named
 */
public interface INamedPathService<NP, T extends Identifying> {

    /**
     * Возвращает уникальную сущность по ее именованному пути с заполнением транзитивных зависимостей
     *
     * @param path именованный путь сущности
     * @param quietly флаг безопасной проверки существования сущности
     * @return Возвращает найденную сущность или <code>NULL</code>, если установлен флаг <i>quietly</i> и такой сущности не найдено
     */
    @RolesAllowed(RoleNames.OPERATOR)
    T findUniqueByNamedPath(NP path, boolean quietly);
}
