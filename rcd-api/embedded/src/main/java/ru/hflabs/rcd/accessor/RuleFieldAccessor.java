package ru.hflabs.rcd.accessor;

import com.google.common.base.Function;
import ru.hflabs.rcd.model.Essence;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;
import ru.hflabs.rcd.model.rule.Rule;

/**
 * Интерфейс <class>RuleFieldAccessor</class> декларирует методы доступа/установки параметров правила
 *
 * @see Rule
 */
public interface RuleFieldAccessor<NP extends MetaFieldNamedPath, T extends Essence, R extends Rule<NP, T, R>> extends FieldAccessor<T, R> {

    /**
     * Возвращает именованный путь правила
     *
     * @param rule целевое правило
     * @return Возвращает именованный путь
     */
    NP applyNamedPath(R rule);

    /**
     * Возвращает идентификатор связанной сущности
     *
     * @param rule целевое правило
     * @return Возвращает идентификатор связанной сущности
     */
    String applyRelativeId(Rule<?, ?, ?> rule);

    /**
     * Создает и возвращает функцию доступа к именованному пути
     *
     * @return Создает и возвращает функцию доступа к именованному пути
     */
    Function<R, NP> getNamedPathFunction();
}
