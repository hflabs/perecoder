package ru.hflabs.rcd.model;

import org.springframework.util.comparator.NullSafeComparator;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.document.Record;

import java.util.Comparator;

/**
 * Класс <class>Comparators</class> реализует вспомогательные сервисы сравнения сущностей
 *
 * @see Comparator
 */
public abstract class Comparators {

    /** Сервис сравнения сущностей по их идентификаторам */
    public static final Comparator<Identifying> IDENTIFYING_COMPARATOR = new Comparator<Identifying>() {
        @Override
        @SuppressWarnings("unchecked")
        public int compare(Identifying o1, Identifying o2) {
            return NullSafeComparator.NULLS_LOW.compare(o1.getId(), o2.getId());
        }
    };
    /** Сервис сравнения МЕТА полей по их позиции */
    public static final Comparator<MetaField> META_FIELD_ORDINAL_COMPARATOR = new Comparator<MetaField>() {
        @Override
        @SuppressWarnings("unchecked")
        public int compare(MetaField o1, MetaField o2) {
            return NullSafeComparator.NULLS_LOW.compare(o1.getOrdinal(), o2.getOrdinal());
        }
    };
    /** Сервис сравнения значений полей */
    public static final Comparator<Field> FIELD_VALUE_COMPARATOR = new Comparator<Field>() {
        @Override
        @SuppressWarnings("unchecked")
        public int compare(Field o1, Field o2) {
            return NullSafeComparator.NULLS_LOW.compare(o1.getValue(), o2.getValue());
        }
    };

    /** Сервис сравнения записей по предопределенному МЕТА-полю */
    public static final class RecordComparator implements Comparator<Record> {

        /** Сервис сравнения значений полей */
        private final Comparator<Field> fieldValueComparator = new NullSafeComparator<>(FIELD_VALUE_COMPARATOR, true);
        /** Название МЕТА-поля */
        private final String metaFieldName;

        public RecordComparator(String metaFieldName) {
            this.metaFieldName = metaFieldName;
        }

        @Override
        public int compare(Record o1, Record o2) {
            Field o1field = o1.retrieveFieldByName(metaFieldName);
            Field o2field = o2.retrieveFieldByName(metaFieldName);
            return fieldValueComparator.compare(o1field, o2field);
        }
    }

    protected Comparators() {
        // embedded constructor
    }
}
