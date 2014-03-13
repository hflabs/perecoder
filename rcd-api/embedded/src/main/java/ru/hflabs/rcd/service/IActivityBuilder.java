package ru.hflabs.rcd.service;

import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.criteria.CriteriaHolder;
import ru.hflabs.rcd.model.criteria.FilterActivity;

/**
 * Интерфейс <class>IActivityBuilder</class> декларирует методы построение критерия активности документов
 *
 * @see FilterActivity
 */
public interface IActivityBuilder<E extends Identifying, C extends CriteriaHolder<Q>, Q> {

    /**
     * Проверяет и возвращает <code>TRUE</code>, если сервис поддерживает текущий класс критерии
     *
     * @param targetClass проверяемый класс
     * @return Возвращает флаг проверки
     */
    boolean isSupport(Class<?> targetClass);

    /**
     * Выполняет построение критерия активности документов
     *
     * @param current текущий критерий
     * @param criteriaClass целевой класс критерии
     * @param activity активность документов
     * @return Возвращает модифицированный критерий
     */
    C createActivity(C current, Class<E> criteriaClass, FilterActivity activity);
}
