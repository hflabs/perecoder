package ru.hflabs.rcd.web.controller.notification;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.hflabs.rcd.model.criteria.FilterCriteria;
import ru.hflabs.rcd.model.criteria.FilterCriteriaValue;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.model.notification.Notification;
import ru.hflabs.rcd.model.notification.NotifyState;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;
import ru.hflabs.rcd.service.INotificationService;
import ru.hflabs.rcd.service.ServiceUtils;
import ru.hflabs.rcd.service.document.IDictionaryService;
import ru.hflabs.rcd.service.document.IGroupService;
import ru.hflabs.rcd.service.rule.IRecodeRuleSetService;
import ru.hflabs.rcd.web.controller.ControllerTemplate;
import ru.hflabs.rcd.web.model.NotificationBean;
import ru.hflabs.util.core.Pair;

import javax.annotation.Resource;
import javax.swing.*;
import java.util.Collection;

/**
 * Класс <class>NotificationController</class> реализует контроллер управления оповещениями
 *
 * @see Notification
 */
@Controller(NotificationController.MAPPING_URI + NotificationController.NAME_POSTFIX)
@RequestMapping(NotificationController.MAPPING_URI + NotificationController.DATA_URI)
public class NotificationController extends ControllerTemplate {

    public static final String MAPPING_URI = "notifications";

    /** Сервис работы с группами справочников */
    @Resource(name = "groupService")
    private IGroupService groupService;
    /** Сервис работы со справочниками */
    @Resource(name = "dictionaryService")
    private IDictionaryService dictionaryService;
    /** Сервис работы с наборами правил перекодирования */
    @Resource(name = "recodeRuleSetService")
    private IRecodeRuleSetService recodeRuleSetService;
    /** Сервис работы с оповещениями */
    @Resource(name = "notificationService")
    private INotificationService notificationService;
    /** Количество отображаемых групп справочников */
    @Value("$web{notification.count}")
    private int notificationCount = -1;

    /**
     * Выполняет поиск назначения
     *
     * @param groupName назнвание группы
     * @param dictionaryName название справочника
     * @return Возвращает пару из группы и ее справочника
     */
    private Pair<Group, Dictionary> findDestination(String groupName, String dictionaryName) {
        Group group = StringUtils.hasText(groupName) ?
                groupService.findUniqueByNamedPath(groupName, true) :
                null;
        Dictionary dictionary = (group != null && StringUtils.hasText(dictionaryName)) ?
                dictionaryService.findUniqueByRelativeId(group.getId(), dictionaryName, false, true) :
                null;
        return Pair.valueOf(group, dictionary);
    }

    /**
     * Выполняет формирование декоратора оповещения с заполнением рассчитываемых параметров
     *
     * @param notification оповещение
     * @return Возвращает сформированный декоратор
     */
    private NotificationBean populateNotification(Notification notification) {
        RecodeRuleSet ruleSet = StringUtils.hasText(notification.getRuleSetName()) ?
                recodeRuleSetService.findUniqueByNamedPath(notification.getRuleSetName(), true) :
                null;
        Pair<Group, Dictionary> from = findDestination(notification.getFromGroupName(), notification.getFromDictionaryName());
        Pair<Group, Dictionary> to = findDestination(notification.getToGroupName(), notification.getToDictionaryName());
        return new NotificationBean(
                notification,
                new NotificationBean.Reference<>(ruleSet, notification.getRuleSetName()),
                new NotificationBean.Reference<>(from.first, notification.getFromGroupName()),
                new NotificationBean.Reference<>(from.second, notification.getFromDictionaryName()),
                new NotificationBean.Reference<>(to.first, notification.getToGroupName()),
                new NotificationBean.Reference<>(to.second, notification.getToDictionaryName())
        );
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public Collection<NotificationBean> getNotifications() {
        Collection<Notification> notifications = notificationService.findByCriteria(
                new FilterCriteria()
                        .injectFilters(
                                ImmutableMap.<String, FilterCriteriaValue<?>>of(
                                        Notification.PROCESSING_STATE, new FilterCriteriaValue.EnumValues<>(NotifyState.PENDING)
                                )
                        )
                        .injectSort(Notification.END_DATE, SortOrder.DESCENDING)
                        .injectCount(notificationCount),
                false).getResult();
        // Для каждого оповещения формируем его декораторы
        ImmutableList.Builder<NotificationBean> result = ImmutableList.builder();
        for (Notification notification : notifications) {
            result.add(populateNotification(notification));
        }
        // Возвращаем сформированные декораторы
        return result.build();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public Notification markAsProcessed(@PathVariable String id) {
        return ServiceUtils.extractSingleDocument(
                notificationService.changeNotifyState(Sets.newHashSet(id), NotifyState.PROCESSED),
                null
        );
    }
}
