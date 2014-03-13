package ru.hflabs.rcd.lucene.binder;

import com.google.common.collect.ImmutableList;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.ReflectionUtils;
import ru.hflabs.rcd.Directories;
import ru.hflabs.rcd.index.IndexedClass;
import ru.hflabs.rcd.index.IndexedField;
import ru.hflabs.rcd.lucene.IndexBinderTransformer;
import ru.hflabs.rcd.model.annotation.Indexed;
import ru.hflabs.rcd.service.IServiceFactory;
import ru.hflabs.util.core.Holder;
import ru.hflabs.util.core.Pair;
import ru.hflabs.util.javac.InMemoryJavaFileObject;
import ru.hflabs.util.javac.RuntimeCompiler;
import ru.hflabs.util.javac.RuntimeCompilerUtil;
import ru.hflabs.util.lucene.LuceneBinderTransformer;
import ru.hflabs.util.spring.Assert;
import ru.hflabs.util.spring.util.ReflectionUtil;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;
import java.util.Date;

/**
 * Класс <class>LuceneBinderTransformerFactory</class> реализует фабрику создания <i>runtime</i> сервисов связи поисковой сущности и сущности API
 *
 * @author Nazin Alexander
 * @see IndexBinderTransformer
 * @see Indexed
 */
public class LuceneBinderTransformerFactory<E, PK extends Serializable> implements IServiceFactory<IndexBinderTransformer<E, PK>, Class<E>>, Converter<Pair<Class<E>, String>, IndexedField> {

    /** Кеш сервисов */
    private final Holder<Class<E>, IndexBinderTransformer<E, PK>> serviceHolder;
    /** Кеш полей */
    private final Holder<Class<E>, IndexedClass<E>> indexedFieldsHolder;
    /** Кеш игнорируемых полей */
    private final Holder<Class<E>, Collection<Member>> collectionMembersHolder;

    public LuceneBinderTransformerFactory() {
        this.serviceHolder = new ServicesHolder();
        this.indexedFieldsHolder = new IndexedFieldsHolder();
        this.collectionMembersHolder = new CollectionMembersHolder();
    }

    @Override
    public IndexBinderTransformer<E, PK> retrieveService(Class<E> key) {
        return serviceHolder.getValue(key);
    }

    @Override
    public void destroyService(Class<E> key, IndexBinderTransformer<E, PK> service) {
        serviceHolder.removeValue(key);
    }

    @Override
    public IndexedField convert(Pair<Class<E>, String> pair) {
        IndexedClass<E> indexedClass = retrieveService(pair.first).retrieveIndexedClass();
        // Проверяем первичный ключ
        if (indexedClass.getName().equals(pair.second)) {
            return indexedClass;
        } else {
            // Т.к. поля не уникальны, то ищем первое поле с подходящим именем
            for (IndexedField indexedField : indexedClass.getFields()) {
                if (pair.second.equals(indexedField.getName())) {
                    return indexedField;
                }
            }
            // Поля с указанным именем не найдено в целевом классе
            throw new IllegalArgumentException(String.format("Field with name '%s' not indexed in class '%s'", pair.second, pair.first.getName()));
        }
    }

    /**
     * Создает новый экземпляр класса
     *
     * @param memoryClass исходный класс
     * @return Возвращает созданный экземпляр класса
     */
    private IndexBinderTransformer<E, PK> createInstance(Class<E> targetClass, Class<IndexBinderTransformer<E, PK>> memoryClass) throws Exception {
        return memoryClass.getConstructor(IndexedClass.class).newInstance(indexedFieldsHolder.getValue(targetClass));
    }

