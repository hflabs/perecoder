package ru.hflabs.rcd.service.document;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import ru.hflabs.rcd.accessor.FieldAccessor;
import ru.hflabs.rcd.model.Essence;
import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;
import ru.hflabs.rcd.model.rule.Rule;
import ru.hflabs.rcd.service.IMergeService;

import java.util.Collection;
import java.util.Map;

import static ru.hflabs.rcd.accessor.Accessors.shallowClone;
import static ru.hflabs.rcd.model.ModelUtils.ID_FUNCTION;

/**
 * Класс <class>RuleActualizeService</class> реализует сервис актуализации правил перекодирования по изменившимся зависимым документами
 *
 * @param <D> класс зависимых документов
 * @param <NP> класс именованного пути документа
 * @param <T> класс базовой сущности правила
 * @param <R> класс правила перекодирования
 * @author Nazin Alexander
 */
public class RuleActualizeService<D extends Identifying, NP extends MetaFieldNamedPath, T extends Essence, R extends Rule<NP, T, R>> implements IMergeService<Collection<D>, Collection<R>, Collection<R>> {

    /** Сервис доступа к зависимой сущности */
    private final FieldAccessor<D, T> dependencyAccessor;
    /** Сервис доступа к источнику */
    private final FieldAccessor<T, R> fromFieldAccessor;
    /** Сервис доступа к назначению */
    private final FieldAccessor<T, R> toFieldAccessor;

    public RuleActualizeService(FieldAccessor<D, T> dependencyAccessor, FieldAccessor<T, R> fromFieldAccessor, FieldAccessor<T, R> toFieldAccessor) {
        this.dependencyAccessor = dependencyAccessor;
        this.fromFieldAccessor = fromFieldAccessor;
        this.toFieldAccessor = toFieldAccessor;
    }

    /**
     * Выполняет актуализацию правила
     *
     * @param rule целевое правило
     * @param dependency новая зависимость (may be <code>NULL</code>)
     * @param fieldAccessor сервис доступа к базовому полю
     * @return Возвращает актуализированное правило
     */
    private R doMerge(R rule, D dependency, FieldAccessor<T, R> fieldAccessor) {
        if (dependency != null) {
            // Выполняем актуализацию поля
            T targetField = dependencyAccessor.inject(fieldAccessor.apply(rule), dependency);
            // Актуализируем и возвращаем правило
            return fieldAccessor.inject(rule, targetField);
        }
        // Возвращаем исходное правило
        return rule;
    }

    /**
     * Выполняет актуализацию правила
     *
     * @param source исходное правило
     * @param target целевое правило
     * @param id2dependency карта зависимостей
     * @return Возвращает актуализированное правило
     */
    private R doMerge(R source, R target, Map<String, D> id2dependency) {
        String fromDependencyId = ID_FUNCTION.apply(dependencyAccessor.apply(source.getFrom()));
        target = doMerge(target, id2dependency.get(fromDependencyId), fromFieldAccessor);

        String toDependencyId = ID_FUNCTION.apply(dependencyAccessor.apply(source.getTo()));
        target = doMerge(target, id2dependency.get(toDependencyId), toFieldAccessor);

        return target;
    }

    @Override
    public Collection<R> merge(Collection<D> dependencies, Collection<R> rules) {
        // Выполняем построение карты зависимых сущностей к их идентификаторам
        Map<String, D> id2dependency = Maps.uniqueIndex(dependencies, ID_FUNCTION);
        // Выполняем обновление правил
        ImmutableList.Builder<R> result = ImmutableList.builder();
        for (R rule : rules) {
            result.add(doMerge(rule, shallowClone(rule), id2dependency));
        }
        // Возвращаем обновленные правила
        return result.build();
    }
}
