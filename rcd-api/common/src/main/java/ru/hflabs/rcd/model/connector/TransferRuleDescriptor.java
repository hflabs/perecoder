package ru.hflabs.rcd.model.connector;

import lombok.Getter;
import ru.hflabs.rcd.model.rule.RecodeRule;

import java.util.Collection;

/**
 * Класс <class>TransferRuleDescriptor</class> дескриптор трансфера правил перекодирования
 */
@Getter
public class TransferRuleDescriptor extends TransferDescriptor<Collection<RecodeRule>> {

    public TransferRuleDescriptor(Collection<RecodeRule> rules) {
        super(rules);
    }
}
