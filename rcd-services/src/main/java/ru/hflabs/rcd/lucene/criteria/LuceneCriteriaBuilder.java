package ru.hflabs.rcd.lucene.criteria;

import org.apache.lucene.search.*;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.index.IndexedField;
import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.criteria.FilterCriteriaValue;
import ru.hflabs.rcd.storage.CriteriaBuilderTemplate;
import ru.hflabs.rcd.term.Condition;
import ru.hflabs.util.core.Pair;
import ru.hflabs.util.lucene.LuceneBinderTransformer;
import ru.hflabs.util.lucene.LuceneQueryUtil;
import ru.hflabs.util.lucene.LuceneUtil;

import javax.swing.*;
import java.util.Date;
import java.util.Set;

import static ru.hflabs.util.lucene.LuceneQueryUtil.createTermQuery;
import static ru.hflabs.util.lucene.LuceneUtil.valueToTerm;

/**
 * Класс <class>LuceneCriteriaBuilder</class> реализует сервис построения поискового запроса
 *
 * @author Nazin Alexander
 */
public class LuceneCriteriaBuilder<E extends Identifying> extends CriteriaBuilderTemplate<E, LuceneCriteriaHolder, Query> {

    static {
        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
    }

    /** Сервис получаения индексированных полей */
    private Converter<Pair<Class<E>, String>, IndexedField> class2fieldConverter;

    public void setClass2fieldConverter(Converter<Pair<Class<E>, String>, IndexedField> class2fieldConverter) {
        this.class2fieldConverter = class2fieldConverter;
    }

    /**
     * Возвращает {@link org.apache.lucene.search.BooleanClause.Occur} по {@link Condition}
     *
     * @param condition текущая операция
     * @return Возвращает {@link org.apache.lucene.search.BooleanClause.Occur}
     */
    public static BooleanClause.Occur retrieveOccurByLogicalOperation(Condition condition) {
        switch (condition) {
            case OR: {
                return BooleanClause.Occur.SHOULD;
            }
            case AND: {
                return BooleanClause.Occur.MUST;
            }
            case NOT: {
                return BooleanClause.Occur.MUST_NOT;
            }
            default: {
                throw new UnsupportedOperationException(
                        String.format("Condition '%s' not supported by '%s'", condition.name(), LuceneCriteriaBuilder.class.getSimpleName())
                );
            }
        }
    }

    /**
     * Создает запрос для пустого значения
     *
     * @param criteriaClass целевой класс критерии
     * @param criteriaField целевое поле
     * @return Возвращает запрос поиска
     */
    private Query createQueryByEmptyValue(Class<E> criteriaClass, String criteriaField) {
        IndexedField indexedField = class2fieldConverter.convert(Pair.valueOf(criteriaClass, criteriaField));
        if (Date.class.isAssignableFrom(indexedField.getType())) {
            return createTermQuery(indexedField.getName(), LuceneUtil.DATE_MIN_NULL_VALUE);
        } else {
            final BooleanQuery query = new BooleanQuery();
            query.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
            query.add(new TermRangeQuery(criteriaField, null, null, true, true), BooleanClause.Occur.MUST_NOT);
            return query;
        }
    }

    /**
     * Создает запрос для НЕ пустого значения
     *
     * @param criteriaClass целевой класс критерии
     * @param criteriaField целевое поле
     * @return Возвращает запрос поиска
     */
    private Query createQueryByNotEmptyValue(Class<E> criteriaClass, String criteriaField) {
        IndexedField indexedField = class2fieldConverter.convert(Pair.valueOf(criteriaClass, criteriaField));
        if (Date.class.isAssignableFrom(indexedField.getType())) {
            final BooleanQuery query = new BooleanQuery();
            query.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
            query.add(createTermQuery(indexedField.getName(), LuceneUtil.DATE_MIN_NULL_VALUE), BooleanClause.Occur.MUST_NOT);
            return query;
        } else {
            return new TermRangeQuery(indexedField.getName(), null, null, true, true);
        }
    }

    @Override
    public LuceneCriteriaHolder createEmptyCriteria(Class<E> criteriaClass) {
        return new LuceneCriteriaHolder();
    }

    @Override
    protected LuceneCriteriaHolder appendFilterByEmptyValue(LuceneCriteriaHolder current, Class<E> criteriaClass, String criteriaField, FilterCriteriaValue.EmptyValue value) {
        return current.appendPredicate(createQueryByEmptyValue(criteriaClass, criteriaField), value.getCondition());
    }

