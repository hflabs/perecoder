package ru.hflabs.rcd.lucene.criteria;

import org.apache.lucene.search.*;
import org.springframework.util.CollectionUtils;
import ru.hflabs.rcd.model.criteria.CriteriaHolder;
import ru.hflabs.rcd.term.Condition;
import ru.hflabs.util.core.collection.ArrayUtil;
import ru.hflabs.util.lucene.LuceneQueryUtil;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Класс <class>LuceneCriteriaHolder</class> содержит информацию о построенной критерии
 *
 * @author Nazin Alexander
 */
public class LuceneCriteriaHolder implements CriteriaHolder<Query> {

    /** Предикаты запроса */
    private final BooleanQuery predicates;
    /** Сортировка запроса */
    private final Set<SortField> orders;
    /** Запрос фильтрации */
    private Filter filter;

    public LuceneCriteriaHolder() {
        this.predicates = new BooleanQuery();
        this.orders = new LinkedHashSet<>();
    }

    public LuceneCriteriaHolder appendPredicate(Query predicate, Condition condition) {
        predicates.add(predicate, LuceneCriteriaBuilder.retrieveOccurByLogicalOperation(condition));
        return this;
    }

    public LuceneCriteriaHolder appendOrder(SortField order) {
        orders.add(order);
        return this;
    }

    public LuceneCriteriaHolder injectFilter(Query query) {
        filter = new QueryWrapperFilter(query);
        return this;
    }

    @Override
    public void appendQuery(Query query, Condition condition) {
        appendPredicate(query, condition);
    }

    @Override
    public Query buildQuery() {
        Query resultQuery = LuceneQueryUtil.rewriteBooleanQuery(predicates);
        if (resultQuery == null) {
            return new MatchAllDocsQuery();
        }
        return resultQuery;
    }

    public Sort buildSort() {
        if (!CollectionUtils.isEmpty(orders)) {
            return new Sort(ArrayUtil.toArray(SortField.class, orders));
        }
        return null;
    }

    public Filter buildFilter() {
        return filter;
    }
}
