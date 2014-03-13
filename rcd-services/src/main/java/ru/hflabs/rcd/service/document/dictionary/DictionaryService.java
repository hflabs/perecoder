package ru.hflabs.rcd.service.document.dictionary;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.accessor.Accessors;
import ru.hflabs.rcd.event.modify.ChangeEvent;
import ru.hflabs.rcd.exception.search.document.UnknownDictionaryException;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.model.path.DictionaryNamedPath;
import ru.hflabs.rcd.service.document.DocumentServiceTemplate;
import ru.hflabs.rcd.service.document.IDictionaryService;
import ru.hflabs.rcd.service.document.IGroupService;
import ru.hflabs.util.core.Pair;
import ru.hflabs.util.spring.Assert;

import java.util.Collection;

import static ru.hflabs.rcd.accessor.Accessors.linkRelative;
import static ru.hflabs.rcd.model.CriteriaUtils.*;
import static ru.hflabs.rcd.model.ModelUtils.validateDictionaryNamedPath;
import static ru.hflabs.rcd.service.ServiceUtils.findUniqueDocumentBy;
import static ru.hflabs.rcd.service.ServiceUtils.injectRelations;

/**
 * Класс <class>DictionaryService</class> реализует сервис работы со справочниками
 *
 * @author Nazin Alexander
 */
public class DictionaryService extends DocumentServiceTemplate<Dictionary> implements IDictionaryService {

    /** Сервис поиска групп */
    private IGroupService groupService;

    public DictionaryService() {
        super(Dictionary.class);
    }

    public void setGroupService(IGroupService groupService) {
        this.groupService = groupService;
    }

    @Override
    protected Collection<Dictionary> injectTransitiveDependencies(Collection<Dictionary> objects) {
        return super.injectTransitiveDependencies(injectRelations(objects, groupService));
    }

    @Override
    public Pair<Dictionary, Collection<Group>> findMemberGroups(DictionaryNamedPath path, boolean quietly) {
        // Получаем целевой справочник
        final Dictionary dictionary = findUniqueByNamedPath(path, quietly);
        if (dictionary == null && quietly) {
            return null;
        }
        assert dictionary != null : "Unexpected NULL in dictionary instance";

        // Получаем коллекцию групп, в которых содержится справочник с таким же названием
        Collection<Dictionary> dictionariesWithSameName = Collections2.filter(
                findAllByCriteria(createCriteriaByIDs(Dictionary.NAME, dictionary.getName()), true),
                new Predicate<Dictionary>() {
                    @Override
                    public boolean apply(Dictionary input) {
                        return !dictionary.getGroupId().equals(input.getGroupId());
                    }
                }
        );
        // Возвращаем сформированный результат
        return Pair.<Dictionary, Collection<Group>>valueOf(
                dictionary,
                Lists.newArrayList(Collections2.transform(dictionariesWithSameName, Accessors.GROUP_TO_DICTIONARY_INJECTOR))
        );
    }

    @Override
    public Dictionary findUniqueByRelativeId(String relativeId, String name, boolean fillTransitive, boolean quietly) {
        Dictionary result = findUniqueDocumentBy(this, createCriteriaByRelative(Dictionary.GROUP_ID, relativeId, Dictionary.NAME, name), fillTransitive);
        if (result == null && !quietly) {
            throw new UnknownDictionaryException(name);
        }
        return result;
    }

    @Override
    public Collection<Dictionary> findAllByRelativeId(String relativeId, String searchQuery, boolean fillTransitive) {
        Assert.isTrue(StringUtils.hasText(relativeId), "ID must not be NULL or EMPTY");
        return findAllByCriteria(createCriteriaByIDs(Dictionary.GROUP_ID, relativeId).injectSearch(searchQuery), fillTransitive);
    }

    @Override
    public Dictionary findUniqueByNamedPath(DictionaryNamedPath path, boolean quietly) {
        validateDictionaryNamedPath(path);
        // Выполняем поиск группы по ее имени
        Group group = groupService.findUniqueByNamedPath(path.getGroupName(), quietly);
        if (group == null && quietly) {
            return null;
        }
        assert group != null : "Unexpected NULL in group instance";

        // Выполняем поиск справочника по связанной группе
        Dictionary result = injectHistory(findUniqueByRelativeId(group.getId(), path.getDictionaryName(), false, quietly));
        return linkRelative(group, result);
    }

    @Override
    protected void handleOtherCloseEvent(ChangeEvent event) {
        if (Group.class.equals(event.getChangedClass())) {
            closeByCriteria(createCriteriaByDocumentIDs(Dictionary.GROUP_ID, event.getChanged(Group.class)));
        }
    }
}
