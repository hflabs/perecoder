package ru.hflabs.rcd.model.rule;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.model.ManyToOne;
import ru.hflabs.rcd.model.Named;
import ru.hflabs.rcd.model.annotation.Hashed;
import ru.hflabs.rcd.model.annotation.Indexed;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.path.FieldNamedPath;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;
import ru.hflabs.util.core.EqualsUtil;

import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Collection;

import static ru.hflabs.util.core.EqualsUtil.lowerCaseEquals;

/**
 * Класс <class>RecodeRuleSet</class> описывает набор правил перекодирования из одной исходной системы в другую
 *
 * @see RecodeRule
 */
@Getter
@Setter
@Indexed(
        id = RecodeRuleSet.PRIMARY_KEY,
        fields = {
                @Indexed.Field(RecodeRuleSet.HISTORY_ID),
                @Indexed.Field(RecodeRuleSet.CHANGE_TYPE),
                @Indexed.Field(RecodeRuleSet.CHANGE_DATE),
                @Indexed.Field(RecodeRuleSet.NAME),
                @Indexed.Field(RecodeRuleSet.DEFAULT_FIELD_ID),
                // From fields
                @Indexed.Field(value = RecodeRuleSet.FROM_GROUP_ID, alias = Dictionary.GROUP_ID),
                @Indexed.Field(value = RecodeRuleSet.FROM_GROUP_NAME, alias = MetaFieldNamedPath.GROUP_NAME),
                @Indexed.Field(value = RecodeRuleSet.FROM_DICTIONARY_ID, alias = MetaField.DICTIONARY_ID),
                @Indexed.Field(value = RecodeRuleSet.FROM_DICTIONARY_NAME, alias = MetaFieldNamedPath.DICTIONARY_NAME),
                @Indexed.Field(value = RecodeRuleSet.FROM_FIELD_ID, alias = RecodeRuleSet.FIELD_ID),
                @Indexed.Field(value = RecodeRuleSet.FROM_FIELD_NAME, alias = MetaFieldNamedPath.FIELD_NAME),
                // To fields
                @Indexed.Field(value = RecodeRuleSet.TO_GROUP_ID, alias = Dictionary.GROUP_ID),
                @Indexed.Field(value = RecodeRuleSet.TO_GROUP_NAME, alias = MetaFieldNamedPath.GROUP_NAME),
                @Indexed.Field(value = RecodeRuleSet.TO_DICTIONARY_ID, alias = MetaField.DICTIONARY_ID),
                @Indexed.Field(value = RecodeRuleSet.TO_DICTIONARY_NAME, alias = MetaFieldNamedPath.DICTIONARY_NAME),
                @Indexed.Field(value = RecodeRuleSet.TO_FIELD_ID, alias = RecodeRuleSet.FIELD_ID),
                @Indexed.Field(value = RecodeRuleSet.TO_FIELD_NAME, alias = MetaFieldNamedPath.FIELD_NAME)
        }
)
@Hashed(ignore = {RecodeRuleSet.PRIMARY_KEY, RecodeRuleSet.HISTORY_ID, RecodeRuleSet.FROM_PATH, RecodeRuleSet.TO_PATH, RecodeRuleSet.DEFAULT_PATH})
public class RecodeRuleSet extends Rule<MetaFieldNamedPath, MetaField, RecodeRuleSet> implements ManyToOne<Field>, Named {

    private static final long serialVersionUID = 888521687570738565L;

    /*
     * Постфиксы полей
     */
    private static final String GROUP_ID = "GroupId";
    private static final String GROUP_NAME = "GroupName";
    private static final String DICTIONARY_ID = "DictionaryId";
    private static final String DICTIONARY_NAME = "DictionaryName";
    /*
     * Название полей с идентификаторами
     */
    public static final String FROM_GROUP_ID = FROM_PREFIX + GROUP_ID;
    public static final String FROM_GROUP_NAME = FROM_PREFIX + GROUP_NAME;
    public static final String FROM_DICTIONARY_ID = FROM_PREFIX + DICTIONARY_ID;
    public static final String FROM_DICTIONARY_NAME = FROM_PREFIX + DICTIONARY_NAME;
    public static final String FROM_FIELD_NAME = FROM_PREFIX + FIELD_NAME;

