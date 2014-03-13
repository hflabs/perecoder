package ru.hflabs.rcd.accessor;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import ru.hflabs.rcd.exception.constraint.document.NotUniqueFieldsException;
import ru.hflabs.rcd.model.*;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.path.FieldNamedPath;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;
import ru.hflabs.rcd.model.rule.RecodeRule;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;
import ru.hflabs.rcd.model.rule.Rule;
import ru.hflabs.util.core.FormatUtil;
import ru.hflabs.util.spring.Assert;

import java.util.Collection;
import java.util.List;

import static ru.hflabs.rcd.model.ModelUtils.*;

/**
 * Класс <class>Accessors</class> реализует вспомогательные методы для установки/доступа объектов, основанных на иерархии интерфейсов
 *
 * @see Function
 * @see ModelUtils
 */
public abstract class Accessors {

    private static final FieldAccessor IDENTITY = new IdentityFieldAccessor();

    /** Сервис установки исходного значения в правило перекодирования */
    public static final RuleFieldAccessor<FieldNamedPath, Field, RecodeRule> FROM_RULE_INJECTOR = new FromRuleFieldAccessorTemplate<FieldNamedPath, Field, RecodeRule>() {

        @Override
        public RecodeRule inject(RecodeRule target, Field value) {
            return target.injectFrom(value).injectFromNamedPath(createFieldNamedPath(value));
        }
    };
    /** Сервис установки целевого значения в правило перекодирования */
    public static final RuleFieldAccessor<FieldNamedPath, Field, RecodeRule> TO_RULE_INJECTOR = new ToRuleFieldAccessorTemplate<FieldNamedPath, Field, RecodeRule>() {

        @Override
        public RecodeRule inject(RecodeRule target, Field value) {
            return target.injectTo(value).injectToNamedPath(createFieldNamedPath(value));
        }
    };

    /** Сервис установки исходного значения в набор правил перекодирования */
    public static final RuleFieldAccessor<MetaFieldNamedPath, MetaField, RecodeRuleSet> FROM_SET_INJECTOR = new FromRuleFieldAccessorTemplate<MetaFieldNamedPath, MetaField, RecodeRuleSet>() {

        @Override
        public RecodeRuleSet inject(RecodeRuleSet target, MetaField value) {
            return target.injectFrom(value).injectFromNamedPath(ModelUtils.createMetaFieldNamedPath(value));
        }
    };
    /** Сервис установки целевого значения в набор правил перекодирования */
    public static final RuleFieldAccessor<MetaFieldNamedPath, MetaField, RecodeRuleSet> TO_SET_INJECTOR = new ToRuleFieldAccessorTemplate<MetaFieldNamedPath, MetaField, RecodeRuleSet>() {

        @Override
        public RecodeRuleSet inject(RecodeRuleSet target, MetaField value) {
            return target.injectTo(value).injectToNamedPath(ModelUtils.createMetaFieldNamedPath(value));
        }
    };
    /** Сервис установки и доступа к полю перекодирования по умолчанию для набора правил */
    public static final RelativeFieldAccessor<RecodeRuleSet, Field> DEFAULT_SET_INJECTOR = new RelativeFieldAccessorTemplate<RecodeRuleSet, Field>() {

        @Override
        public RecodeRuleSet inject(RecodeRuleSet target, Field value) {
            RecodeRuleSet result = super.inject(target, value);
            result.setDefaultPath(createFieldNamedPath(value));
            return result;
        }
    };

    /** Сервис установки и доступа к группе справочника через справочник */
    public static final RelativeFieldAccessor<Dictionary, Group> GROUP_TO_DICTIONARY_INJECTOR = new RelativeFieldAccessorTemplate<>();
    /** Сервис установки и доступа к справочнику через МЕТА-поле */
    public static final RelativeFieldAccessor<MetaField, Dictionary> DICTIONARY_TO_META_FIELD_INJECTOR = new RelativeFieldAccessorTemplate<>();
    /** Сервис установки и доступа к значению поля через МЕТА-поле */
    public static final RelativeFieldAccessor<Field, MetaField> META_FIELD_TO_FIELD_INJECTOR = new RelativeFieldAccessorTemplate<>();
    /** Сервис установки и доступа к группе справочников через МЕТА-поле */
    public static final FieldAccessor<Group, MetaField> GROUP_TO_META_FIELD_INJECTOR = compose(DICTIONARY_TO_META_FIELD_INJECTOR, GROUP_TO_DICTIONARY_INJECTOR);

    protected Accessors() {
        // embedded constructor
    }

