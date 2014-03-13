package ru.hflabs.rcd.service.rule;

import ru.hflabs.rcd.RoleNames;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.path.DirectionNamedPath;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;
import ru.hflabs.rcd.service.IDocumentService;
import ru.hflabs.rcd.service.INamedPathService;

import javax.annotation.security.RolesAllowed;
import java.util.Collection;
import java.util.Set;

/**
 * Интерфейс <class>IRecodeRuleSetService</class> декларирует методы для работы с наборами правил перекодирования
 *
 * @see RecodeRuleSet
 */
public interface IRecodeRuleSetService extends IDocumentService<RecodeRuleSet>, INamedPathService<String, RecodeRuleSet>, IRuleService<RecodeRuleSet> {

    /**
     * Выполняет поиск набора правил перекодирования по именованным путям
     *
     * @param fromPath именованный путь исходного справочника
     * @param toPath именованный путь целевого справочника
     * @param fillTransitive флаг необходимости заполнения транзитивных зависимостей
     * @param quietly флаг безопасной проверки существования набора правил
     * @return Возвращает набор правил или <code>NULL</code>, если установлен флаг <i>quietly</i> и такого набора не найдено
     */
    @RolesAllowed(RoleNames.OPERATOR)
    RecodeRuleSet findRecodeRuleSetByNamedPath(MetaFieldNamedPath fromPath, MetaFieldNamedPath toPath, boolean fillTransitive, boolean quietly);

    /**
     * Выполняет поиск наборов правил перекодирования по именованным путям
     *
     * @param rulePaths коллекция именованных направленных путей
     * @param fillTransitive флаг необходимости заполнения транзитивных зависимостей
     * @return Возвращает коллекция найденных наборов
     */
    @RolesAllowed(RoleNames.OPERATOR)
    Collection<RecodeRuleSet> findRecodeRuleSetByNamedPath(Collection<DirectionNamedPath<MetaFieldNamedPath>> rulePaths, boolean fillTransitive);

    /**
     * Выполняет поиск несопоставленных справочников
     * <p/>
     * Справочник считается несопоставленным если:
     * <ul>
     * <li>нет ни одного набора правил перекодирования</li>
     * <li>есть хотя бы один набор перекодировок, где справочник выступает в качестве исходного,
     * при этом нет умолчательного значения,
     * и хотя бы одна запись исходного справочника не сопоставлена
     * </li>
     * </ul>
     *
     * @param groupId идентификатор группы, для которой необходимо найти целевые справочники
     * @param fillTransitive флаг необходимости заполнения транзитивных зависимостей
     * @return Возвращает несопоставленные справочники
     */
    @RolesAllowed(RoleNames.OPERATOR)
    Set<Dictionary> findUnmatchedDictionaries(String groupId, boolean fillTransitive);
}
