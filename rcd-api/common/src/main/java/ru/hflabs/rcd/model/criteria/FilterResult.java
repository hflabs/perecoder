package ru.hflabs.rcd.model.criteria;

import lombok.Getter;

import java.io.Serializable;
import java.util.Collection;

/**
 * Класс <class>FilterResult</class> содержит информацию о результатах поиска по критерию
 *
 * @see FilterCriteria
 */
@Getter
public class FilterResult<E> implements Serializable {

    private static final long serialVersionUID = 6290140531815100155L;

    /** Коллекция документов */
    private final Collection<E> result;

    /** Количество документов по фильтру */
    private final int countByFilter;
    /** Общее количество документов */
    private final int totalCount;

    public FilterResult(Collection<E> result, int countByFilter, int totalCount) {
        this.result = result;
        this.countByFilter = countByFilter;
        this.totalCount = totalCount;
    }

    public boolean isEmpty() {
        return result == null || result.isEmpty();
    }

    public int size() {
        return !isEmpty() ? result.size() : 0;
    }
}
