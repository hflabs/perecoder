package ru.hflabs.rcd.event.modify;

import com.google.common.base.Predicate;
import org.springframework.util.CollectionUtils;
import ru.hflabs.rcd.model.Historical;
import ru.hflabs.rcd.model.change.ChangeSet;
import ru.hflabs.rcd.model.change.Diff;
import ru.hflabs.rcd.model.change.History;

import java.util.Collection;

/**
 * Класс <class>ChangeEvent</class> содержит информацию о событии изменения объектов
 *
 * @see ChangeSet
 * @see History
 */
public class ChangeEvent extends ModifyEvent {

    private static final long serialVersionUID = -9070933249467205777L;

    public ChangeEvent(Object source, ChangeSet<?> changeSet) {
        super(source, changeSet);
    }

    /**
     * Выполняет фильтрацию измененных объектов по переданному предикату
     *
     * @param expectedClass ожидаемый класс объектов
     * @param predicate предикат из коллекции изменений
     * @return Возвращает коллекцию объектов, которые удовлетворяют предикату
     */
    public <T extends Historical> Collection<T> getChangedByPredicate(Class<T> expectedClass, final Predicate<Collection<Diff>> predicate) {
        return getChanged(expectedClass, new Predicate<T>() {
            @Override
            public boolean apply(T input) {
                History history = input.getHistory();
                return history != null &&
                        !CollectionUtils.isEmpty(history.getDiffs()) &&
                        predicate.apply(history.getDiffs());
            }
        });
    }
}
