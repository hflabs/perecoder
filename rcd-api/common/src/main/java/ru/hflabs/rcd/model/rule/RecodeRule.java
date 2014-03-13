package ru.hflabs.rcd.model.rule;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import ru.hflabs.rcd.model.ManyToOne;
import ru.hflabs.rcd.model.annotation.Hashed;
import ru.hflabs.rcd.model.annotation.Indexed;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.path.FieldNamedPath;

import javax.validation.constraints.Size;

/**
 * Класс <class>RecodeRule</class> описывает правило перекодирования значений справочников
 *
 * @see RecodeRuleSet
 */
@Getter
@Setter
@Indexed(
        id = RecodeRule.PRIMARY_KEY,
        fields = {
                @Indexed.Field(RecodeRule.HISTORY_ID),
                @Indexed.Field(RecodeRule.CHANGE_TYPE),
                @Indexed.Field(RecodeRule.CHANGE_DATE),
                @Indexed.Field(RecodeRule.RECODE_RULE_SET_ID),
                @Indexed.Field(value = RecodeRule.FROM_FIELD_ID, alias = RecodeRule.FIELD_ID),
                @Indexed.Field(value = RecodeRule.TO_FIELD_ID, alias = RecodeRule.FIELD_ID),
                @Indexed.Field(value = RecodeRule.VALUE)
        }
)
@Hashed(ignore = {RecodeRule.PRIMARY_KEY, RecodeRule.HISTORY_ID, RecodeRule.FROM_PATH, RecodeRule.TO_PATH})
public class RecodeRule extends Rule<FieldNamedPath, Field, RecodeRule> implements ManyToOne<RecodeRuleSet> {

    private static final long serialVersionUID = 3339318130648910276L;

    /*
     * Название полей с идентификаторами
     */
    public static final String RECODE_RULE_SET_ID = "recodeRuleSetId";
    public static final String VALUE = FieldNamedPath.FIELD_VALUE;

    /** Идентификатор набора правил, к которому относится перекодировка */
    @Size(max = PRIMARY_KEY_MAX_SIZE)
    private String recodeRuleSetId;
    /** Набор правил, к которому относится перекодировка */
    private transient RecodeRuleSet relative;

    @Override
    public String getRelativeId() {
        return getRecodeRuleSetId();
    }

    @Override
    public void setRelative(RecodeRuleSet relative) {
        this.relative = relative;
        setRecodeRuleSetId(this.relative != null ? this.relative.getId() : null);
    }

    public RecodeRule injectRecodeRuleSet(RecodeRuleSet relativeRecodeRuleSet) {
        setRelative(relativeRecodeRuleSet);
        return this;
    }

    public String getFieldValue() {
        return getFromNamedPath() != null ? getFromNamedPath().getFieldValue() : null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (recodeRuleSetId != null ? recodeRuleSetId.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        RecodeRule that = (RecodeRule) o;

        if (recodeRuleSetId != null ? !recodeRuleSetId.equals(that.recodeRuleSetId) : that.recodeRuleSetId != null) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append(RECODE_RULE_SET_ID, getRecodeRuleSetId())
                .toString();
    }
}
