package ru.hflabs.rcd.model.change;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import ru.hflabs.rcd.model.Historical;
import ru.hflabs.util.core.Holder;
import ru.hflabs.util.core.Pair;
import ru.hflabs.util.core.collection.ArrayUtil;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

/**
 * Класс <class>ChangeSetBuilder</class> реализует дескриптор построения изменений сущностей
 *
 * @see ChangeSet
 */
@Getter
@Setter
public class HistoryBuilder<E extends Historical> {

    /** Целевой класс объектов */
    private final Class<E> targetClass;
    /** Режим изменения объектов */
    private ChangeMode changeMode;

    /** Карта операций с сущностями */
    private final Holder<ChangeType, ChangeSet<E>> holder;

    public HistoryBuilder(Class<E> targetClass) {
        this(targetClass, ChangeMode.DEFAULT);
    }

    public HistoryBuilder(Class<E> targetClass, ChangeMode changeMode) {
        this.targetClass = targetClass;
        this.holder = new Type2SetHolder();
        this.changeMode = changeMode;
    }

    /**
     * @param type тип изменений
     * @return Возвращает коллекцию изменений по типу
     */
    public ChangeSet<E> getChangeSet(ChangeType type) {
        return holder.getValue(type);
    }

    /**
     * @param types коллекция целевых типов изменений
     * @return Возвращает коллекцию изменений
     */
    public Map<ChangeType, ChangeSet<E>> getChangeSets(ChangeType... types) {
        final Collection<ChangeType> targetTypes = ArrayUtil.isEmpty(types) ?
                EnumSet.allOf(ChangeType.class) :
                Sets.newHashSet(types);
        return Maps.newHashMap(Maps.filterKeys(holder.takeSnapshot(), new Predicate<ChangeType>() {
            @Override
            public boolean apply(ChangeType input) {
                return targetTypes.contains(input);
            }
        }));
    }

    /**
     * @param types коллекция целевых типов изменений
     * @return Возвращает все сущности дескриптора
     */
    public Collection<E> getEssences(ChangeType... types) {
        final ImmutableList.Builder<E> builder = ImmutableList.builder();
        for (ChangeSet<E> changeSet : getChangeSets(types).values()) {
            builder.addAll(changeSet.getChanged());
        }
        return builder.build();
    }

    /**
     * Добавляет изменение
     *
     * @param change измерение
     * @return Возвращает ссылку на сервис построения
     */
    public HistoryBuilder<E> addChange(Pair<ChangeType, E> change) {
        getChangeSet(change.first).appendChange(change.second);
        return this;
    }

    /**
     * Добавляет коллекцию изменений
     *
     * @param changeSets коллекция изменений
     * @return Возвращает ссылку на сервис построения
     */
    public HistoryBuilder<E> addChangeSets(Map<ChangeType, ChangeSet<E>> changeSets) {
        for (Map.Entry<ChangeType, ChangeSet<E>> entry : changeSets.entrySet()) {
            getChangeSet(entry.getKey()).appendChanges(entry.getValue().getChanged());
        }
        return this;
    }

    /**
     * Класс <class>Type2SetHolder</class> реализует кеш операций
     *
     * @see Holder
     */
    private class Type2SetHolder extends Holder<ChangeType, ChangeSet<E>> {

        @Override
        protected ChangeSet<E> createValue(ChangeType key) {
            return new ChangeSet<>(targetClass, key, changeMode);
        }
    }
}