    @Override
    protected LuceneCriteriaHolder appendFilterByNotEmptyValue(LuceneCriteriaHolder current, Class<E> criteriaClass, String criteriaField, FilterCriteriaValue.NotEmptyValue value) {
        return current.appendPredicate(createQueryByNotEmptyValue(criteriaClass, criteriaField), value.getCondition());
    }

    @Override
    protected LuceneCriteriaHolder appendFilterByBooleanValue(LuceneCriteriaHolder current, Class<E> criteriaClass, String criteriaField, FilterCriteriaValue.BooleanValue value) {
        return current.appendPredicate(createTermQuery(criteriaField, value.getValue()), value.getCondition());
    }

    @Override
    protected LuceneCriteriaHolder appendFilterByEnumValues(LuceneCriteriaHolder current, Class<E> criteriaClass, String criteriaField, FilterCriteriaValue.EnumValues<?> value) {
        BooleanQuery query = new BooleanQuery();
        BooleanClause.Occur occur = retrieveOccurByLogicalOperation(value.getClauseCondition());
        for (Enum<?> enumValue : value.getValue()) {
            query.add(createTermQuery(criteriaField, enumValue), occur);
        }
        return current.appendPredicate(query, value.getCondition());
    }

    @Override
    protected LuceneCriteriaHolder appendFilterByStringValue(LuceneCriteriaHolder current, Class<E> criteriaClass, String criteriaField, FilterCriteriaValue.StringValue value) {
        if (StringUtils.hasText(value.getValue())) {
            return current.appendPredicate(createTermQuery(criteriaField, value.getValue()), value.getCondition());
        }
        return appendFilterByEmptyValue(current, criteriaClass, criteriaField, FilterCriteriaValue.EMPTY_VALUE);
    }

    @Override
    protected LuceneCriteriaHolder appendFilterByDateValue(LuceneCriteriaHolder current, Class<E> criteriaClass, String criteriaField, FilterCriteriaValue.DateValue value) {
        return current.appendPredicate(
                NumericRangeQuery.newLongRange(
                        criteriaField,
                        value.getStart() != null ? LuceneUtil.dateToLong(value.getStart()) : null,
                        value.getEnd() != null ? LuceneUtil.dateToLong(value.getEnd()) : null,
                        true,
                        true
                ),
                value.getCondition()
        );
    }

    @Override
    protected LuceneCriteriaHolder appendFilterByStringsValue(LuceneCriteriaHolder current, Class<E> criteriaClass, String criteriaField, FilterCriteriaValue.StringsValue value) {
        final BooleanQuery query = new BooleanQuery();
        final BooleanClause.Occur occur = retrieveOccurByLogicalOperation(value.getClauseCondition());
        for (String str : value.getValue()) {
            query.add(
                    StringUtils.hasText(str) ? createTermQuery(criteriaField, str) : createQueryByEmptyValue(criteriaClass, criteriaField),
                    occur
            );
        }
        Query rewritedQuery = LuceneQueryUtil.rewriteBooleanQuery(query);
        if (rewritedQuery != null) {
            return current.appendPredicate(rewritedQuery, value.getCondition());
        }
        return current;
    }

    @Override
    protected LuceneCriteriaHolder appendSearch(LuceneCriteriaHolder current, Class<E> criteriaClass, Set<String> values, Condition condition) {
        final BooleanQuery query = new BooleanQuery();
        for (String word : values) {
            if (StringUtils.hasText(word)) {
                query.add(new PrefixQuery(valueToTerm(LuceneBinderTransformer.DEFAULT_SEARCH_FIELD, word)), BooleanClause.Occur.MUST);
            }
        }
        Query rewritedQuery = LuceneQueryUtil.rewriteBooleanQuery(query);
        if (rewritedQuery != null) {
            current.appendPredicate(rewritedQuery, condition);
        }
        return current;
    }

    @Override
    protected LuceneCriteriaHolder appendOrder(LuceneCriteriaHolder current, Class<E> criteriaClass, String orderKey, SortOrder orderValue) {
        IndexedField indexedField = class2fieldConverter.convert(Pair.valueOf(criteriaClass, orderKey));
        SortField.Type sortFieldType = LuceneUtil.sortFieldTypeByClass(indexedField.getType());
        return current.appendOrder(new SortField(orderKey, sortFieldType, SortOrder.DESCENDING.equals(orderValue)));
    }

    @Override
    protected LuceneCriteriaHolder appendDefaultOrder(LuceneCriteriaHolder current, Class<E> criteriaClass) {
        return appendOrder(current, criteriaClass, E.PRIMARY_KEY, SortOrder.ASCENDING);
    }
}
