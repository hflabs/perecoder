package ru.hflabs.rcd.model.criteria;

import ru.hflabs.rcd.term.Condition;

/**
 * Интерфейс <class>CriteriaHolder</class> декларирует методы кеша критерии поиска сущностей
 *
 * @see FilterCriteriaValue
 */
public interface CriteriaHolder<Q> {

    /**
     * Добавляет к существующей критерии подзапрос
     *
     * @param query добавляемый подзапрос
     * @param condition условие запроса
     */
    void appendQuery(Q query, Condition condition);

    /**
     * @return Возвращает построенный запрос
     */
    Q buildQuery();
}
