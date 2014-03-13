package ru.hflabs.rcd.accessor;

import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.ManyToOne;

/**
 * Интерфейс <class>RelativeFieldAccessor</class> декларирует методы доступа/установки связанных сущностей
 *
 * @see ManyToOne
 */
public interface RelativeFieldAccessor<R extends ManyToOne<E>, E extends Identifying> extends FieldAccessor<E, R> {

    /**
     * Возвращает идентификатор связанной сущности
     *
     * @param relative сущность
     * @return Возвращает идентификатор связанной сущности
     */
    String applyRelativeId(R relative);
}
