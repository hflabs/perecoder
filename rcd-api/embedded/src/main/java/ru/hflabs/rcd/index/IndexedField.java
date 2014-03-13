package ru.hflabs.rcd.index;

import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * Класс <class>IndexedField</class> реализует описание индексированного поля
 *
 * @see ru.hflabs.rcd.model.annotation.Indexed.Field
 */
public abstract class IndexedField {

    /*
     * Доступные статусы
     */
    public static final int FILTERABLE = 0b001;
    public static final int SEARCHABLE = 0b010;
    public static final int SORTABLE = 0b101;

    /** Статус индексирования поля */
    private int state;

    public IndexedField(int state) {
        this.state = state;
    }

    /**
     * @return Возвращает отслеживаемый сервис доступа к индекстированному полю
     */
    public abstract Member getMember();

    /**
     * @return Возвращает название поля
     */
    public abstract String getName();

    /**
     * @return Возвращает тип поля
     */
    public abstract Class<?> getType();

    /**
     * Проверяет и возвращает <code>TRUE</code>, если флаг поля установлен
     *
     * @param targetFlag проверяемый статус
     * @return Возвращает флаг проверки
     */
    public boolean isStateEnabled(int targetFlag) {
        return (state & targetFlag) == targetFlag;
    }

    /**
     * Класс <class>ByField</class> реализует описание индексированного поля на основе {@link Field реального поля} класса
     */
    public static class ByField extends IndexedField {

        /** Отслеживаемое поле */
        private final Field field;

        public ByField(int state, Field field) {
            super(state);
            this.field = field;
        }

        @Override
        public Member getMember() {
            return field;
        }

        @Override
        public String getName() {
            return field.getName();
        }

        @Override
        public Class<?> getType() {
            return ClassUtils.resolvePrimitiveIfNecessary(field.getType());
        }
    }

    /**
     * Класс <class>ByMethod</class> реализует описание индексированного поля на основе {@link Method метода} класса
     */
    public static class ByMethod extends IndexedField {

        /** Название переменной */
        private final String name;
        /** Отслеживаемый метод */
        private final Method method;

        public ByMethod(int state, String name, Method method) {
            super(state);
            this.name = name;
            this.method = method;
        }

        @Override
        public Member getMember() {
            return method;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Class<?> getType() {
            return ClassUtils.resolvePrimitiveIfNecessary(method.getReturnType());
        }
    }

    /**
     * Класс <class>ByAlias</class> реализует описание индексированного поля, ссылаясь на другое поле, используя псевдоним
     */
    public static class ByAlias extends IndexedField {

        /** Название поля */
        private final String alias;
        /** Делегат поля */
        private final IndexedField delegate;

        public ByAlias(String alias, IndexedField delegate) {
            super(delegate.state);
            this.alias = alias;
            this.delegate = delegate;
        }

        @Override
        public Member getMember() {
            return delegate.getMember();
        }

        @Override
        public String getName() {
            return alias;
        }

        @Override
        public Class<?> getType() {
            return delegate.getType();
        }
    }
}
