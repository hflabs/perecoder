package ru.hflabs.rcd.service;

/**
 * Интерфейс <class>IMergeService</class> декларирует методы сервиса слияния сущностей
 *
 * @param <F> источник
 * @param <T> назначение
 * @param <R> результат слияния
 */
public interface IMergeService<F, T, R> {

    /**
     * Выполняет слияние сущностей
     *
     * @param newEssence новая сущность
     * @param oldEssence старая сущность
     * @return Возвращает слитую сущность
     */
    R merge(F newEssence, T oldEssence);

    /**
     * Интерфейс <class>Single</class> реализует маркерный интерфейс для слияния сущностей одного типа
     *
     * @param <E> класс сущностей
     */
    interface Single<E> extends IMergeService<E, E, E> {
    }
}
