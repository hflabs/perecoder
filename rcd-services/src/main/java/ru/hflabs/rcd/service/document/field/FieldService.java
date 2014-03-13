package ru.hflabs.rcd.service.document.field;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.apache.lucene.document.DocumentStoredFieldVisitor;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ReferenceManager;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.event.modify.ChangeEvent;
import ru.hflabs.rcd.exception.search.document.UnknownFieldException;
import ru.hflabs.rcd.lucene.criteria.LuceneCriteriaHolder;
import ru.hflabs.rcd.model.criteria.FilterCriteria;
import ru.hflabs.rcd.model.document.*;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;
import ru.hflabs.rcd.service.document.DocumentServiceTemplate;
import ru.hflabs.rcd.service.document.IFieldService;
import ru.hflabs.rcd.service.document.IMetaFieldService;
import ru.hflabs.util.core.EqualsUtil;
import ru.hflabs.util.lucene.LuceneQueryCallback;
import ru.hflabs.util.lucene.LuceneQueryDescriptor;
import ru.hflabs.util.lucene.LuceneQueryUtil;
import ru.hflabs.util.spring.Assert;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

import static ru.hflabs.rcd.accessor.Accessors.META_FIELD_TO_FIELD_INJECTOR;
import static ru.hflabs.rcd.accessor.Accessors.linkRelative;
import static ru.hflabs.rcd.model.CriteriaUtils.*;
import static ru.hflabs.rcd.model.ModelUtils.ID_FUNCTION;
import static ru.hflabs.rcd.model.ModelUtils.extractFieldsFromRecords;
import static ru.hflabs.rcd.service.ServiceUtils.*;

/**
 * Класс <class>FieldService</class> реализует сервис работы со значениями полей записи
 *
 * @author Nazin Alexander
 */
public class FieldService extends DocumentServiceTemplate<Field> implements IFieldService {

    /** Сервис работы с МЕТА-полями */
    private IMetaFieldService metaFieldService;

    public FieldService() {
        super(Field.class);
    }

    public void setMetaFieldService(IMetaFieldService metaFieldService) {
        this.metaFieldService = metaFieldService;
    }

    @Override
    protected Collection<Field> injectTransitiveDependencies(Collection<Field> objects) {
        return super.injectTransitiveDependencies(injectRelations(objects, metaFieldService));
    }

    @Override
    public Field findUniqueByRelativeId(String relativeId, String name, boolean fillTransitive, boolean quietly) {
        Field result = findUniqueDocumentBy(this, createCriteriaByRelative(Field.META_FIELD_ID, relativeId, Field.NAME, name), fillTransitive);
        if (result == null && !quietly) {
            throw new UnknownFieldException(name);
        }
        return result;
    }

    @Override
    public Collection<Field> findAllByRelativeId(String relativeId, String searchQuery, boolean fillTransitive) {
        Assert.isTrue(StringUtils.hasText(relativeId), "Relative ID must not be NULL or EMPTY");
        return findAllByCriteria(createCriteriaByIDs(Field.META_FIELD_ID, relativeId).injectSearch(searchQuery), fillTransitive);
    }

    @Override
    public Collection<Field> findByNames(String relativeId, Set<String> names, boolean fillTransitive) {
        Assert.isTrue(StringUtils.hasText(relativeId), "Relative ID must not be NULL or EMPTY");
        return findAllByCriteria(createCriteriaByRelative(Field.META_FIELD_ID, relativeId, Field.NAME, names), fillTransitive);
    }

    @Override
    public Collection<Field> findByValues(String relativeId, Set<String> values, boolean fillTransitive) {
        Assert.isTrue(StringUtils.hasText(relativeId), "Relative ID must not be NULL or EMPTY");
        final MetaField metaField = metaFieldService.findByID(relativeId, fillTransitive, false);

        Collection<Field> result = findAllByCriteria(
                createCriteriaByRelative(Field.META_FIELD_ID, relativeId, Field.VALUE, values),
                false
        );
        return fillTransitive ?
                Lists.newArrayList(Collections2.transform(result, new Function<Field, Field>() {
                    @Override
                    public Field apply(Field input) {
                        return linkRelative(metaField, input);
                    }
                })) :
                result;
    }