    /**
     * Формирует и возвращает комплексный сервис преобразования сущностей по следующему принципу доступа:
     * <p/>
     * <i>A</i> -> <i>B</i> -> <i>C</i>
     *
     * @param b2c сервис преобразования <i>B</i> в <i>C</i>
     * @param a2b сервис преобразования <i>A</i> в <i>B</i>
     * @return Возвращает комплексный сервис преобразования сущностей
     */
    public static <A, B, C> FieldAccessor<A, C> compose(final FieldAccessor<B, C> b2c, final FieldAccessor<A, B> a2b) {
        return new FieldAccessor<A, C>() {
            private final FieldAccessor<B, C> b_to_c = b2c;
            private final FieldAccessor<A, B> a_to_b = a2b;
            private final Function<C, A> a_to_c = Functions.compose(a_to_b, b_to_c);

            @Override
            public C inject(C target, A value) {
                B intermediate = b_to_c.apply(target);
                intermediate = a_to_b.inject(intermediate, value);
                return b_to_c.inject(target, intermediate);
            }

            @Override
            public A apply(C input) {
                return a_to_c.apply(input);
            }
        };
    }

    /**
     * Возвращает самозамыкающийся сервис преобразования
     *
     * @see com.google.common.base.Functions#identity()
     */
    @SuppressWarnings("unchecked")
    public static <T> FieldAccessor<T, T> identity() {
        return (FieldAccessor<T, T>) IDENTITY;
    }

    /**
     * Устанавливает идентификатор и возвращает сущность
     *
     * @param target целевая сущность
     * @param value устанавливаемый идентификатор
     * @return Возвращает сущность или <code>NULL</code>, если сущность не задана
     * @see Identifying#setId(String)
     */
    public static <T extends Identifying> T injectId(T target, String value) {
        if (target != null) {
            target.injectId(value);
        }
        return target;
    }

    /**
     * Устанавливает название и возвращает сущность
     *
     * @param target целевая сущность
     * @param value устанавливаемое название
     * @return Возвращает сущность или <code>NULL</code>, если сущность не задана
     * @see Named#setName(String)
     */
    public static <T extends Named> T injectName(T target, String value) {
        if (target != null) {
            target.setName(value);
        }
        return target;
    }

    /**
     * Устанавливает название, предварительно удалив из него лидирующие пробелы, и возвращает сущность
     *
     * @param target целевая сущность
     * @param value устанавливаемое название
     * @return Возвращает сущность или <code>NULL</code>, если сущность не задана
     * @see #injectName(Named, String)
     * @see String#trim()
     */
    public static <T extends Named> T injectTrimmedName(T target, String value) {
        return injectName(target, FormatUtil.parseString(value));
    }

    /**
     * Устанавливает описание и возвращает сущность
     *
     * @param target целевая сущность
     * @param value устанавливаемое описание
     * @return Возвращает сущность или <code>NULL</code>, если сущность не задана
     * @see Descriptioned#setDescription(String)
     */
    public static <T extends Descriptioned> T injectDescription(T target, String value) {
        if (target != null) {
            target.setDescription(value);
        }
        return target;
    }

    /**
     * Устанавливает описание, предварительно удалив из него лидирующие пробелы, и возвращает сущность
     *
     * @param target целевая сущность
     * @param value устанавливаемое описание
     * @return Возвращает сущность или <code>NULL</code>, если сущность не задана
     * @see #injectDescription(Descriptioned, String)
     * @see String#trim()
     */
    public static <T extends Descriptioned> T injectTrimmedDescription(T target, String value) {
        return injectDescription(target, FormatUtil.parseString(value));
    }

    /**
     * Устанавливает связь между связанными объектами
     *
     * @param parent родитель
     * @param descendant потомок
     * @return Возвращает модифицированного потомка
     */
    public static <R extends Identifying & OneToMany<T>, T extends Identifying & ManyToOne<R>> T linkRelative(R parent, T descendant) {
        if (descendant != null) {
            descendant.setRelative(parent);
        }
        return descendant;
    }

    /**
     * Устанавливает связь между связанными объектами
     *
     * @param parent родитель
     * @param descendants коллекция потомоков
     * @return Возвращает модифицированного родителя
     */
    public static <R extends Identifying & OneToMany<T>, T extends Identifying & ManyToOne<R>> R linkDescendants(R parent, Collection<T> descendants) {
        if (descendants != null) {
            List<T> targetDescendant = Lists.newArrayListWithExpectedSize(descendants.size());
            for (T descendant : descendants) {
                targetDescendant.add(linkRelative(parent, descendant));
            }
            parent.setDescendants(targetDescendant);
        }
        return parent;
    }

    /**
     * Устанавливает связь между связанными объектами
     *
     * @param parent родитель
     * @param descendants коллекция потомоков
     * @return Возвращает коллекцию модифицированных потомков
     */
    public static <R extends Identifying & OneToMany<T>, T extends Identifying & ManyToOne<R>> Collection<T> linkDescendants(Collection<T> descendants, R parent) {
        return linkDescendants(parent, descendants).getDescendants();
    }

