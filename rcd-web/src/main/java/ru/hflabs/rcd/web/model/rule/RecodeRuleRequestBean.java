package ru.hflabs.rcd.web.model.rule;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

/**
 * Класс <class>RecodeRuleRequestBean</class> реализует декоратор запроса модификации правил
 *
 * @author Nazin Alexander
 */
@Getter
@Setter
public class RecodeRuleRequestBean extends RecodeRuleBean {

    private static final long serialVersionUID = 7522942921544944774L;

    /** Идентификаторы исходных записей */
    @NotNull
    @Size(min = 1)
    private Set<String> fromRecordIDs;

    public RecodeRuleRequestBean() {
        super(null);
    }
}