    public static final String TO_GROUP_ID = TO_PREFIX + GROUP_ID;
    public static final String TO_GROUP_NAME = TO_PREFIX + GROUP_NAME;
    public static final String TO_DICTIONARY_ID = TO_PREFIX + DICTIONARY_ID;
    public static final String TO_DICTIONARY_NAME = TO_PREFIX + DICTIONARY_NAME;
    public static final String TO_FIELD_NAME = TO_PREFIX + FIELD_NAME;

    public static final String DEFAULT_PATH = "defaultPath";
    public static final String DEFAULT_FIELD_ID = "default" + FIELD_ID;

    /** Символическое название набора правил перекодирования */
    @Size(min = NAME_MIN_SIZE, max = NAME_MAX_SIZE)
    private String name;
    /** Именованный путь значения по умолчанию */
    private FieldNamedPath defaultPath;
    /** Идентификатор поля для перекодировки по умолчанию */
    @Size(max = PRIMARY_KEY_MAX_SIZE)
    private String defaultFieldId;
    /** Поле перекодировки по умолчанию */
    private transient Field relative;
    /** Коллекция правил перекодирования */
    private Collection<RecodeRule> recodeRules;

    @Override
    public void injectId(String targetId) {
        super.injectId(targetId);
        // Если название набора не заполнено, то устанавливаем его равным идентификатору
        if (!StringUtils.hasText(getName())) {
            setName(getId());
        }
    }

    @XmlTransient
    public FieldNamedPath getDefaultPath() {
        return defaultPath;
    }

    @Override
    public String getRelativeId() {
        return getDefaultFieldId();
    }

    @Override
    public void setRelative(Field relative) {
        this.relative = relative;
        setDefaultFieldId(this.relative != null ? this.relative.getId() : null);
    }

    public RecodeRuleSet injectDefaultField(Field relativeDefaultField) {
        setRelative(relativeDefaultField);
        return this;
    }

    @XmlTransient
    public Collection<RecodeRule> getRecodeRules() {
        return recodeRules;
    }

    public String getFromGroupId() {
        return getFrom() != null && getFrom().getRelative() != null ? getFrom().getRelative().getGroupId() : null;
    }

    public String getFromGroupName() {
        return getFromNamedPath() != null ? getFromNamedPath().getGroupName() : null;
    }

    public String getFromDictionaryId() {
        return getFrom() != null ? getFrom().getDictionaryId() : null;
    }

    public String getFromDictionaryName() {
        return getFromNamedPath() != null ? getFromNamedPath().getDictionaryName() : null;
    }

    public String getFromFieldName() {
        return getFromNamedPath() != null ? getFromNamedPath().getFieldName() : null;
    }

    public String getToGroupId() {
        return getTo() != null && getTo().getRelative() != null ? getTo().getRelative().getGroupId() : null;
    }

    public String getToGroupName() {
        return getToNamedPath() != null ? getToNamedPath().getGroupName() : null;
    }

    public String getToDictionaryId() {
        return getTo() != null ? getTo().getDictionaryId() : null;
    }

    public String getToDictionaryName() {
        return getToNamedPath() != null ? getToNamedPath().getDictionaryName() : null;
    }

    public String getToFieldName() {
        return getToNamedPath() != null ? getToNamedPath().getFieldName() : null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + EqualsUtil.lowerCaseHashCode(name);
        result = 31 * result + (defaultFieldId != null ? defaultFieldId.hashCode() : 0);
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

        RecodeRuleSet ruleSet = (RecodeRuleSet) o;

        if (!lowerCaseEquals(name, ruleSet.name)) {
            return false;
        }
        if (defaultFieldId != null ? !defaultFieldId.equals(ruleSet.defaultFieldId) : ruleSet.defaultFieldId != null) {
            return false;
        }
        if (defaultPath != null ? !defaultPath.equals(ruleSet.defaultPath) : ruleSet.defaultPath != null) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append(DEFAULT_FIELD_ID, getDefaultFieldId())
                .toString();
    }
}
