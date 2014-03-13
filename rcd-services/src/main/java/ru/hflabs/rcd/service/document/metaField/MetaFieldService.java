package ru.hflabs.rcd.service.document.metaField;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.event.modify.ChangeEvent;
import ru.hflabs.rcd.exception.search.document.UnknownMetaFieldException;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.path.DictionaryNamedPath;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;
import ru.hflabs.rcd.service.document.DocumentServiceTemplate;
import ru.hflabs.rcd.service.document.IDictionaryService;
import ru.hflabs.rcd.service.document.IMetaFieldService;
import ru.hflabs.util.core.EqualsUtil;
import ru.hflabs.util.core.Pair;
import ru.hflabs.util.spring.Assert;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static ru.hflabs.rcd.accessor.Accessors.linkRelative;
import static ru.hflabs.rcd.model.CriteriaUtils.*;
import static ru.hflabs.rcd.model.ModelUtils.*;
import static ru.hflabs.rcd.service.ServiceUtils.*;

/**
 * Класс <class>MetaFieldService</class> реализует сервис работы с МЕТА-полями справочников
 *
 * @author Nazin Alexander
 */
public class MetaFieldService extends DocumentServiceTemplate<MetaField> implements IMetaFieldService {

    /** Сервис работы со справочниками */
    private IDictionaryService dictionaryService;

    public MetaFieldService() {
        super(MetaField.class);
    }

    public void setDictionaryService(IDictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Override
    protected Collection<MetaField> injectTransitiveDependencies(Collection<MetaField> objects) {
        return super.injectTransitiveDependencies(injectRelations(objects, dictionaryService));
    }

    @Override
    public MetaField findUniqueByRelativeId(String relativeId, String name, boolean fillTransitive, boolean quietly) {
        MetaField result = findUniqueDocumentBy(this, createCriteriaByRelative(MetaField.DICTIONARY_ID, relativeId, MetaField.NAME, name), fillTransitive);
        if (result == null && !quietly) {
            throw new UnknownMetaFieldException(name);
        }
        return result;
    }

    @Override
    public Collection<MetaField> findAllByRelativeId(String relativeId, String searchQuery, boolean fillTransitive) {
        Assert.isTrue(StringUtils.hasText(relativeId), "ID must not be NULL or EMPTY");
        Collection<MetaField> metaFields = findAllByCriteria(createCriteriaByIDs(MetaField.DICTIONARY_ID, relativeId).injectSearch(searchQuery), fillTransitive);
        return sortMetaFieldsByOrdinal(metaFields);
    }

    @Override
    public MetaField findUniqueByNamedPath(MetaFieldNamedPath path, boolean quietly) {
        return findMetaFieldByNamedPath(path, quietly);
    }

    @Override
    public Collection<MetaField> findMetaFieldsByNamedPath(DictionaryNamedPath path) {
        validateDictionaryNamedPath(path);
        final Dictionary dictionary = dictionaryService.findUniqueByNamedPath(path, false);
        Collection<MetaField> metaFields = injectHistory(findAllByRelativeId(dictionary.getId(), null, false));
        return Lists.newArrayList(Collections2.transform(metaFields, new Function<MetaField, MetaField>() {
            @Override
            public MetaField apply(MetaField input) {
                return linkRelative(dictionary, input);
            }
        }));
    }

    @Override
    public MetaField findMetaFieldByNamedPath(MetaFieldNamedPath path, boolean quietly) {
        return extractSingleDocument(
                findMetaFieldByNamedPath(Sets.newHashSet(path), true).values(),
                quietly ?
                        null :
                        Pair.valueOf(path.getFieldName(), UnknownMetaFieldException.class)
        );
    }

    @Override
    public Map<MetaFieldNamedPath, MetaField> findMetaFieldByNamedPath(Set<MetaFieldNamedPath> paths, boolean quietly) {
        Assert.isTrue(!CollectionUtils.isEmpty(paths), "Collection must not be NULL or EMPTY");
        // Выполняем сортировку путей по справочникам
        Map<DictionaryNamedPath, Collection<MetaFieldNamedPath>> dictionary2path = Multimaps.index(paths, new Function<MetaFieldNamedPath, DictionaryNamedPath>() {
            @Override
            public DictionaryNamedPath apply(MetaFieldNamedPath input) {
                validateMetaFieldNamedPath(input);
                return new DictionaryNamedPath(input.getGroupName(), input.getDictionaryName());
            }
        }).asMap();

        // Выполняем поиск МЕТА-полей
        Map<MetaFieldNamedPath, MetaField> result = new HashMap<>(paths.size());
        for (Map.Entry<DictionaryNamedPath, Collection<MetaFieldNamedPath>> entry : dictionary2path.entrySet()) {
            Collection<MetaField> metaFields = findMetaFieldsByNamedPath(entry.getKey());
            for (final MetaFieldNamedPath path : entry.getValue()) {
                MetaField metaField = extractSingleDocument(Collections2.filter(metaFields, new Predicate<MetaField>() {
                    @Override
                    public boolean apply(MetaField input) {
                        return EqualsUtil.equals(input.getName(), path.getFieldName());
                    }
                }), null);
                if (metaField != null) {
                    result.put(path, metaField);
                }
            }
        }
        // Проверяем, что все МЕТА-поля найдены
        if (!quietly && paths.size() != result.size()) {
            Set<MetaFieldNamedPath> notFoundMetaFields = Sets.difference(paths, result.keySet());
            throw new UnknownMetaFieldException(
                    StringUtils.collectionToCommaDelimitedString(notFoundMetaFields)
            );
        }

        return result;
    }

    /**
     * Выполняет выделение {@link MetaField#PRIMARY_KEY первичного ключа} из коллеции МЕТА-полей
     *
     * @param dictionary описание справочника
     * @param metaFields коллекция МЕТА-полей
     * @return Возвращает первичное МЕТА-поле
     */
    private MetaField doFindPrimaryMetaField(String dictionary, Collection<MetaField> metaFields) {
        MetaField primaryMetaField = retrievePrimaryMetaField(metaFields);
        Assert.notNull(primaryMetaField, String.format("Can't find primary META field for dictionary '%s'", dictionary));
        return primaryMetaField;
    }

    @Override
    public MetaField findPrimaryMetaField(String dictionaryId, boolean fillTransitive, boolean quietly) {
        Assert.isTrue(StringUtils.hasText(dictionaryId), "Dictionary id must not be NULL or EMPTY");
        return doFindPrimaryMetaField(dictionaryId, findAllByRelativeId(dictionaryId, null, fillTransitive));
    }

    @Override
    public MetaField findPrimaryMetaFieldByNamedPath(DictionaryNamedPath path, boolean quietly) {
        validateDictionaryNamedPath(path);
        return doFindPrimaryMetaField(path.toString(), findMetaFieldsByNamedPath(path));
    }

    @Override
    protected void handleOtherCloseEvent(ChangeEvent event) {
        if (Dictionary.class.equals(event.getChangedClass())) {
            closeByCriteria(createCriteriaByDocumentIDs(MetaField.DICTIONARY_ID, event.getChanged(Dictionary.class)));
        }
    }
}
