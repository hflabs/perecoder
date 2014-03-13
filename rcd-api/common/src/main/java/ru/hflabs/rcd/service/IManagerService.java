package ru.hflabs.rcd.service;

import org.springframework.context.ApplicationEvent;
import ru.hflabs.rcd.RoleNames;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.model.path.DictionaryNamedPath;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;

import javax.annotation.security.RolesAllowed;
import java.util.Collection;

/**
 * Интерфейс <class>IManagerService</class> декларирует методы сервиса, для работы с документами
 *
 * @see Group
 * @see ru.hflabs.rcd.model.document.Dictionary
 * @see ru.hflabs.rcd.model.document.Record
 * @see ru.hflabs.rcd.model.rule.RecodeRule
 */
public interface IManagerService {

    /**
     * Выполняет создание/изменение коллекции групп справочников
     *
     * @param groups модифицируемая коллекция групп справочников
     * @return Возвращает модифицированную коллекцию групп справочников
     */
    @RolesAllowed(RoleNames.ADMINISTRATOR)
    Collection<Group> storeGroups(Collection<Group> groups);

    /**
     * Возвращает все группы справочников
     *
     * @param path именованный путь справочника или <code>NULL</code>
     * @return Возвращает все группы справочников
     */
    @RolesAllowed(RoleNames.ADMINISTRATOR)
    Collection<Group> dumpGroups(DictionaryNamedPath path);

    /**
     * Выполняет создание и изменение наборов правил перекодирования
     *
     * @param recodeRuleSets модифицируемые наборы правил
     * @return Возвращает модифицированные правила
     */
    @RolesAllowed(RoleNames.ADMINISTRATOR)
    Collection<RecodeRuleSet> storeRecodeRuleSets(Collection<RecodeRuleSet> recodeRuleSets);

    /**
     * Выполняет растпространение события
     *
     * @param event событие
     */
    @RolesAllowed(RoleNames.ADMINISTRATOR)
    <T extends ApplicationEvent> void propagateEvent(T event);
}
