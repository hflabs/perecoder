package ru.hflabs.rcd.model.document;

import com.google.common.base.Function;
import ru.hflabs.rcd.model.path.DictionaryNamedPath;
import ru.hflabs.rcd.model.path.FieldNamedPath;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;

/**
 * Класс <class>Contexts</class> реализует вспомогательные методы для создания контекстов документов
 *
 * @see DocumentContext
 */
public abstract class Contexts {

    /** Контекст справочника */
    public static final Function<DocumentContext, Context<DictionaryNamedPath, Dictionary>> DICTIONARY_CONTEXT =
            new Function<DocumentContext, Context<DictionaryNamedPath, Dictionary>>() {

                @Override
                public Context<DictionaryNamedPath, Dictionary> apply(DocumentContext input) {
                    return new NamedContext.DictionaryContext(input);
                }
            };

    /** Контекст МЕТА-поля */
    public static final Function<DocumentContext, Context<MetaFieldNamedPath, MetaField>> META_FIELD_CONTEXT =
            new Function<DocumentContext, Context<MetaFieldNamedPath, MetaField>>() {

                @Override
                public Context<MetaFieldNamedPath, MetaField> apply(DocumentContext input) {
                    return new NamedContext.MetaFieldContext(input);
                }
            };

    /** Контекст значения поля */
    public static final Function<DocumentContext, Context<FieldNamedPath, Field>> FIELD_CONTEXT =
            new Function<DocumentContext, Context<FieldNamedPath, Field>>() {

                @Override
                public Context<FieldNamedPath, Field> apply(DocumentContext input) {
                    return new NamedContext.FieldContext(input);
                }
            };

    protected Contexts() {
        // embedded constructor
    }

    /**
     * Выполняет создание контекста документа по значению поля с установленными транзитивными зависимостями
     *
     * @param field значение поля
     * @return Возвращает контекст документа
     */
    public static DocumentContext createDocumentContext(Field field) {
        MetaField metaField = field.getRelative();
        assert metaField != null : "MetaField must be not NULL";
        Dictionary dictionary = metaField.getRelative();
        assert dictionary != null : "Dictionary must be not NULL";
        Group group = dictionary.getRelative();
        assert group != null : "Group must be not NULL";
        return new DocumentContext(group, dictionary, metaField, field);
    }
}
