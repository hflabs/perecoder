package ru.hflabs.rcd.model.document;

import lombok.Getter;
import ru.hflabs.rcd.model.path.DictionaryNamedPath;
import ru.hflabs.rcd.model.path.FieldNamedPath;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;

/**
 * Класс <class>NamedContext</class> содержит информацию об именованном контексте
 *
 * @param <NP> именованный контекст
 * @see DocumentContext
 */
@Getter
public abstract class NamedContext<NP, I> implements Context<NP, I> {

    /** Именованный путь контекста */
    protected final NP namedPath;
    /** Контекст документа */
    protected final DocumentContext documentContext;

    public NamedContext(DocumentContext documentContext, NP namedPath) {
        this.namedPath = namedPath;
        this.documentContext = documentContext;
    }

    @Override
    public int hashCode() {
        return namedPath.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NamedContext that = (NamedContext) o;

        if (!namedPath.equals(that.namedPath)) {
            return false;
        }

        return true;
    }

    /**
     * Класс <class>DictionaryContext</class> содержит контекст справочника
     *
     * @see DictionaryNamedPath
     */
    public static final class DictionaryContext extends NamedContext<DictionaryNamedPath, Dictionary> {

        public DictionaryContext(DocumentContext documentContext) {
            super(documentContext, new DictionaryNamedPath(
                    documentContext.group.getName(), documentContext.dictionary.getName())
            );
        }

        @Override
        public Dictionary getEssence() {
            return documentContext.dictionary;
        }
    }

    /**
     * Класс <class>MetaFieldContext</class> содержит контекст МЕТА-поля
     *
     * @see MetaFieldNamedPath
     */
    public static final class MetaFieldContext extends NamedContext<MetaFieldNamedPath, MetaField> {

        public MetaFieldContext(DocumentContext documentContext) {
            super(documentContext, new MetaFieldNamedPath(
                    documentContext.group.getName(), documentContext.dictionary.getName(), documentContext.metaField.getName())
            );
        }

        @Override
        public MetaField getEssence() {
            return documentContext.metaField;
        }
    }

    /**
     * Класс <class>FieldContext</class> содержит контекст значения поля
     *
     * @see FieldNamedPath
     */
    public static final class FieldContext extends NamedContext<FieldNamedPath, Field> {

        public FieldContext(DocumentContext documentContext) {
            super(documentContext, new FieldNamedPath(
                    documentContext.group.getName(), documentContext.dictionary.getName(), documentContext.metaField.getName(), documentContext.field.getValue())
            );
        }

        @Override
        public Field getEssence() {
            return documentContext.field;
        }
    }
}
