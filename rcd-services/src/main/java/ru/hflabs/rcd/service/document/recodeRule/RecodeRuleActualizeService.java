package ru.hflabs.rcd.service.document.recodeRule;

import ru.hflabs.rcd.accessor.Accessors;
import ru.hflabs.rcd.accessor.FieldAccessor;
import ru.hflabs.rcd.model.Essence;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.path.FieldNamedPath;
import ru.hflabs.rcd.model.rule.RecodeRule;
import ru.hflabs.rcd.service.document.RuleActualizeService;

import static ru.hflabs.rcd.accessor.Accessors.FROM_RULE_INJECTOR;
import static ru.hflabs.rcd.accessor.Accessors.TO_RULE_INJECTOR;

/**
 * Класс <class>RecodeRuleActualizeService</class> реализует сервис актуализации правил перекодирования по изменившимся зависимостям
 *
 * @author Nazin Alexander
 * @see Accessors
 */
public class RecodeRuleActualizeService<T extends Essence> extends RuleActualizeService<T, FieldNamedPath, Field, RecodeRule> {

    /** Сервис обновления правил перекодирования по изменившимся значениям полей */
    public static final RecodeRuleActualizeService<Field> BY_FIELD = new RecodeRuleActualizeService<>(
            Accessors.<Field>identity()
    );

    public RecodeRuleActualizeService(FieldAccessor<T, Field> dependencyAccessor) {
        super(dependencyAccessor, FROM_RULE_INJECTOR, TO_RULE_INJECTOR);
    }
}