    private InMemoryJavaFileObject<IndexBinderTransformer<E, PK>> buildInstance(Class<E> targetClass) {
        final StringWriter writer = new StringWriter();
        final PrintWriter out = new PrintWriter(writer);
        // Определяем пакет класса
        final String packageName = getClass().getPackage().getName();
        out.format("package %s;", packageName).println();
        out.println();
        // Добавляем необходимые import-ы
        buildImports(out, targetClass);

        // Формируем заголовок класса
        RuntimeCompilerUtil.printPublicClassHeaderTemplate(
                out,
                getClass().getName(),
                String.format("extends %s<%s>", LuceneBinderTransformerTemplate.class.getSimpleName(), targetClass.getSimpleName())
        );

        // Формирование конструктора
        buildConstructor(out, RuntimeCompilerUtil.CLASS_NAME_PLACEHOLDER, targetClass);
        // Формирование методов
        buildMethod_prepareToSerialize(out, targetClass);
        buildMethod_reverseConvert(out, targetClass);

        out.println("}");
        out.close();

        return RuntimeCompilerUtil.createJavaFileObject(
                packageName,
                targetClass.getSimpleName() + LuceneBinderTransformer.class.getSimpleName(),
                writer.toString()
        );
    }

    /**
     * Формирование импортов
     *
     * @param out поток вывода
     * @param targetClass целевой класс
     */
    protected void buildImports(PrintWriter out, Class<E> targetClass) {
        out.format("import %s;", ru.hflabs.rcd.index.IndexedClass.class.getName()).println();
        out.format("import %s;", targetClass.getName()).println();
        out.println();
        out.format("import %s;", org.apache.lucene.document.Document.class.getName()).println();
        out.format("import %s;", org.apache.lucene.document.LongField.class.getName()).println();
        out.format("import %s;", org.apache.lucene.document.StoredField.class.getName()).println();
        out.format("import %s;", org.apache.lucene.document.StringField.class.getName()).println();
        out.format("import %s;", org.apache.lucene.document.TextField.class.getName()).println();
        out.format("import %s;", org.apache.lucene.index.IndexableField.class.getName()).println();
        out.format("import %s;", ru.hflabs.util.core.FormatUtil.class.getName()).println();
        out.format("import %s;", ru.hflabs.util.lucene.LuceneUtil.class.getName()).println();
        out.println();
        out.format("import %s;", java.util.ArrayList.class.getName()).println();
        out.format("import %s;", java.util.List.class.getName()).println();
        out.println();
    }

    /**
     * Формирование конструктора
     *
     * @param out поток вывода
     * @param targetClass целевой класс
     */
    private void buildConstructor(PrintWriter out, String className, Class<E> targetClass) {
        out.format("    public %s(IndexedClass<%s> indexedClass) {", className, targetClass.getSimpleName()).println();
        out.println("        super(indexedClass);");
        out.println("    }");
        out.println();
    }

    /**
     * Выполняет построение метода  {@link LuceneBinderTransformerTemplate#prepareToSerialize(ru.hflabs.rcd.model.Identifying)}
     *
     * @param out поток вывода
     * @param targetClass целевой класс
     */
    private void buildMethod_prepareToSerialize(PrintWriter out, Class<E> targetClass) {
        out.println("    @Override");
        out.format("    protected %s prepareToSerialize(%s target) {", targetClass.getSimpleName(), targetClass.getSimpleName()).println();
        final Collection<Member> collectionMembers = collectionMembersHolder.getValue(targetClass);
        // Если в объекте есть поля, которые представляют собой коллекцию, то клонируем объект и обнуляем их
        if (!collectionMembers.isEmpty()) {
            out.format("        final %s toStore = target.copy();", targetClass.getSimpleName()).println();
            for (Member field : collectionMembers) {
                out.format("        toStore.%s(null);", RuntimeCompilerUtil.set(field)).println();
            }
            out.println("        return toStore;");
        } else {
            out.println("        return target;");
        }
        out.println("    }");
        out.println();
    }

