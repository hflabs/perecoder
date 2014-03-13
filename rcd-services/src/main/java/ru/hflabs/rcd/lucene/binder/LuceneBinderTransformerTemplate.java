package ru.hflabs.rcd.lucene.binder;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.index.IndexedClass;
import ru.hflabs.rcd.lucene.IndexBinderTransformer;
import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.service.ISingleClassObserver;
import ru.hflabs.util.lucene.LuceneUtil;

import java.util.Arrays;
import java.util.Collection;

import static ru.hflabs.util.lucene.LuceneUtil.KRYO_OBJECT_TO_BYTE_CONVERTER;

/**
 * Класс <class>LuceneBinderTransformerTemplate</class> реализует базовый сервис преобразования поисковой сущности в сущность API
 *
 * @author Nazin Alexander
 */
public abstract class LuceneBinderTransformerTemplate<E extends Identifying> implements IndexBinderTransformer<E, String>, ISingleClassObserver<E> {

    /** Статус сохранения поисковых полей */
    public static final Field.Store FIELD_STORED = Field.Store.NO;
    /** Описание индексации */
    private final IndexedClass<E> indexedClass;

    public LuceneBinderTransformerTemplate(IndexedClass<E> indexedClass) {
        this.indexedClass = indexedClass;
    }

    @Override
    public IndexedClass<E> retrieveIndexedClass() {
        return indexedClass;
    }

    @Override
    public Class<E> retrieveTargetClass() {
        return indexedClass.getIndexedClass();
    }

    @Override
    public Term getPrimaryKey(E essence) {
        return new Term(E.PRIMARY_KEY, essence.getId());
    }

    @Override
    public String getPrimaryKey(Document document) {
        return document.get(E.PRIMARY_KEY);
    }

    @Override
    public E convert(Document source) {
        return LuceneUtil.byteToObject(retrieveTargetClass(), source.getBinaryValue(OBJECT_FIELD), KRYO_OBJECT_TO_BYTE_CONVERTER);
    }

    /**
     * Создает поле поиска по умолчанию
     *
     * @param values коллекция значений для поиска
     * @return Возвращает поле поиска поумолчанию
     */
    protected static IndexableField createDefaultSearchField(Iterable<IndexableField> values) {
        ImmutableSet.Builder<String> defaultValues = ImmutableSet.builder();
        for (IndexableField field : values) {
            String value = field.stringValue();
            if (value != null) {
                Collection<String> worlds = Collections2.filter(
                        Arrays.asList(LuceneUtil.SPACE_REGEXP.split(value)),
                        new Predicate<String>() {
                            @Override
                            public boolean apply(String o) {
                                return StringUtils.hasText(o);
                            }
                        }
                );
                defaultValues.addAll(worlds);
            }
        }
        return new TextField(DEFAULT_SEARCH_FIELD, StringUtils.collectionToDelimitedString(defaultValues.build(), " "), FIELD_STORED);
    }

    /**
     * Выполняет подготовку сущности к сериализации - удаление транзитивных полей и т.д.
     *
     * @param target целевая сущность
     * @return Возвращает подготовленную сущность
     */
    protected abstract E prepareToSerialize(E target);
}
