package ru.hflabs.rcd.event.recode;

import lombok.Getter;
import ru.hflabs.rcd.event.ContextEvent;

/**
 * Класс <class>RecodeEvent</class> содержит информацию о событии перекодировки
 *
 * @see ru.hflabs.rcd.model.rule.RecodeRuleSet
 * @see ru.hflabs.rcd.model.rule.RecodeRule
 */
@Getter
public abstract class RecodeEvent extends ContextEvent {

    private static final long serialVersionUID = -1506713284329513440L;

    /** Название набора правил перекодирования */
    private final String ruleSetName;

    public RecodeEvent(Object source, String ruleSetName) {
        super(source);
        this.ruleSetName = ruleSetName;
    }
}
