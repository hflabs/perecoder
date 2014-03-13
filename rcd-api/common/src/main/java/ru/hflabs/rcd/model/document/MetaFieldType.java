package ru.hflabs.rcd.model.document;

import java.util.regex.Pattern;

/**
 * Класс <class>MetaFieldType</class> перечисляет возможные типы МЕТА-поля
 *
 * @see MetaField
 */
public enum MetaFieldType {

    /** Строковое поле */
    STRING(String.class, ".*");

    /** Регулярное выражение проверки данных */
    private Pattern pattern;
    /** Целевой класс типа */
    private Class<?> targetClass;

    private MetaFieldType(Class<?> targetClass, String pattern) {
        this.targetClass = targetClass;
        this.pattern = Pattern.compile(pattern);
    }

    public Pattern getPattern() {
        return pattern;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }
}