    /**
     * Выполняет построение метода  {@link LuceneBinderTransformer#reverseConvert(Object)}
     *
     * @param out поток вывода
     * @param targetClass целевой класс
     */
    private void buildMethod_reverseConvert(PrintWriter out, Class<E> targetClass) {
        out.println("    @Override");
        out.format("    public Document reverseConvert(%s target) {", targetClass.getSimpleName()).println();
        out.println("        final Document result = new Document();");
        out.println();
        // Получаем индексированный класс
        IndexedClass<E> indexedClass = indexedFieldsHolder.getValue(targetClass);
        // Добавляем первичный ключ
        out.format("        // %s", indexedClass.getName()).println();
        out.format("        result.add(new StringField(\"%s\", FormatUtil.format(target.%s()), FIELD_STORED));", indexedClass.getName(), RuntimeCompilerUtil.get(indexedClass.getMember())).println();
        out.println();
        // Список полей поиска поумолчанию
        out.println("        final List<IndexableField> defaultSearchFields = new ArrayList<IndexableField>();");
        // Текущее обрабатываемое поле
        out.println("        IndexableField currentField;");
        // Добавляем индексируемые поля
        for (IndexedField indexedField : indexedClass.getFields()) {
            final Member member = indexedField.getMember();
            final String memberName = indexedField.getName();
            final Class<?> fieldClass = indexedField.getType();
            out.format("        // %s", memberName).println();
            if (String.class.isAssignableFrom(fieldClass)) {
                out.format("        currentField = new TextField(\"%s\", FormatUtil.format(target.%s()), FIELD_STORED);", memberName, RuntimeCompilerUtil.get(member)).println();
            } else if (Date.class.isAssignableFrom(fieldClass)) {
                out.format("        currentField = new LongField(\"%s\", LuceneUtil.dateToLong(target.%s()), FIELD_STORED);", memberName, RuntimeCompilerUtil.get(member)).println();
            } else if (Number.class.isAssignableFrom(fieldClass)) {
                out.format("        currentField = new TextField(\"%s\", FormatUtil.format(target.%s()),FIELD_STORED);", memberName, RuntimeCompilerUtil.get(member)).println();
            } else if (Enum.class.isAssignableFrom(fieldClass)) {
                out.format("        currentField = new TextField(\"%s\", FormatUtil.format(target.%s(), false), FIELD_STORED);", memberName, RuntimeCompilerUtil.get(member)).println();
            } else {
                throw new UnsupportedOperationException(String.format("Class '%s' not supported by %s", fieldClass.getSimpleName(), getClass().getName()));
            }
            // Добавляем поле в фильтрацию
            if (indexedField.isStateEnabled(IndexedField.FILTERABLE)) {
                out.println("        result.add(currentField);");
            }
            // Добавляем в поиск по умолчанию
            if (indexedField.isStateEnabled(IndexedField.SEARCHABLE)) {
                out.println("        defaultSearchFields.add(currentField);");
            }
        }
        // Добавляем поле поиска по умолчанию
        out.format("        // %s", LuceneBinderTransformer.DEFAULT_SEARCH_FIELD).println();
        out.println("        result.add(createDefaultSearchField(defaultSearchFields));");
        out.println();
        // Добавляем сериализованный объект
        out.format("        // %s", LuceneBinderTransformer.OBJECT_FIELD).println();
        out.format("        result.add(new StoredField(OBJECT_FIELD, LuceneUtil.objectToByte(prepareToSerialize(target), LuceneUtil.KRYO_OBJECT_TO_BYTE_CONVERTER)));").println();
        out.println();
        out.println("        return result;");
        out.println("    }");
        out.println();
    }

    /**
     * Класс <class>ServicesHolder</class> реализует кеш сервисов
     *
     * @author Nazin Alexander
     */
    private class ServicesHolder extends Holder<Class<E>, IndexBinderTransformer<E, PK>> {

