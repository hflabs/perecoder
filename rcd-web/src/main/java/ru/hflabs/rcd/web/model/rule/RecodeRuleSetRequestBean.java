package ru.hflabs.rcd.web.model.rule;

import lombok.Getter;
import lombok.Setter;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import static ru.hflabs.rcd.model.Identifying.PRIMARY_KEY_MAX_SIZE;

/**
 * Класс <class>RecodeRuleSetRequestBean</class> реализует декоратор идентификаторов набора правил перекодирования
 *
 * @see RecodeRuleSetBean
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.PROPERTY)
public class RecodeRuleSetRequestBean extends RecodeRuleSetBean {

    private static final long serialVersionUID = -1372140441600700465L;

    /** Идентификатор исходного справочника */
    @NotNull
    @Size(max = PRIMARY_KEY_MAX_SIZE)
    private String fromDictionaryId;
    /** Идентификатор целевого справочника */
    @NotNull
    @Size(max = PRIMARY_KEY_MAX_SIZE)
    private String toDictionaryId;

    public RecodeRuleSetRequestBean() {
        super(new RecodeRuleSet(), null);
    }
}
