package ru.hflabs.rcd.model.criteria;

import lombok.Getter;
import lombok.Setter;
import ru.hflabs.rcd.term.Condition;

import javax.swing.*;
import java.io.Serializable;
import java.util.Map;

/**
 * Класс <class>FilterCriteria</class> содержит информацию о критерии поиска сущностей
 *
 * @see FilterCriteriaValue
 */
@Getter
@Setter
public class FilterCriteria implements Serializable {

    private static final long serialVersionUID = 7953241649742266203L;

    /*
     * Название полей с идентификаторами
     */
    public static final String SEARCH = "search";
    public static final String PAGE = "page";

    /*
     * Значения по умолчанию
     */
    public static final int COUNT_DEFAULT = 100;
    public static final int COUNT_ALL = -1;

    /** Поле, по которому производится сортировка */
    private String sortOrderKey;
    /** Значение сортировки */
    private SortOrder sortOrderValue;

    /** Смещение односительно начала объектов */
    private int offset = 0;
    /** Количество запрашиваемых объектов */
    private int count = Integer.MAX_VALUE;

    /** Строка поиска */
    private String search;
    /** Условие ассоциации поиска с фильтрами */
    private Condition searchCondition = Condition.AND;
    /** Карта фильтрации, где ключ - имя фильтруемого поля, значение фильтра */
    private Map<String, FilterCriteriaValue<?>> filters;
    /** Целевая активность документов */
    private FilterActivity activity = FilterActivity.ACTUAL;

    public FilterCriteria injectSort(String targetOrder, SortOrder targetValue) {
        setSortOrderKey(targetOrder);
        setSortOrderValue(targetValue);
        return this;
    }

    public FilterCriteria injectOffset(int targetOffset) {
        setOffset(targetOffset);
        return this;
    }

    public FilterCriteria injectCount(int targetCount) {
        setCount(targetCount);
        return this;
    }

    public FilterCriteria injectSearch(String targetSearch) {
        return injectSearch(targetSearch, Condition.AND);
    }

    public FilterCriteria injectSearch(String targetSearch, Condition targetSearchCondition) {
        setSearch(targetSearch);
        setSearchCondition(targetSearchCondition);
        return this;
    }

    public FilterCriteria injectFilters(Map<String, FilterCriteriaValue<?>> filter) {
        setFilters(filter);
        return this;
    }

    public FilterCriteria injectActivity(FilterActivity targetActivity) {
        setActivity(targetActivity);
        return this;
    }
}
