package ru.hflabs.rcd.service.rule;

import ru.hflabs.rcd.RoleNames;
import ru.hflabs.rcd.model.rule.RecodeRule;
import ru.hflabs.rcd.service.IDocumentService;
import ru.hflabs.rcd.service.IManyToOneService;

import javax.annotation.security.RolesAllowed;
import java.util.Collection;

/**
 * Интерфейс <class>IRecodeRuleService</class> декларирует методы для работы с правилами перекодирования
 *
 * @see RecodeRule
 */
public interface IRecodeRuleService extends IDocumentService<RecodeRule>, IRuleService<RecodeRule>, IManyToOneService<RecodeRule> {

    /**
     * Выполняет поиск правил привязанных к идентификаторам исходных полей
     *
     * @param recodeRuleSetId идентификатор набора правил
     * @param fromFieldIDs коллекция идентификаторов исходных полей
     * @param fillTransitive флаг заполнения транзитивных зависимостей
     * @return Возвращает коллекцию найденных правил
     */
    @RolesAllowed(RoleNames.OPERATOR)
    Collection<RecodeRule> findAllByFieldIDs(String recodeRuleSetId, Collection<String> fromFieldIDs, boolean fillTransitive);

    /**
     * Выполняет модификацию правил перекодирования
     *
     * @param toCreate коллекция правил для создания
     * @param toUpdate коллекция правил для обновления
     * @param toClose коллекция правил для закрытия
     * @param needValidation флаг, указывающий на необходимость валидации модифицируемых правил
     * @return Возвращает коллекцию модифицированных правил
     */
    @RolesAllowed(RoleNames.ADMINISTRATOR)
    Collection<RecodeRule> modify(Collection<RecodeRule> toCreate, Collection<RecodeRule> toUpdate, Collection<RecodeRule> toClose, boolean needValidation);
}
