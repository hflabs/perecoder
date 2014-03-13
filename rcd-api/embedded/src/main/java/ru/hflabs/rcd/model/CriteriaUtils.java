package ru.hflabs.rcd.model;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.model.criteria.FilterCriteria;
import ru.hflabs.rcd.model.criteria.FilterCriteriaValue;
import ru.hflabs.util.spring.Assert;

import java.util.Collection;

import static ru.hflabs.rcd.model.ModelUtils.ID_FUNCTION;

/**
 * Класс <class>CriteriaUtils</class> реализует всмомогательные методы для построения критерий
 *
 * @see FilterCriteria
 */
public abstract class CriteriaUtils {

    protected CriteriaUtils() {
        // embedded constructor
    }

    /**
     * @param fieldName название поля с идентификатором
     * @param ids коллекция идентификаторов
     * @return Возвращает критерий поиска сущностей по идентификаторам
     */
    public static FilterCriteria createCriteriaByIDs(String fieldName, String... ids) {
        return createCriteriaByIDs(fieldName, Sets.newHashSet(ids));
    }

    /**
     * @param fieldName название поля с идентификатором
     * @param ids коллекция идентификаторов
     * @return Возвращает критерий поиска сущностей по идентификаторам
     */
    public static FilterCriteria createCriteriaByIDs(String fieldName, Collection<String> ids) {
        return new FilterCriteria().injectFilters(ImmutableMap.<String, FilterCriteriaValue<?>>of(fieldName, new FilterCriteriaValue.StringsValue(ids)));
    }

    /**
     * @param fieldName название поля с идентификатором
     * @param documents коллекция значений для получения идентификаторов
     * @return Возвращает критерий поиска сущностей по идентификаторам
     */
    public static <T extends Identifying> FilterCriteria createCriteriaByDocumentIDs(String fieldName, Collection<T> documents) {
        return createCriteriaByDocuments(fieldName, documents, ID_FUNCTION);
    }

    /**
     * @param fieldName название поля с идентификатором
     * @param documents коллекция значений для получения идентификаторов
     * @param function функция извлечения значения поля
     * @return Возвращает критерий поиска сущностей по идентификаторам
     */
    public static <T> FilterCriteria createCriteriaByDocuments(String fieldName, Collection<T> documents, Function<? super T, String> function) {
        return createCriteriaByIDs(fieldName, Collections2.transform(documents, function));
    }

    /**
     * @param relativeFieldName название поля связанного идентификатора
     * @param relativeId значение связанного идентификатора
     * @param searchField поисковое поле
     * @param searchValues коллекция значений поиска
     * @return Возвращает критерий поиска сущностей по связанным идентификаторам
     */
    public static FilterCriteria createCriteriaByRelative(String relativeFieldName, String relativeId, String searchField, String... searchValues) {
        return createCriteriaByRelative(relativeFieldName, relativeId, searchField, Lists.newArrayList(searchValues));
    }

    /**
     * @param relativeFieldName название поля связанного идентификатора
     * @param relativeId значение связанного идентификатора
     * @param searchField поисковое поле
     * @param searchValues коллекция значений поиска
     * @return Возвращает критерий поиска сущностей по связанным идентификаторам
     */
    public static FilterCriteria createCriteriaByRelative(String relativeFieldName, String relativeId, String searchField, Collection<String> searchValues) {
        Assert.isTrue(StringUtils.hasText(relativeId), String.format("Relative '%s' must not be NULL or EMPTY", relativeFieldName));
        ImmutableMap.Builder<String, FilterCriteriaValue<?>> filters = ImmutableMap.<String, FilterCriteriaValue<?>>builder()
                .put(relativeFieldName, new FilterCriteriaValue.StringValue(relativeId));
        if (searchField != null && !CollectionUtils.isEmpty(searchValues)) {
            filters.put(searchField, new FilterCriteriaValue.StringsValue(searchValues));
        }
        return new FilterCriteria().injectFilters(filters.build());
    }
}
