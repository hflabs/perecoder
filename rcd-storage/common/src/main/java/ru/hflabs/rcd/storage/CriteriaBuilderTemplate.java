package ru.hflabs.rcd.storage;

import com.google.common.collect.Sets;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.criteria.CriteriaHolder;
import ru.hflabs.rcd.model.criteria.FilterActivity;
import ru.hflabs.rcd.model.criteria.FilterCriteria;
import ru.hflabs.rcd.model.criteria.FilterCriteriaValue;
import ru.hflabs.rcd.service.IActivityBuilder;
import ru.hflabs.rcd.service.ICriteriaBuilder;
import ru.hflabs.rcd.term.Condition;

import javax.swing.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Класс <class>CriteriaBuilderTemplate</class> базовый класс для сервиса построения критерии
 *
 * @see FilterCriteria
 */
public abstract class CriteriaBuilderTemplate<E extends Identifying, C extends CriteriaHolder<Q>, Q> implements ICriteriaBuilder<E, C, Q> {

    /** Регулярное выражение для определения пустых символов */
    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");
    /** Делегат построения активности документов */
    private Collection<IActivityBuilder<E, C, Q>> activityBuilders;

    protected CriteriaBuilderTemplate() {
        this.activityBuilders = Collections.emptyList();
    }

    public void setActivityBuilders(Collection<IActivityBuilder<E, C, Q>> activityBuilders) {
        this.activityBuilders = activityBuilders;
    }

    /**
     * Создает и вызывает исключтельную ситуацию нереализованного метода
     *
     * @return Вызывает исключительную ситуацию нереализованного метода проверки
     */
    protected <K> K throwAssertionError(String action) {
        throw new UnsupportedOperationException(String.format("%s not supported by '%s'", action, getClass().getName()));
    }

    /**
     * Выполняет формирование критерии для пустых значений
     *
     * @param current текущий критерий
     * @param criteriaClass целевой класс критерии
     * @param criteriaField целевое поле
     * @param value значение фильтра
     * @return Возвращает обновленный критерий
     */
    protected C appendFilterByEmptyValue(C current, Class<E> criteriaClass, String criteriaField, FilterCriteriaValue.EmptyValue value) {
        return throwAssertionError(value.getClass().getName());
    }

    /**
     * Выполняет формирование критерии для не пустых значений
     *
     * @param current текущий критерий
     * @param criteriaClass целевой класс критерии
     * @param criteriaField целевое поле
     * @param value значение фильтра
     * @return Возвращает обновленный критерий
     */
    protected C appendFilterByNotEmptyValue(C current, Class<E> criteriaClass, String criteriaField, FilterCriteriaValue.NotEmptyValue value) {
        return throwAssertionError(value.getClass().getName());
    }

    /**
     * Выполняет формирование критерии для логического типа
     *
     * @param current текущий критерий
     * @param criteriaClass целевой класс критерии
     * @param criteriaField целевое поле
     * @param value значение фильтра
     * @return Возвращает обновленный критерий
     */
    protected C appendFilterByBooleanValue(C current, Class<E> criteriaClass, String criteriaField, FilterCriteriaValue.BooleanValue value) {
        return throwAssertionError(value.getClass().getName());
    }

    /**
     * Выполняет формирование критерии для перечисляемого типа
     *
     * @param current текущий критерий
     * @param criteriaClass целевой класс критерии
     * @param criteriaField целевое поле
     * @param value значение фильтра
     * @return Возвращает обновленный критерий
     */
    protected C appendFilterByEnumValues(C current, Class<E> criteriaClass, String criteriaField, FilterCriteriaValue.EnumValues<?> value) {
        return throwAssertionError(value.getClass().getName());
    }

    /**
     * Выполняет формирование критерии для строки
     *
     * @param current текущий критерий
     * @param criteriaClass целевой класс критерии
     * @param criteriaField целевое поле
     * @param value значение фильтра
     * @return Возвращает обновленный критерий
     */
    protected C appendFilterByStringValue(C current, Class<E> criteriaClass, String criteriaField, FilterCriteriaValue.StringValue value) {
        return throwAssertionError(value.getClass().getName());
    }

    /**
     * Выполняет формирование критерии для даты
     *
     * @param current текущий критерий
     * @param criteriaClass целевой класс критерии
     * @param criteriaField целевое поле
     * @param value значение фильтра
     * @return Возвращает обновленный критерий
     */
    protected C appendFilterByDateValue(C current, Class<E> criteriaClass, String criteriaField, FilterCriteriaValue.DateValue value) {
        return throwAssertionError(value.getClass().getName());
    }

    /**
     * Выполняет формирование критерии для коллекции строки
     *
     * @param current текущий критерий
     * @param criteriaClass целевой класс критерии
     * @param criteriaField целевое поле
     * @param value значение фильтра
     * @return Возвращает обновленный критерий
     */
    protected C appendFilterByStringsValue(C current, Class<E> criteriaClass, String criteriaField, FilterCriteriaValue.StringsValue value) {
        return throwAssertionError(value.getClass().getName());
    }

    /**
     * Выполняет построение критерии поиска по умолчанию
     *
     * @param current текущий критерий
     * @param criteriaClass целевой класс критерии
     * @param values коллекция значений поиска
     * @param condition условие ассоциации поиска с фильтрами
     * @return Возвращает обновленный критерий
     */
    protected C appendSearch(C current, Class<E> criteriaClass, Set<String> values, Condition condition) {
        return throwAssertionError("search by default value");
    }