    @Override
    public Collection<Field> findAllByMetaFields(Collection<String> metaFieldIDs, boolean fillTransitive) {
        if (!CollectionUtils.isEmpty(metaFieldIDs)) {
            return findAllByCriteria(createCriteriaByIDs(Field.META_FIELD_ID, metaFieldIDs), fillTransitive);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isFieldExist(String metaFieldId, String value) {
        return countByCriteria(createCriteriaByRelative(Field.META_FIELD_ID, metaFieldId, Field.VALUE, value)) != 0;
    }

    @Override
    public boolean isFieldUnique(Field field) {
        Assert.notNull(field, "Field must not be NULL");
        FilterCriteria criteria = createCriteriaByRelative(
                Field.META_FIELD_ID, field.getMetaFieldId(), Field.VALUE, field.getValue()
        ).injectCount(2);

        Collection<Field> result = findByCriteria(criteria, false).getResult();
        if (result.size() == 1) {
            return EqualsUtil.equals(extractSingleDocument(result).getId(), field.getId());
        } else {
            return result.isEmpty();
        }
    }

    @Override
    public boolean isFieldsUnique(String metaFieldId) {
        // Проверяем, что поля существуют
        final LuceneCriteriaHolder criteria = criteriaBuilder.createCriteria(
                retrieveTargetClass(),
                createCriteriaByIDs(Field.META_FIELD_ID, metaFieldId).injectSort(Field.VALUE, SortOrder.ASCENDING)
        );
        final int totalCount = queryProvider.executeCountByCriteria(criteria);
        if (totalCount <= 0) {
            return true;
        }
        // Выполняем итерирование значений полей пока не дойдем до конца или не встретим дублирующегося значения
        UniqueFieldHandler uniqueFieldHandler = new UniqueFieldHandler();
        try {
            LuceneQueryUtil.query(
                    binderTransformer,
                    new LuceneQueryCallback() {
                        @Override
                        public ReferenceManager<IndexSearcher> getSearcherManager() {
                            return refreshSearcherManager(false);
                        }

                        @Override
                        public LuceneQueryDescriptor getQueryDescriptor() {
                            return new LuceneQueryDescriptor(criteria.buildQuery(), criteria.buildSort(), criteria.buildFilter(), 0, totalCount);
                        }

                        @Override
                        public DocumentStoredFieldVisitor createStoredFieldVisitor() {
                            return new DocumentStoredFieldVisitor();
                        }
                    },
                    uniqueFieldHandler
            );
            return uniqueFieldHandler.isUnique();
        } catch (IOException ex) {
            throw new RuntimeException(String.format("Can't find unique fields '%s' index. Cause by: %s", retrieveTargetClassName(), ex.getMessage()), ex);
        }
    }

    @Override
    public Collection<DocumentContext> findDocumentContexts(Set<MetaFieldNamedPath> namedPath) {
        Map<MetaFieldNamedPath, MetaField> metaFields = metaFieldService.findMetaFieldByNamedPath(namedPath, false);
        Map<String, MetaField> id2metaFields = Maps.uniqueIndex(metaFields.values(), ID_FUNCTION);
        Collection<Field> fields = findAllByMetaFields(id2metaFields.keySet(), false);

        ImmutableList.Builder<DocumentContext> result = ImmutableList.builder();
        for (Field field : fields) {
            field = META_FIELD_TO_FIELD_INJECTOR.inject(field, id2metaFields.get(field.getMetaFieldId()));
            result.add(Contexts.createDocumentContext(field));
        }

        return result.build();
    }

    @Override
    protected void handleOtherCreateEvent(ChangeEvent event) {
        if (Record.class.equals(event.getChangedClass())) {
            create(extractFieldsFromRecords(event.getChanged(Record.class)), true);
        }
    }

    @Override
    protected void handleOtherUpdateEvent(ChangeEvent event) {
        if (Record.class.isAssignableFrom(event.getChangedClass())) {
            update(extractFieldsFromRecords(event.getChanged(Record.class)), true);
        }
    }

    @Override
    protected void handleOtherCloseEvent(ChangeEvent event) {
        if (MetaField.class.equals(event.getChangedClass())) {
            closeByCriteria(createCriteriaByDocumentIDs(Field.META_FIELD_ID, event.getChanged(MetaField.class)));
        } else if (Record.class.equals(event.getChangedClass())) {
            Collection<String> fieldIDs = Sets.newHashSet(Collections2.transform(extractFieldsFromRecords(event.getChanged(Record.class)), ID_FUNCTION));
            closeByCriteria(createCriteriaByIDs(Field.PRIMARY_KEY, fieldIDs));
        }
    }

    /**
     * Класс <class>UniqueFieldHandler</class> реализует предикат уникальных значений полей
     *
     * @author Nazin Alexander
     */
    private static class UniqueFieldHandler implements Predicate<Field> {

        /** Предыдущее значение поля */
        private String previousValue;
        /** Флаг уникальности */
        private boolean isUnique;

        private UniqueFieldHandler() {
            this.previousValue = UUID.randomUUID().toString();
            this.isUnique = true;
        }

        public boolean isUnique() {
            return isUnique;
        }

        @Override
        public boolean apply(Field input) {
            isUnique = !EqualsUtil.equals(previousValue, input.getValue());
            previousValue = input.getValue();
            return isUnique;
        }
    }
}
