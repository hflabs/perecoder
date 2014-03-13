package ru.hflabs.rcd.model.change;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.springframework.util.CollectionUtils;
import ru.hflabs.rcd.model.Historical;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * Класс <class>ChangeSet</class> содержит набор сущностей с однотипными изменениями
 *
 * @see History
 * @see ChangeType
 */
public final class ChangeSet<E> implements Serializable {

    private static final long serialVersionUID = 5829683980564622438L;

    /** Класс изменившихся объектов */
    public final Class<E> targetClass;
    /** Тип изменения */
    public final ChangeType changeType;
    /** Режим изменения */
    public final ChangeMode changeMode;

    /** Коллекция изменений */
    private final Collection<E> changed;

    public ChangeSet(Class<E> targetClass, ChangeType changeType, ChangeMode changeMode) {
        this(targetClass, changeType, changeMode, Lists.<E>newArrayList());
    }

    public ChangeSet(Class<E> targetClass, ChangeType changeType, ChangeMode changeMode, Collection<E> changed) {
        this.targetClass = targetClass;
        this.changeType = changeType;
        this.changeMode = changeMode;
        this.changed = changed;
    }

    public void appendChange(E change) {
        changed.add(change);
    }

    public void appendChanges(Collection<E> changes) {
        changed.addAll(changes);
    }

    public boolean isEmpty() {
        return changed.isEmpty();
    }

    public Collection<E> getChanged() {
        return Collections.unmodifiableCollection(changed);
    }

    public Collection<History> getChanges() {
        return (Historical.class.isAssignableFrom(targetClass) && !CollectionUtils.isEmpty(changed)) ?
                Collections2.transform(changed, new Function<E, History>() {
                    @Override
                    public History apply(E input) {
                        return ((Historical) input).getHistory();
                    }
                }) :
                Collections.<History>emptyList();
    }
}