    /**
     * Выполняет построение критерии поиска по умолчанию
     *
     * @param current текущий критерий
     * @param criteriaClass целевой класс критерии
     * @param value значение поиска
     * @param condition условие ассоциации поиска с фильтрами
     * @return Возвращает обновленный критерий
     */
    protected C appendSearch(C current, Class<E> criteriaClass, String value, Condition condition) {
        if (StringUtils.hasText(value)) {
            // Выполняем выделение отдельных слов из строки поиска
            Set<String> words = Sets.newLinkedHashSet(Arrays.asList(SPACE_PATTERN.split(value)));
            // Выполняем построение фильтров
            current = appendSearch(current, criteriaClass, words, condition);
        }
        return current;
    }

    /**
     * Выполняет построение фильтров критерии
     *
     * @param current текущий критерий
     * @param criteriaClass целевой класс критерии
     * @param filters коллекция фильтров
     * @return Возвращает обновленный критерий
     */
    protected C appendFilters(C current, Class<E> criteriaClass, Map<String, FilterCriteriaValue<?>> filters) {
        // Выполняем построение фильтров
        if (filters != null) {
            for (Map.Entry<String, FilterCriteriaValue<?>> entry : filters.entrySet()) {
                // Получаем клю фильтра
                final String key = entry.getKey();
                // Получаем значение фильтра
                final FilterCriteriaValue<?> value = entry.getValue();
                // Определяем тип запроса
                if (value instanceof FilterCriteriaValue.ClauseValue) {
                    C innerClauseCriteria = createEmptyCriteria(criteriaClass);
                    innerClauseCriteria = appendFilters(innerClauseCriteria, criteriaClass, ((FilterCriteriaValue.ClauseValue) value).getValue());
                    current.appendQuery(innerClauseCriteria.buildQuery(), value.getCondition());
                } else if (value instanceof FilterCriteriaValue.EmptyValue) {
                    current = appendFilterByEmptyValue(current, criteriaClass, key, (FilterCriteriaValue.EmptyValue) value);
                } else if (value instanceof FilterCriteriaValue.NotEmptyValue) {
                    current = appendFilterByNotEmptyValue(current, criteriaClass, key, (FilterCriteriaValue.NotEmptyValue) value);
                } else if (value instanceof FilterCriteriaValue.BooleanValue) {
                    current = appendFilterByBooleanValue(current, criteriaClass, key, (FilterCriteriaValue.BooleanValue) value);
                } else if (value instanceof FilterCriteriaValue.EnumValues) {
                    current = appendFilterByEnumValues(current, criteriaClass, key, (FilterCriteriaValue.EnumValues<?>) value);
                } else if (value instanceof FilterCriteriaValue.StringValue) {
                    current = appendFilterByStringValue(current, criteriaClass, key, (FilterCriteriaValue.StringValue) value);
                } else if (value instanceof FilterCriteriaValue.DateValue) {
                    current = appendFilterByDateValue(current, criteriaClass, key, (FilterCriteriaValue.DateValue) value);
                } else if (value instanceof FilterCriteriaValue.StringsValue) {
                    current = appendFilterByStringsValue(current, criteriaClass, key, (FilterCriteriaValue.StringsValue) value);
                } else {
                    throwAssertionError(value.getClass().getName());
                }
            }
        }
        // Возвращаем модифицированный критерий
        return current;
    }

    /**
     * Выполняет построение сортировки критерии
     *
     * @param current текущий критерий
     * @param criteriaClass целевой класс критерии
     * @param orderKey поле по которому производится сортировка
     * @param orderValue значение сортировки
     * @return Возвращает обновленный критерий
     */
    protected C appendOrder(C current, Class<E> criteriaClass, String orderKey, SortOrder orderValue) {
        return throwAssertionError(SortOrder.class.getSimpleName());
    }

    /**
     * Добавляет сортировку по умолчанию
     *
     * @param current текущий критерий
     * @param criteriaClass целевой класс критерии
     * @return Возвращает обновленный критерий
     */
    protected C appendDefaultOrder(C current, Class<E> criteriaClass) {
        return current;
    }

    /**
     * Выполняет построение сортировки критерии
     *
     * @param current текущий критерий
     * @param criteriaClass целевой класс критерии
     * @param orderKey поле по которому производится сортировка
     * @param orderValue значение сортировки
     * @return Возвращает обновленный критерий
     */
    protected C appendOrders(C current, Class<E> criteriaClass, String orderKey, SortOrder orderValue) {
        if (StringUtils.hasText(orderKey) && !SortOrder.UNSORTED.equals(orderValue)) {
            current = appendOrder(current, criteriaClass, orderKey, orderValue);
        }
        return appendDefaultOrder(current, criteriaClass);
    }

    /**
     * Выполняет построение фильтра активности документов
     *
     * @param current текущий критерий
     * @param criteriaClass целевой класс критерии
     * @param activity целевая активность документов
     * @return Возвращает обновленный критерий
     */
    protected C appendActivity(C current, Class<E> criteriaClass, FilterActivity activity) {
        for (IActivityBuilder<E, C, Q> builder : activityBuilders) {
            if (builder.isSupport(criteriaClass)) {
                current = builder.createActivity(current, criteriaClass, activity);
            }
        }
        return current;
    }

    @Override
    public C createCriteria(Class<E> criteriaClass, FilterCriteria filter) {
        C criteria = createEmptyCriteria(criteriaClass);
        {
            criteria = appendSearch(criteria, criteriaClass, filter.getSearch(), filter.getSearchCondition());
            criteria = appendFilters(criteria, criteriaClass, filter.getFilters());
            criteria = appendOrders(criteria, criteriaClass, filter.getSortOrderKey(), filter.getSortOrderValue());
            criteria = appendActivity(criteria, criteriaClass, filter.getActivity());
        }
        return criteria;
    }
}
