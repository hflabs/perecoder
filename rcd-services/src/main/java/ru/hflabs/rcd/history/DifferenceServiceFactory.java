package ru.hflabs.rcd.history;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import ru.hflabs.rcd.Directories;
import ru.hflabs.rcd.model.Historical;
import ru.hflabs.rcd.model.annotation.Hashed;
import ru.hflabs.rcd.model.change.Diff;
import ru.hflabs.rcd.service.IDifferenceService;
import ru.hflabs.rcd.service.IServiceFactory;
import ru.hflabs.util.core.EqualsUtil;
import ru.hflabs.util.core.Holder;
import ru.hflabs.util.javac.InMemoryJavaFileObject;
import ru.hflabs.util.javac.RuntimeCompiler;
import ru.hflabs.util.javac.RuntimeCompilerUtil;
import ru.hflabs.util.spring.util.ReflectionUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;

/**
 * Класс <class>DifferenceServiceFactory</class> реализует фабрику создания <i>runtime</i> сервисов построения разницы между однотипными объектами
 *
 * @author Nazin Alexander
 * @see IServiceFactory
 * @see IDifferenceService
 * @see Hashed
 */
public class DifferenceServiceFactory<E> implements IDifferenceService<E>, IServiceFactory<IDifferenceService<E>, Class<?>>, InitializingBean {

    /** Сервис сравнения полей */
    private static final Comparator<Field> FIELDS_COMPARATOR = new FieldsComparator();

    /** Кеш сервисов */
    private final Holder<Class<?>, IDifferenceService<E>> servicesHolder;
    /** Кеш полей */
    private final Holder<Class<?>, Collection<Field>> hashedFieldsHolder;

    public DifferenceServiceFactory() {
        this.servicesHolder = new ServicesHolder();
        this.hashedFieldsHolder = new HashedFieldsHolder();
    }

    @Override
    public Collection<Diff> createDiff(E from, E to) {
        if (from != null && to != null) {
            Class<?> fromClass = from.getClass();
            Class<?> toClass = to.getClass();
            Assert.isTrue(
                    EqualsUtil.equals(fromClass, toClass),
                    String.format("Create a difference for different entities (%s != %s) is not supported", fromClass.getName(), toClass.getName())
            );

            return retrieveService(fromClass).createDiff(from, to);
        } else if (from != null) {
            return retrieveService(from.getClass()).createDiff(from, to);
        } else if (to != null) {
            return retrieveService(to.getClass()).createDiff(from, to);
        }
        return Collections.emptyList();
    }

    @Override
    public String createHashCode(E target) {
        Assert.notNull(target, "Target essence must not be NULL");
        return retrieveService(target.getClass()).createHashCode(target);
    }

    @Override
    public IDifferenceService<E> retrieveService(Class<?> targetClass) {
        Assert.notNull(targetClass, "Target class must not be NULL");
        return servicesHolder.getValue(targetClass);
    }

    @Override
    public void destroyService(Class<?> key, IDifferenceService<E> service) {
        servicesHolder.removeValue(key);
    }

    /**
     * Выполняет построение сервиса
     *
     * @param targetClass целевой класс
     * @return Возвращает объект готовый к компиляции
     */
    private InMemoryJavaFileObject<IDifferenceService<E>> buildInstance(Class<?> targetClass) {
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
                String.format("extends %s<%s>", DifferenceService.class.getSimpleName(), targetClass.getSimpleName())
        );

        // Формирование методов
        buildMethod_createDiff(out, targetClass);
        buildMethod_createHashCode(out, targetClass);

        out.println("}");
        out.close();

