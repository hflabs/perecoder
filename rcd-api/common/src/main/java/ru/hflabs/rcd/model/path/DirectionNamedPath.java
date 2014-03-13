package ru.hflabs.rcd.model.path;

import ru.hflabs.util.core.Pair;

/**
 * Класс <class>DirectionNamedPath</class> реализует класс, содержащий пару из именованных путей направлений
 *
 * @see DictionaryNamedPath
 */
public class DirectionNamedPath<T extends DictionaryNamedPath> extends Pair<T, T> {

    private static final long serialVersionUID = 7600047598413789784L;

    public DirectionNamedPath(T fromNamedPath, T toNamedPath) {
        super(fromNamedPath, toNamedPath);
    }
}
