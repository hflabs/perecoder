package ru.hflabs.rcd.lucene.criteria;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import ru.hflabs.rcd.model.Historical;
import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.change.ChangeType;
import ru.hflabs.rcd.model.criteria.FilterActivity;
import ru.hflabs.rcd.service.IActivityBuilder;
import ru.hflabs.util.lucene.LuceneQueryUtil;
import ru.hflabs.util.spring.Assert;

/**
 * Класс <class>LuceneHistoricalActivityBuilder</class> реализует методы построения критерии историцируемых документов
 *
 * @see Historical
 */
public class LuceneHistoricalActivityBuilder<E extends Identifying> implements IActivityBuilder<E, LuceneCriteriaHolder, Query> {

    @Override
    public boolean isSupport(Class<?> targetClass) {
        return Historical.class.isAssignableFrom(targetClass);
    }

    @Override
    public LuceneCriteriaHolder createActivity(LuceneCriteriaHolder current, Class<E> criteriaClass, FilterActivity activity) {
        Assert.isTrue(isSupport(criteriaClass), String.format("Class '%s' not supported by '%s'", criteriaClass.getName(), getClass().getName()));
        if (activity != null && !FilterActivity.ALL.equals(activity)) {
            BooleanQuery query = new BooleanQuery();
            BooleanClause.Occur condition = LuceneCriteriaBuilder.retrieveOccurByLogicalOperation(activity.getClauseCondition());
            if (BooleanClause.Occur.MUST_NOT.equals(condition)) {
                query.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
            }
            for (ChangeType changeType : activity.getValue()) {
                query.add(LuceneQueryUtil.createTermQuery(Historical.CHANGE_TYPE, changeType), condition);
            }
            current.injectFilter(query);
        }
        return current;
    }
}
