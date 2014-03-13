package ru.hflabs.rcd.service;

import com.google.common.collect.Lists;
import ru.hflabs.rcd.model.Descriptioned;
import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.Named;
import ru.hflabs.rcd.model.Permissioned;
import ru.hflabs.rcd.model.document.MetaField;

import java.util.Collection;

import static ru.hflabs.rcd.accessor.Accessors.*;

/**
 * Класс <class>MergeServices</class> реализует вспомогательные методы для агрегирования сервисов слияния сущностей
 *
 * @see IMergeService
 * @see IMergeService.Single
 */
public abstract class MergeServices {

    /** Заглушка - возвращает всегда новый объект */
    public static final IMergeService.Single<?> DUMMY = new IMergeService.Single() {
        @Override
        public Object merge(Object newEssence, Object oldEssence) {
            return newEssence;
        }
    };

    /** Сервис копирования идентификаторов */
    public static final IMergeService.Single<? extends Identifying> IDENTIFIER = new IMergeService.Single<Identifying>() {
        @Override
        public Identifying merge(Identifying newEssence, Identifying oldEssence) {
            return (oldEssence != null) ? injectId(newEssence, oldEssence.getId()) : newEssence;
        }
    };
    /** Сервис копирования прав доступа */
    public static final IMergeService.Single<? extends Permissioned> PERMISSION = new IMergeService.Single<Permissioned>() {
        @Override
        public Permissioned merge(Permissioned newEssence, Permissioned oldEssence) {
            if (newEssence != null && oldEssence != null) {
                newEssence.setPermissions(oldEssence.getPermissions());
            }
            return newEssence;
        }
    };
    /** Сервис копирования названия */
    public static final IMergeService.Single<? extends Named> NAME = new IMergeService.Single<Named>() {
        @Override
        public Named merge(Named newEssence, Named oldEssence) {
            return (oldEssence != null) ? injectName(newEssence, oldEssence.getName()) : newEssence;
        }
    };
    /** Сервис копирования описания */
    public static final IMergeService.Single<? extends Descriptioned> DESCRIPTION = new IMergeService.Single<Descriptioned>() {
        @Override
        public Descriptioned merge(Descriptioned newEssence, Descriptioned oldEssence) {
            return (oldEssence != null) ? injectDescription(newEssence, oldEssence.getDescription()) : newEssence;
        }
    };

    protected MergeServices() {
        // embedded constructor
    }

    @SuppressWarnings("unchecked")
    public static <T> IMergeService.Single<T> dummy() {
        return (IMergeService.Single<T>) DUMMY;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Identifying> IMergeService.Single<T> copyId() {
        return (IMergeService.Single<T>) IDENTIFIER;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Permissioned> IMergeService.Single<T> copyPermission() {
        return (IMergeService.Single<T>) PERMISSION;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Named> IMergeService.Single<T> copyName() {
        return (IMergeService.Single<T>) NAME;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Descriptioned> IMergeService.Single<T> copyDescription() {
        return (IMergeService.Single<T>) DESCRIPTION;
    }

    @SafeVarargs
    public static <T> IMergeService.Single<T> chain(IMergeService.Single<T>... components) {
        return new ChainMergeService<>(Lists.newArrayList(components));
    }

    /**
     * Класс <class>MetaFieldMergeService</class> реслизует сервис слияния МЕТА-полей с сохранением старых значений флагов
     */
    public static class MetaFieldFlagsMergeService implements IMergeService.Single<MetaField> {

        @Override
        public MetaField merge(MetaField newEssence, MetaField oldEssence) {
            if (newEssence != null && oldEssence != null) {
                newEssence.setFlags(oldEssence.getFlags());
            }
            return newEssence;
        }
    }

    /**
     * Класс <class>ChainMergeService</class> реализует сервис слияния, который в качестве новой сущности
     * передает результат слияния предыдущего сервиса
     */
    public static class ChainMergeService<T> implements IMergeService.Single<T> {

        /** Коллекция сервисов */
        private final Collection<IMergeService.Single<T>> components;

        private ChainMergeService(Collection<IMergeService.Single<T>> components) {
            this.components = components;
        }

        @Override
        public T merge(T newEssence, T oldEssence) {
            T result = newEssence;
            for (IMergeService<T, T, T> service : components) {
                result = service.merge(result, oldEssence);
            }
            return result;
        }
    }
}