        @Override
        protected IndexBinderTransformer<E, PK> createValue(Class<E> key) {
            try {
                return createInstance(
                        key,
                        RuntimeCompiler.compileIfNotFound(
                                buildInstance(key),
                                System.out,
                                Directories.RUNTIME_FOLDER_SOURCE.location,
                                Directories.RUNTIME_FOLDER_COMPILED.location
                        )
                );
            } catch (Exception ex) {
                throw new UndeclaredThrowableException(ex);
            }
        }
    }

    /**
     * Класс <class>CollectionMembersHolder</class> реализует кеш полей, которые являются коллекциями
     *
     * @author Nazin Alexander
     */
    private class CollectionMembersHolder extends Holder<Class<E>, Collection<Member>> {

        @Override
        protected Collection<Member> createValue(Class<E> key) {
            final ImmutableList.Builder<Member> result = ImmutableList.builder();
            ReflectionUtil.doWithFields(
                    key,
                    new ReflectionUtils.FieldCallback() {
                        @Override
                        public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                            result.add(field);
                        }
                    },
                    new ReflectionUtils.FieldFilter() {
                        @Override
                        public boolean matches(Field field) {
                            return Collection.class.isAssignableFrom(field.getType());
                        }
                    }
            );
            return result.build();
        }
    }

    /**
     * Класс <class>IndexedFieldsHolder</class> реализует кеш полей, которые присутствуют в аннотации {@link Indexed}
     *
     * @author Nazin Alexander
     */
    private class IndexedFieldsHolder extends Holder<Class<E>, IndexedClass<E>> {

        /**
         * Создает состояние индексируемого поля по аннотации
         *
         * @param annotation аннотация индексирования
         * @return Возвращает состояние индексируемого поля
         */
        private int createStates(Indexed.Field annotation) {
            int state = 0;
            state = state | (annotation.search() ? IndexedField.SEARCHABLE : 0);
            state = state | (annotation.filter() ? IndexedField.FILTERABLE : 0);
            state = state | (annotation.sort() ? IndexedField.SORTABLE : 0);
            return state;
        }

        /**
         * Выполняет формирование индексированного поля основываясь на указанной аннотации
         *
         * @param targetClass целевой класс
         * @param annotation целевая аннотация индексации
         * @return Возвращает индексированное поле
         */
        private IndexedField createIndexedField(Class<E> targetClass, Indexed.Field annotation) {
            final int fieldState = createStates(annotation);
            final Field field = ReflectionUtil.findField(targetClass, annotation.value());
            if (field == null) {
                try {
                    Method method = ReflectionUtil.findGetter(targetClass, annotation.value());
                    String fieldName = ReflectionUtil.extractFieldName(method);
                    return new IndexedField.ByMethod(fieldState, fieldName, method);
                } catch (NoSuchMethodException ex) {
                    throw new IllegalArgumentException(String.format("Can't find field or method with name '%s' in class '%s'", annotation.value(), targetClass.getName()));
                }
            } else {
                return new IndexedField.ByField(fieldState, field);
            }
        }

        @Override
        protected IndexedClass<E> createValue(Class<E> key) {
            // Получаем аннотацию индексации
            final Indexed indexed = key.getAnnotation(Indexed.class);
            Assert.notNull(indexed, String.format("Class '%s' not contains %s annotation", key.getName(), Indexed.class.getSimpleName()));
            // Первичный ключ
            Field primaryKey = ReflectionUtil.findField(key, indexed.id());
            Assert.notNull(primaryKey, String.format("Can't find field with name '%s' in class '%s'", indexed.id(), key.getName()));
            // Формируем коллекцию индексированных полей
            final ImmutableList.Builder<IndexedField> result = ImmutableList.builder();
            for (Indexed.Field annotation : indexed.fields()) {
                IndexedField indexedField = createIndexedField(key, annotation);
                result.add(indexedField);
                for (String alias : annotation.alias()) {
                    result.add(new IndexedField.ByAlias(alias, indexedField));
                }
            }
            // Формируем индексированный класс
            return new IndexedClass<E>(key, primaryKey, result.build());
        }
    }
}
