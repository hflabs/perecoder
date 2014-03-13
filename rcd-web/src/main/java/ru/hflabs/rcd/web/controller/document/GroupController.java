package ru.hflabs.rcd.web.controller.document;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.hflabs.rcd.model.criteria.FilterCriteria;
import ru.hflabs.rcd.model.definition.ModelDefinition;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.service.document.IDictionaryService;
import ru.hflabs.rcd.service.document.IGroupService;
import ru.hflabs.rcd.service.rule.IRecodeRuleSetService;
import ru.hflabs.rcd.web.controller.ControllerTemplate;
import ru.hflabs.rcd.web.model.document.DictionaryStatisticBean;
import ru.hflabs.rcd.web.model.document.GroupBean;
import ru.hflabs.util.core.FormatUtil;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Set;

import static ru.hflabs.rcd.accessor.Accessors.injectId;
import static ru.hflabs.rcd.model.CriteriaUtils.createCriteriaByIDs;
import static ru.hflabs.rcd.model.ModelUtils.hasPermission;

/**
 * Класс <class>GroupController</class> реализует контроллер управления группами справочников
 *
 * @see Group
 */
@Controller(GroupController.MAPPING_URI + GroupController.NAME_POSTFIX)
@RequestMapping(GroupController.MAPPING_URI + GroupController.DATA_URI)
public class GroupController extends ControllerTemplate {

    public static final String MAPPING_URI = "groups";

    /** Сервис работы с группами справочников */
    @Resource(name = "groupService")
    private IGroupService groupService;
    /** Сервис работы со справочниками */
    @Resource(name = "dictionaryService")
    private IDictionaryService dictionaryService;
    /** Сервис работы с наборами правил перекодирования */
    @Resource(name = "recodeRuleSetService")
    private IRecodeRuleSetService recodeRuleSetService;

    @RequestMapping(value = "/model", method = RequestMethod.GET)
    @ResponseBody
    public ModelDefinition createModel() {
        return modelDefinitionFactory.retrieveService(Group.class);
    }

    /**
     * Выполняет формирование декоратора группы с заполнением рассчитываемых параметров
     *
     * @param group целевая группа
     * @return Возвращает декоратор группы
     */
    private GroupBean populateGroup(Group group) {
        // Получаем количество справочников в группе
        int dictionariesCount = dictionaryService.countByCriteria(createCriteriaByIDs(Dictionary.GROUP_ID, group.getId()));
        // Получаем несопоставленные справочники для группы
        Set<Dictionary> unmatchedDictionaries = recodeRuleSetService.findUnmatchedDictionaries(group.getId(), false);
        // Формируем и возвращаем декоратор группы
        return new GroupBean(group, new DictionaryStatisticBean(dictionariesCount, unmatchedDictionaries.size()));
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public Collection<GroupBean> getGroups(
            @RequestParam(value = FilterCriteria.SEARCH, required = false) String search,
            @RequestParam(value = Group.PERMISSION_WRITE_NAME, required = false) boolean onlyWritable) {
        // Получаем все группы справочников
        Collection<Group> groups = groupService.findAllByCriteria(
                new FilterCriteria().injectSearch(FormatUtil.parseString(search)),
                false
        );
        // Для каждой группы формируем ее декоратор
        ImmutableList.Builder<GroupBean> result = ImmutableList.builder();
        for (Group group : groups) {
            if (!onlyWritable || hasPermission(group, Group.PERMISSION_WRITE)) {
                result.add(populateGroup(group));
            }
        }
        // Возвращаем сформированные декораторы
        return result.build();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public GroupBean getGroup(@PathVariable String id) {
        return populateGroup(groupService.findByID(id, false, false));
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ResponseBody
    public GroupBean createGroup(@RequestBody Group group) {
        return populateGroup(createSingleDocument(groupService, injectId(group, null), true));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public GroupBean updateGroup(@PathVariable String id, @RequestBody Group group) {
        return populateGroup(updateSingleDocument(groupService, injectId(group, id), true));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void closeGroup(@PathVariable String id) {
        groupService.closeByIDs(Sets.newHashSet(id));
    }
}
