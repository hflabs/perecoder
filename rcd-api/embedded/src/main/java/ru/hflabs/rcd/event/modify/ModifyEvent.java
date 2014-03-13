package ru.hflabs.rcd.event.modify;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import lombok.Getter;
import org.springframework.util.Assert;
import ru.hflabs.rcd.event.ContextEvent;
import ru.hflabs.rcd.model.change.ChangeMode;
import ru.hflabs.rcd.model.change.ChangeSet;
import ru.hflabs.rcd.model.change.ChangeType;

import java.util.Collection;

/**
 * Класс <class>ModifyEvent</class> содержит информацию о событии изменения объектов
 *
 * @see ChangeSet
 * @see ChangeType
 */
@Getter
public abstract class ModifyEvent extends ContextEvent {

    private static final long serialVersionUID = 1557156668568360970L;

    /** Набор изменений */
    private final ChangeSet<?> changeSet;

    public ModifyEvent(Object source, ChangeSet<?> changeSet) {
        super(source);
        this.changeSet = changeSet;
    }

    public Class<?> getChangedClass() {
        return changeSet.targetClass;
    }

    public ChangeType getChangeType() {
        return changeSet.changeType;
    }

    public ChangeMode getChangeMode() {
        return changeSet.changeMode;
    }

    public <T> Collection<T> getChanged(Class<T> expectedClass) {
        return getChanged(expectedClass, Predicates.<T>alwaysTrue());
    }

    public <T> Collection<T> getChanged(final Class<T> expectedClass, Predicate<T> predicate) {
        Assert.isTrue(getChangedClass().equals(expectedClass), "Expected class is not equals changed class");
        Assert.notNull(predicate, "Filter predicate must not be NULL");
        Collection<T> targetCollection = Collections2.transform(changeSet.getChanged(), new Function<Object, T>() {
            @Override
            public T apply(Object input) {
                return expectedClass.cast(input);
            }
        });
        return Collections2.filter(targetCollection, predicate);
    }
}
