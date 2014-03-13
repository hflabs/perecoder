package ru.hflabs.rcd.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import ru.hflabs.rcd.index.IndexedField;
import ru.hflabs.rcd.model.annotation.Indexed;
import ru.hflabs.rcd.model.definition.ModelDefinition;
import ru.hflabs.rcd.model.definition.ModelFieldDefinition;
import ru.hflabs.util.core.Holder;
import ru.hflabs.util.core.Pair;
import ru.hflabs.util.core.collection.ArrayUtil;
import ru.hflabs.util.spring.util.ReflectionUtil;

import javax.validation.constraints.*;
import javax.xml.bind.annotation.XmlTransient;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static ru.hflabs.rcd.accessor.Accessors.injectId;

/**
 * Класс <class>ModelDefinitionFactory</class> реализует фабрику создания описание моделей
 *
 * @see ModelDefinition
 */
public class ModelDefinitionFactory implements IServiceFactory<ModelDefinition, Class<?>> {

    /** Кеш моделей */
    private final Holder<Class<?>, ModelDefinition> modelHolder;
    /** Сервис получаения индексированных полей */
    private Converter<Pair<Class<?>, String>, IndexedField> class2fieldConverter;

    public ModelDefinitionFactory() {
        this.modelHolder = new ModelBeanHolder();
    }

    public void setClass2fieldConverter(Converter<Pair<Class<?>, String>, IndexedField> class2fieldConverter) {
        this.class2fieldConverter = class2fieldConverter;
    }

    @Override
    public ModelDefinition retrieveService(Class<?> key) {
        return modelHolder.getValue(key);
    }

    @Override
    public void destroyService(Class<?> key, ModelDefinition service) {
        modelHolder.removeValue(key);
    }

    /**
     * Определяет и возвращает {@link ModelFieldDefinition.FieldType тип поля}
     *
     * @param member целевой объект
     * @return Возвращает тип поля
     */
    private ModelFieldDefinition.FieldType retrieveFieldType(Member member) {
        Class<?> fieldClass = ClassUtils.resolvePrimitiveIfNecessary(ReflectionUtil.extractFieldType(member));
        if (Number.class.isAssignableFrom(fieldClass)) {
            return ModelFieldDefinition.FieldType.NUMBER;
        } else if (Boolean.class.isAssignableFrom(fieldClass)) {
            return ModelFieldDefinition.FieldType.BOOLEAN;
        } else if (Date.class.isAssignableFrom(fieldClass)) {
            return ModelFieldDefinition.FieldType.DATE;
        } else {
            return ModelFieldDefinition.FieldType.STRING;
        }
    }

    /**
     * Выполняет поиск указанной аннотации
     *
     * @param annotationClass класс аннотации
     * @param member проверяемый объект
     * @return Возвращает найденную аннотацию или <code>NULL</code>, если она на задана
     */
    private <T extends Annotation> T findAnnotation(Class<T> annotationClass, Member member) {
        if (member instanceof Field) {
            return ((Field) member).getAnnotation(annotationClass);
        } else if (member instanceof Method) {
            Method method = (Method) member;
            T result = method.getAnnotation(annotationClass);
            if (result == null) {
                return findAnnotation(annotationClass, ReflectionUtil.findField(member.getDeclaringClass(), ReflectionUtil.extractFieldName(method)));
            }
            return result;
        } else {
            return null;
        }
    }

    /**
     * Выполняет обраборку аннотации {@link Size}
     *
     * @param bean целевая модель
     * @param member проверяемый объект
     * @return Возвращает модифицированную модель
     */
    private ModelFieldDefinition processSizeAnnotation(ModelFieldDefinition bean, Member member) {
        Size size = findAnnotation(Size.class, member);
        if (size != null) {
            bean.setMinLength((long) size.min());
            bean.setMaxLength((long) size.max());
        }
        return bean;
    }

    /**
     * Выполняет обраборку аннотации {@link Min} и {@link Max}
     *
     * @param bean целевая модель
     * @param member проверяемый объект
     * @return Возвращает модифицированную модель
     */
    private ModelFieldDefinition processLengthAnnotation(ModelFieldDefinition bean, Member member) {
        Min min = findAnnotation(Min.class, member);
        if (min != null) {
            bean.setMinLength(min.value());
        }
        Max max = findAnnotation(Max.class, member);
        if (max != null) {
            bean.setMaxLength(max.value());
        }
        return bean;
    }

