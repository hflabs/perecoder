package ru.hflabs.rcd.model.criteria;

import ru.hflabs.rcd.model.change.ChangeType;
import ru.hflabs.rcd.term.Condition;

import java.util.EnumSet;
import java.util.Set;

/**
 * Класс <class>FilterActivity</class> содержит информацию об активности документов
 *
 * @see ChangeType
 */
public class FilterActivity extends FilterCriteriaValue.EnumValues<ChangeType> {

    private static final long serialVersionUID = 798278006650504764L;

    /** Все документы */
    public static final FilterActivity ALL = new FilterActivity(EnumSet.allOf(ChangeType.class));
    /** Только актуальные */
    public static final FilterActivity ACTUAL = new FilterActivity(EnumSet.of(ChangeType.CREATE, ChangeType.UPDATE, ChangeType.RESTORE));
    /** Только закрытые */
    public static final FilterActivity CLOSED = new FilterActivity(EnumSet.of(ChangeType.CLOSE), Condition.AND);

    public FilterActivity(Set<ChangeType> changeTypes) {
        super(changeTypes);
    }

    public FilterActivity(Set<ChangeType> changeTypes, Condition condition) {
        super(changeTypes, condition);
    }
}
