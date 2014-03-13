package ru.hflabs.rcd.model.change;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import ru.hflabs.rcd.model.Historical;
import ru.hflabs.rcd.model.Named;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.task.TaskDescriptor;
import ru.hflabs.util.core.collection.ArrayUtil;

import java.util.Collection;
import java.util.EnumSet;

/**
 * Класс <class>Predicates</class> реализует вспомогательные методы для построения предикатов по различным критериям
 *
 * @see Predicate
 */
public abstract class Predicates {

    /** Предикат, отслеживающий изменения именованного документа */
    public static final Predicate<Collection<Diff>> CHANGE_NAME_PREDICATE = new Predicate<Collection<Diff>>() {
        @Override
        public boolean apply(Collection<Diff> input) {
            for (Diff diff : input) {
                if (Named.NAME.equals(diff.getField())) {
                    return true;
                }
            }
            return false;
        }
    };
    /** Предикат, отслеживающий изменение значение поля */
    public static final Predicate<Collection<Diff>> CHANGE_VALUE_PREDICATE = new Predicate<Collection<Diff>>() {
        @Override
        public boolean apply(Collection<Diff> input) {
            for (Diff diff : input) {
                if (Field.VALUE.equals(diff.getField())) {
                    return true;
                }
            }
            return false;
        }
    };
    /** Предикат, отслеживающий изменение триггера задачи */
    public static final Predicate<Collection<Diff>> CHANGE_CRON_PREDICATE = new Predicate<Collection<Diff>>() {
        @Override
        public boolean apply(Collection<Diff> input) {
            for (Diff diff : input) {
                if (TaskDescriptor.CRON.equals(diff.getField())) {
                    return true;
                }
            }
            return false;
        }
    };

    /** Предикат, отслеживающий флаг первичного ключа МЕТА-поля */
    public static final Predicate<MetaField> PRIMARY_META_FIELD_PREDICATE = new Predicate<MetaField>() {
        @Override
        public boolean apply(MetaField input) {
            return input != null && input.isFlagEstablished(MetaField.FLAG_PRIMARY);
        }
    };
    /** Предикат, отслеживающий флаг первичного ключа МЕТА-поля */
    public static final Predicate<MetaField> UNIQUE_META_FIELD_PREDICATE = new Predicate<MetaField>() {
        @Override
        public boolean apply(MetaField input) {
            return input != null && input.isFlagEstablished(MetaField.FLAG_UNIQUE);
        }
    };
    /** Предикат, отслеживающий флаг первичного ключа МЕТА-поля */
    public static final Predicate<MetaField> HIDDEN_META_FIELD_PREDICATE = new Predicate<MetaField>() {
        @Override
        public boolean apply(MetaField input) {
            return input != null && input.isFlagEstablished(MetaField.FLAG_HIDDEN);
        }
    };
    /** Предикат, отслеживающий флаг первичного ключа МЕТА-поля */
    public static final Predicate<MetaField> NOT_HIDDEN_META_FIELD_PREDICATE = new Predicate<MetaField>() {
        @Override
        public boolean apply(MetaField input) {
            return input != null && !HIDDEN_META_FIELD_PREDICATE.apply(input);
        }
    };

    protected Predicates() {
        // embedded constructor
    }

    /**
     * Формирует и возвращает предикат, проверяющий значение функции на <code>NULL</code>
     *
     * @param function функция
     * @return Возвращает созданный предикат
     */
    public static <T> Predicate<T> notNull(final Function<T, ?> function) {
        return new Predicate<T>() {

            @Override
            public boolean apply(T input) {
                return function.apply(input) != null;
            }
        };
    }

    /**
     * Формирует и возвращает предикат для исторических сущностей по типам их изменений
     *
     * @param types целевая коллекция типов или <code>NULL</code>
     * @return Возвращает созданный предикат
     */
    public static <T extends Historical> Predicate<T> changeTypes(ChangeType... types) {
        final Collection<ChangeType> targetTypes = ArrayUtil.isEmpty(types) ?
                EnumSet.allOf(ChangeType.class) :
                Sets.newHashSet(types);
        return new Predicate<T>() {
            @Override
            public boolean apply(T input) {
                return input != null && targetTypes.contains(input.getChangeType());
            }
        };
    }
}