    /**
     * Выполняет обраборку аннотации {@link NotNull}
     *
     * @param bean целевая модель
     * @param member проверяемый объект
     * @return Возвращает модифицированную модель
     */
    private ModelFieldDefinition processNotNullAnnotation(ModelFieldDefinition bean, Member member) {
        if (findAnnotation(NotNull.class, member) != null) {
            bean.setRequired(true);
        }
        return bean;
    }

    /**
     * Выполняет обраборку аннотации {@link Pattern}
     *
     * @param bean целевая модель
     * @param member проверяемый объект
     * @return Возвращает модифицированную модель
     */
    private ModelFieldDefinition processPatternAnnotation(ModelFieldDefinition bean, Member member) {
        Pattern pattern = findAnnotation(Pattern.class, member);
        if (pattern != null) {
            bean.setPattern(pattern.regexp());
        }
        return bean;
    }

    /**
     * Выполняет обраборку аннотации {@link ru.hflabs.rcd.model.annotation.Indexed}
     *
     * @param targetClass целевой класс
     * @param bean целевая модель
     * @param member проверяемый объект
     * @return Возвращает модифицированную модель
     */
    private ModelFieldDefinition processIndexedAnnotation(Class<?> targetClass, ModelFieldDefinition bean, Member member) {
        if (targetClass.isAnnotationPresent(Indexed.class)) {
            try {
                // Получаем индексированное поле
                IndexedField indexedField = class2fieldConverter.convert(Pair.<Class<?>, String>valueOf(targetClass, ReflectionUtil.extractFieldName(member)));
                // Заполняем дескриптор
                bean.setSortable(indexedField.isStateEnabled(IndexedField.SORTABLE));
            } catch (IllegalArgumentException ex) {
                // do noting
            }
        }
        return bean;
    }

    /**
     * Выполняем построение модели объекта
     *
     * @param targetClass целевой класс
     * @return Возвращает модель объекта
     */
    private ModelDefinition buildInstance(Class<?> targetClass) {
        final ModelDefinition result = injectId(new ModelDefinition(), targetClass.getSimpleName());
        // Формируем доступные методы
        final Collection<Member> members = Lists.newLinkedList();
        ReflectionUtil.doWithMethods(
                targetClass,
                new ReflectionUtils.MethodCallback() {
                    @Override
                    public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                        members.add(method);
                    }
                },
                new ReflectionUtils.MethodFilter() {
                    @Override
                    public boolean matches(Method method) {
                        return Modifier.isPublic(method.getModifiers()) &&
                                !Modifier.isNative(method.getModifiers()) &&
                                !method.isSynthetic() &&
                                !method.isAnnotationPresent(XmlTransient.class) &&
                                ArrayUtil.isEmpty(method.getParameterTypes()) &&
                                (method.getName().startsWith("get") || method.getName().startsWith("is"));
                    }
                }
        );
        // Для каждого доступного метотода формируем дескриптор
        Map<String, ModelFieldDefinition> fields = Maps.newLinkedHashMap();
        for (Member member : members) {
            ModelFieldDefinition modelFieldDefinition = new ModelFieldDefinition();
            // Type
            modelFieldDefinition.setType(retrieveFieldType(member));
            // Size
            modelFieldDefinition = processSizeAnnotation(modelFieldDefinition, member);
            // Length
            modelFieldDefinition = processLengthAnnotation(modelFieldDefinition, member);
            // NotNull
            modelFieldDefinition = processNotNullAnnotation(modelFieldDefinition, member);
            // Pattern
            modelFieldDefinition = processPatternAnnotation(modelFieldDefinition, member);
            // Indexed
            modelFieldDefinition = processIndexedAnnotation(targetClass, modelFieldDefinition, member);
            // Сохраняем построенный дескриптор
            fields.put(ReflectionUtil.extractFieldName(member), modelFieldDefinition);
        }
        // Сохраняем найденные поля
        result.setFields(fields);
        // Возвращаем построенную модель
        return result;
    }

    /**
     * Класс <class>ModelBeanHolder</class>
     *
     * @author Nazin Alexander
     */
    private class ModelBeanHolder extends Holder<Class<?>, ModelDefinition> {

        @Override
        protected ModelDefinition createValue(Class<?> key) {
            return buildInstance(key);
        }
    }
}