        return RuntimeCompilerUtil.createJavaFileObject(
                packageName,
                targetClass.getSimpleName() + IDifferenceService.class.getSimpleName(),
                writer.toString()

        );
    }

    /**
     * Формирование импортов
     *
     * @param out поток вывода
     * @param targetClass целевой класс
     */
    protected void buildImports(PrintWriter out, Class<?> targetClass) {
        out.format("import %s;", targetClass.getName()).println();
        out.format("import %s;", ru.hflabs.rcd.model.change.Diff.class.getName()).println();
        out.format("import %s;", ru.hflabs.rcd.service.IDifferenceService.class.getName()).println();
        out.format("import %s;", ru.hflabs.util.core.EqualsUtil.class.getName()).println();
        out.format("import %s;", ru.hflabs.util.core.FormatUtil.class.getName()).println();
        out.format("import %s;", ru.hflabs.util.core.MD5.class.getName()).println();
        out.println();
        out.format("import %s;", java.util.ArrayList.class.getName()).println();
        out.format("import %s;", java.util.Collection.class.getName()).println();
        out.println();
    }

    /**
     * Выполняет построение метода {@link IDifferenceService#createDiff(Object, Object)}
     *
     * @param out поток вывода
     * @param targetClass целевой класс
     */
    private void buildMethod_createDiff(PrintWriter out, Class<?> targetClass) {
        out.println("    @Override");
        out.format("    public Collection<Diff> createDiff(%s from, %s to) {", targetClass.getSimpleName(), targetClass.getSimpleName()).println();
        out.println("        final Collection<Diff> result = new ArrayList<Diff>();");
        for (Field field : hashedFieldsHolder.getValue(targetClass)) {
            final String fieldName = field.getName();
            final String fieldClassName = ClassUtils.resolvePrimitiveIfNecessary(field.getType()).getName();

            out.format("        // %s", fieldName).println();
            out.println("        {");
            if (Enum.class.isAssignableFrom(field.getType())) {
                out.format("            final String fromValue = (from != null) ? FormatUtil.format(from.%s(), false) : null;", RuntimeCompilerUtil.get(field)).println();
                out.format("            final String toValue = (to != null) ? FormatUtil.format(to.%s(), false) : null;", RuntimeCompilerUtil.get(field)).println();
            } else if (Collection.class.isAssignableFrom(field.getType()) || Map.class.isAssignableFrom(field.getType())) {
                out.format("            final String fromValue = (from != null) ? formatObject(from.%s()) : null;", RuntimeCompilerUtil.get(field)).println();
                out.format("            final String toValue = (to != null) ? formatObject(to.%s()) : null;", RuntimeCompilerUtil.get(field)).println();
            } else {
                out.format("            final String fromValue = (from != null) ? FormatUtil.format(from.%s()) : null;", RuntimeCompilerUtil.get(field)).println();
                out.format("            final String toValue = (to != null) ? FormatUtil.format(to.%s()) : null;", RuntimeCompilerUtil.get(field)).println();
            }
            out.println("            if (!EqualsUtil.equals(fromValue, toValue)) {");
            out.format("                result.add(new Diff(\"%s\", \"%s\", fromValue, toValue));", fieldClassName, fieldName).println();
            out.println("            }");
            out.println("        }");
        }
        out.println("        return !result.isEmpty() ? result : null;");
        out.println("    }");
        out.println();
    }

    /**
     * Выполняет построение метода {@link IDifferenceService#createHashCode(Object)}
     *
     * @param out поток вывода
     * @param targetClass целевой класс
     */
    private void buildMethod_createHashCode(PrintWriter out, Class<?> targetClass) {
        out.println("    @Override");
        out.format("    public String createHashCode(%s target) {", targetClass.getSimpleName()).println();
        out.println("        return MD5.asHex(");
        for (Iterator<Field> iterator = hashedFieldsHolder.getValue(targetClass).iterator(); iterator.hasNext(); ) {
            final Field field = iterator.next();
            final String separator = iterator.hasNext() ? "," : "";
            if (CharSequence.class.isAssignableFrom(field.getType())) {
                out.format("                target.%s()%s", RuntimeCompilerUtil.get(field), separator).println();
            } else if (Enum.class.isAssignableFrom(field.getType())) {
                out.format("                FormatUtil.format(target.%s(), false)%s", RuntimeCompilerUtil.get(field), separator).println();
            } else if (Collection.class.isAssignableFrom(field.getType())) {
                out.format("                formatCollection(target.%s())%s", RuntimeCompilerUtil.get(field), separator).println();
            } else if (Map.class.isAssignableFrom(field.getType())) {
                out.format("                formatMap(target.%s())%s", RuntimeCompilerUtil.get(field), separator).println();
            } else {
                out.format("                FormatUtil.format(target.%s())%s", RuntimeCompilerUtil.get(field), separator).println();
            }
        }
        out.println("        );");
        out.println("    }");
        out.println();
    }

    /**
     * Создает новый экземпляр класса
     *
     * @param memoryClass исходный класс
     * @return Возвращает созданный экземпляр класса
     */
    private IDifferenceService<E> createInstance(Class<IDifferenceService<E>> memoryClass) throws Exception {
        return memoryClass.newInstance();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (Class<?> clazz : ReflectionUtil.getAnnotatedClasses(Historical.class.getPackage().getName(), Hashed.class)) {
            retrieveService(clazz);
        }
    }

    /**
     * Класс <class>ServicesHolder</class> реализует кеш сервисов
     *
     * @author Nazin Alexander
     */
    private class ServicesHolder extends Holder<Class<?>, IDifferenceService<E>> {

        @Override
        protected IDifferenceService<E> createValue(Class<?> key) {
            try {
                return createInstance(
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
     * Класс <class>HashedFieldsHolder</class> реализует кеш полей, которые присутствуют в аннотации {@link Hashed}
     *
     * @author Nazin Alexander
     */
    private class HashedFieldsHolder extends Holder<Class<?>, Collection<Field>> {

        @Override
        protected Collection<Field> createValue(Class<?> key) {
            final Hashed hashed = key.getAnnotation(Hashed.class);
            Assert.notNull(hashed, String.format("Class '%s' not contains @%s annotation", key.getName(), Hashed.class.getSimpleName()));

            final Collection<String> ignoreFields = Sets.newHashSet(key.getAnnotation(Hashed.class).ignore());
            final List<Field> result = Lists.newArrayList();
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
                            return !ignoreFields.contains(field.getName()) &&
                                    !Modifier.isTransient(field.getModifiers()) &&
                                    !Modifier.isStatic(field.getModifiers()) &&
                                    !Modifier.isFinal(field.getModifiers()) &&
                                    !Collection.class.isAssignableFrom(field.getType());
                        }
                    }
            );

            Assert.isTrue(!CollectionUtils.isEmpty(result), String.format("Can't find any hashed fields for class '%s'", key.getName()));
            Collections.sort(result, FIELDS_COMPARATOR);
            return result;
        }
    }

    /**
     * Класс <class>FieldsComparator</class> реализует сервис сравнения полей на основании задекларированного класса и имени поля
     *
     * @author Nazin Alexander
     */
    private static class FieldsComparator implements Comparator<Field> {

        @Override
        public int compare(Field o1, Field o2) {
            Class<?> o1Class = o1.getDeclaringClass();
            Class<?> o2Class = o1.getDeclaringClass();
            int classResult = o1Class.getName().compareTo(o2Class.getName());
            return classResult == 0 ? o1.getName().compareTo(o2.getName()) : classResult;
        }
    }
}
