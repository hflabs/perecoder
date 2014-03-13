package ru.hflabs.rcd.index;

import lombok.Getter;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Класс <class>IndexedClass</class> реализует описание индексированного класса
 *
 * @see ru.hflabs.rcd.model.annotation.Indexed
 */
@Getter
public final class IndexedClass<E> extends IndexedField.ByField {

    /** Целевой класс */
    private final Class<E> indexedClass;
    /** Коллекция индексированных полей */
    private final Collection<IndexedField> fields;

    public IndexedClass(Class<E> indexedClass, Field primaryField, Collection<IndexedField> fields) {
        super(FILTERABLE, primaryField);
        this.indexedClass = indexedClass;
        this.fields = fields;
    }
}
