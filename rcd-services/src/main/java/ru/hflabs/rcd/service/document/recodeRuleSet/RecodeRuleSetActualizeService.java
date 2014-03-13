package ru.hflabs.rcd.service.document.recodeRuleSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import ru.hflabs.rcd.accessor.Accessors;
import ru.hflabs.rcd.accessor.FieldAccessor;
import ru.hflabs.rcd.model.Essence;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;
import ru.hflabs.rcd.service.IMergeService;
import ru.hflabs.rcd.service.document.RuleActualizeService;

import java.util.Collection;
import java.util.Map;

import static ru.hflabs.rcd.accessor.Accessors.*;
import static ru.hflabs.rcd.model.ModelUtils.ID_FUNCTION;

/**
 * Класс <class>RecodeRuleSetActualizeService</class> реализует сервис актуализации наборов правил перекодирования по изменившимся зависимостям
 *
 * @author Nazin Alexander
 * @see Accessors
 */
public class RecodeRuleSetActualizeService<T extends Essence> extends RuleActualizeService<T, MetaFieldNamedPath, MetaField, RecodeRuleSet> {

    /** Сервис обновления правил перекодирования по изменившимся группам справочников */
    public static final RecodeRuleSetActualizeService<Group> BY_GROUP = new RecodeRuleSetActualizeService<>(
            GROUP_TO_META_FIELD_INJECTOR
    );
    /** Сервис обновления правил перекодирования по изменившимся справочникам */
    public static final RecodeRuleSetActualizeService<Dictionary> BY_DICTIONARY = new RecodeRuleSetActualizeService<>(
            DICTIONARY_TO_META_FIELD_INJECTOR
    );
    /** Сервис обновления правил перекодирования по изменившимся МЕТА-полям */
    public static final RecodeRuleSetActualizeService<MetaField> BY_META_FIELD = new RecodeRuleSetActualizeService<>(
            Accessors.<MetaField>identity()
    );
    /** Сервис обновления полей перекодирования по умолчанию */
    public static final ByFieldActualizeService BY_DEFAULT_FIELD = new ByFieldActualizeService();

    public RecodeRuleSetActualizeService(FieldAccessor<T, MetaField> dependencyAccessor) {
        super(dependencyAccessor, FROM_SET_INJECTOR, TO_SET_INJECTOR);
    }

    /**
     * Класс <class>ByFieldActualizeService</class> реализует сервис актуализации полей перекодирования по умолчанию для наборов правил
     *
     * @author Nazin Alexander
     */
    private static class ByFieldActualizeService implements IMergeService<Collection<Field>, Collection<RecodeRuleSet>, Collection<RecodeRuleSet>> {

        @Override
        public Collection<RecodeRuleSet> merge(Collection<Field> fields, Collection<RecodeRuleSet> rules) {
            // Выполняем построение карты зависимых сущностей к их идентификаторам
            Map<String, Field> id2fields = Maps.uniqueIndex(fields, ID_FUNCTION);
            // Выполняем значений по умолчанию для правил
            ImmutableList.Builder<RecodeRuleSet> result = ImmutableList.builder();
            for (RecodeRuleSet ruleSet : rules) {
                RecodeRuleSet changed = shallowClone(ruleSet);
                // Актуализируем правило
                changed = DEFAULT_SET_INJECTOR.inject(changed, id2fields.get(ruleSet.getDefaultFieldId()));
                // Сохраняем обновленное значение
                result.add(changed);
            }
            // Возвращаем обновленные правила
            return result.build();
        }
    }
}
