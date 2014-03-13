package ru.hflabs.rcd.model.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Аннотация <class>Indexed</class> определяет набор полей, которые участвуют в поиске/сортировке/фильтрации
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Indexed {

    /**
     * @return Возвращает название поля с первичным ключем
     */
    String id();

    /**
     * @return Возвращает набор полей для индексации
     */
    Field[] fields();

    /**
     * Аннотация <class>Field</class> описывает параметры индексации поля
     */
    @interface Field {

        /**
         * @return Возвращает название индексируемого поля
         */
        String value();

        /**
         * @return Возвращает массив дополнительных псевдонимов для поля
         */
        String[] alias() default {};

        /**
         * @return Возвращает флаг участия поля в поиске
         */
        boolean search() default false;

        /**
         * @return Возвращает флаг участия поля в фильтрации
         */
        boolean filter() default true;

        /**
         * @return Возвращает флаг участия поля в сортировке
         */
        boolean sort() default true;
    }
}
