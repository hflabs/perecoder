package ru.hflabs.rcd.model.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Аннотация <class>Hashed</class> определяет набор полей, которые участвуют в построении разницы между двумя однотипными сущностями
 *
 * @see Object#hashCode()
 * @see Object#equals(Object)
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Hashed {

    /**
     * @return Возвращает массив названий полей, которые <b>НЕ ДОЛЖНЫ</b>  участвовать в построении разницы
     */
    String[] ignore();
}
