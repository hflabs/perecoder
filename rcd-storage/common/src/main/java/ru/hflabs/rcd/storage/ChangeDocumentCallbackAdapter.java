package ru.hflabs.rcd.storage;

import ru.hflabs.rcd.exception.constraint.IllegalPrimaryKeyException;
import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.util.spring.Assert;

import java.util.Collection;

/**
 * Класс <class>ChangeDocumentCallbackAdapter</class> реализует базовый адаптер модификации документов
 *
 * @see Identifying
 */
public abstract class ChangeDocumentCallbackAdapter<E extends Identifying> implements ChangeDocumentCallback<E> {

    @Override
    public Collection<E> beforeModify(Collection<E> objects) {
        for (E object : objects) {
            Assert.notNull(object.getId(), String.format("Can't modify '%s' without ID", object.toString()), IllegalPrimaryKeyException.class);
        }
        return objects;
    }

    @Override
    public Collection<E> afterModify(Collection<E> objects) {
        return objects;
    }
}
