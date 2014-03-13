package ru.hflabs.rcd.accessor;

import com.google.common.base.Function;

/**
 * Интерфейс <class>FieldAccessor</class> декларирует методы для установки и получения значений объекта
 *
 * @see Function
 */
public interface FieldAccessor<O, T> extends Function<T, O> {

    /**
     * Устанавливает значение в объект
     *
     * @param value значение
     * @return Возвращает ссылку на текущий объект
     */
    T inject(T target, O value);
}