    /**
     * Выполняет добавление значения поля к уже существующей коллекции в МЕТА-поле
     *
     * @param metaField целевое МЕТА-поле
     * @param field добавляемое значение поля
     */
    public static Field linkFieldToMetaField(MetaField metaField, Field field) {
        field = linkRelative(metaField, field);

        Collection<Field> fields = metaField.getDescendants();
        if (fields == null) {
            fields = metaField.isFlagEstablished(MetaField.FLAG_UNIQUE) ?
                    Sets.<Field>newLinkedHashSet() :
                    Lists.<Field>newLinkedList();
        }
        Assert.isTrue(
                fields.add(field),
                String.format("Meta field '%s' marked as unique, but has duplicate value '%s'", metaField.getName(), field.getValue()),
                NotUniqueFieldsException.class
        );
        metaField.setDescendants(fields);

        return field;
    }

    /**
     * Выполняет копировавание сущности
     *
     * @param target целевая сущность
     * @return Возвращает копию сущности
     * @see Copyable#copy()
     */
    public static <T extends Copyable> T shallowClone(T target) {
        return target.copy();
    }

    /**
     * Класс <class>IdentityFieldAccessor</class> реализует самозамыкающийся сервис доступа
     */
    private static final class IdentityFieldAccessor implements FieldAccessor<Object, Object> {

        @Override
        public Object inject(Object target, Object value) {
            return value;
        }

        @Override
        public Object apply(Object input) {
            return input;
        }
    }

    /**
     * Класс <class>RelativeFieldAccessor</class> реализует сервис доступа и установки связанных сущностей
     */
    private static class RelativeFieldAccessorTemplate<R extends Identifying & ManyToOne<E>, E extends Identifying> implements RelativeFieldAccessor<R, E> {

        @Override
        public R inject(R target, E value) {
            Assert.notNull(target, "Source relative object must not be NULL");
            target.setRelative(value);
            return target;
        }

        @Override
        public E apply(R input) {
            return input != null ? input.getRelative() : null;
        }

        @Override
        public String applyRelativeId(R relative) {
            return RELATIVE_ID_FUNCTION.apply(relative);
        }
    }

    /**
     * Класс <class>RuleFieldAccessorTemplate</class> реализует шаблон для доступа/установки полей для правила
     */
    private abstract static class RuleFieldAccessorTemplate<NP extends MetaFieldNamedPath, T extends Essence, R extends Rule<NP, T, R>> implements RuleFieldAccessor<NP, T, R> {

        /** Функция доступа к связанному идентификатору */
        private final Function<Rule<?, ?, ?>, String> fieldIdFunction;
        /** Функция доступа к именованному пути */
        private final Function<R, NP> namedPathFunction;

        protected RuleFieldAccessorTemplate(Function<Rule<?, ?, ?>, String> fieldIdFunction) {
            this.fieldIdFunction = fieldIdFunction;
            this.namedPathFunction = new Function<R, NP>() {
                @Override
                public NP apply(R input) {
                    return applyNamedPath(input);
                }
            };
        }

        @Override
        public String applyRelativeId(Rule<?, ?, ?> rule) {
            return fieldIdFunction.apply(rule);
        }

        @Override
        public Function<R, NP> getNamedPathFunction() {
            return namedPathFunction;
        }
    }

    /**
     * Класс <class>FromRuleFieldAccessorTemplate</class> реализует шаблон доступа/установки полей источника для правила
     */
    private abstract static class FromRuleFieldAccessorTemplate<NP extends MetaFieldNamedPath, T extends Essence, R extends Rule<NP, T, R>> extends RuleFieldAccessorTemplate<NP, T, R> {

        protected FromRuleFieldAccessorTemplate() {
            super(FROM_RULE_FIELD_ID);
        }

        @Override
        public NP applyNamedPath(R rule) {
            return rule.getFromNamedPath();
        }

        @Override
        public T apply(R input) {
            return input.getFrom();
        }
    }

    /**
     * Класс <class>ToRuleFieldAccessorTemplate</class> реализует шаблон доступа/установки полей назначения для правила
     */
    private abstract static class ToRuleFieldAccessorTemplate<NP extends MetaFieldNamedPath, T extends Essence, R extends Rule<NP, T, R>> extends RuleFieldAccessorTemplate<NP, T, R> {

        protected ToRuleFieldAccessorTemplate() {
            super(TO_RULE_FIELD_ID);
        }

        @Override
        public NP applyNamedPath(R rule) {
            return rule.getToNamedPath();
        }

        @Override
        public T apply(R input) {
            return input.getTo();
        }
    }
}
