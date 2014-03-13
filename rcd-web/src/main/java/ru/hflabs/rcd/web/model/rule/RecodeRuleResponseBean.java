package ru.hflabs.rcd.web.model.rule;

import com.google.common.base.Function;
import lombok.Getter;
import ru.hflabs.rcd.model.rule.RecodeRule;

import static ru.hflabs.rcd.accessor.Accessors.FROM_RULE_INJECTOR;
import static ru.hflabs.rcd.accessor.Accessors.TO_RULE_INJECTOR;
import static ru.hflabs.rcd.model.ModelUtils.NAME_FUNCTION;

/**
 * Класс <class>RecodeRuleResponseBean</class> реализует декоратор ответа правила перекодирования
 *
 * @author Nazin Alexander
 */
@Getter
public class RecodeRuleResponseBean extends RecodeRuleBean {

    private static final long serialVersionUID = 4737921774128034637L;

    /** Функция создания декоратора */
    public static final Function<RecodeRule, RecodeRuleResponseBean> CONVERT = new Function<RecodeRule, RecodeRuleResponseBean>() {
        @Override
        public RecodeRuleResponseBean apply(RecodeRule input) {
            return new RecodeRuleResponseBean(
                    NAME_FUNCTION.apply(FROM_RULE_INJECTOR.apply(input)),
                    NAME_FUNCTION.apply(TO_RULE_INJECTOR.apply(input))
            );
        }
    };

    /** Идентификатор исходной записи */
    private final String fromRecordId;

    public RecodeRuleResponseBean(String fromRecordId, String toRecordId) {
        super(toRecordId);
        this.fromRecordId = fromRecordId;
    }
}
