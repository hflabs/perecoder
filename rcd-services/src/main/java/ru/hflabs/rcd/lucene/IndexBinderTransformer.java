package ru.hflabs.rcd.lucene;

import ru.hflabs.rcd.index.IndexedClass;
import ru.hflabs.util.lucene.LuceneBinderTransformer;

import java.io.Serializable;

/**
 * Интерфейс <class>IndexBinderTransformer</class> декларирует методы для связи сущности API и поисковой сущности
 *
 * @see IndexedClass
 */
public interface IndexBinderTransformer<E, PK extends Serializable> extends LuceneBinderTransformer<E, PK> {

    /**
     * Возвращает описание индексированного класса
     *
     * @return Возвращает описание индексированного класса
     */
    IndexedClass<E> retrieveIndexedClass();
}
