package ru.hflabs.rcd.task.performer.synchronization;

import com.google.common.base.Function;
import ru.hflabs.rcd.service.IMergeService;
import ru.hflabs.rcd.service.ISingleClassObserver;
import ru.hflabs.rcd.service.MergeServices;

import java.util.Collection;
import java.util.Collections;

/**
 * Интерфейс <class>SynchronizeCallback</class> декларирует методы для построения истории изменения коллекции сущностей
 *
 * @param <T> класс сущности
 * @author Nazin Alexander
 */
public interface SynchronizeCallback<T> extends ISingleClassObserver<T> {

    /** Возвращает целевые значения */
    Collection<T> getTarget();

    /** Возвращает функцию для построение карты уникальных объектов */
    Function<T, String> getUniqueFunction();

    /** Возвращает функцию слияния старого и нового объекта */
    IMergeService.Single<T> getMergeFunction();

    /**
     * Реализует адартер для синхронизации динамических значений
     *
     * @param <T> целевой класс
     */
    class Adapter<T> implements SynchronizeCallback<T> {

        /** Целевой класс */
        private final Class<T> targetClass;
        /** Целевые значения */
        private final Collection<T> target;
        /** Функция уникальных значений */
        private final Function<T, String> uniqueFunction;
        /** Функция слияния */
        private final IMergeService.Single<T> mergeFunction;

        public Adapter(Class<T> targetClass, Collection<T> target, Function<T, String> uniqueFunction, IMergeService.Single<T> mergeFunction) {
            this.targetClass = targetClass;
            this.target = target;
            this.uniqueFunction = uniqueFunction;
            this.mergeFunction = mergeFunction;
        }

        @Override
        public Class<T> retrieveTargetClass() {
            return targetClass;
        }

        @Override
        public Collection<T> getTarget() {
            return target;
        }

        @Override
        public Function<T, String> getUniqueFunction() {
            return uniqueFunction;
        }

        @Override
        public IMergeService.Single<T> getMergeFunction() {
            return mergeFunction;
        }
    }

    /**
     * Реализует адаптер для синхронизации с пустыми значениями
     *
     * @param <T> целевой класс
     */
    final class Empty<T> extends Adapter<T> {

        public Empty(Class<T> targetClass, Function<T, String> uniqueFunction) {
            super(targetClass, Collections.<T>emptyList(), uniqueFunction, MergeServices.<T>dummy());
        }
    }
}
