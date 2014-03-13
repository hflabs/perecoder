package ru.hflabs.rcd.web.model.rule;

import com.google.common.base.Function;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import static ru.hflabs.rcd.accessor.Accessors.DICTIONARY_TO_META_FIELD_INJECTOR;
import static ru.hflabs.rcd.accessor.Accessors.GROUP_TO_META_FIELD_INJECTOR;
import static ru.hflabs.rcd.model.ModelUtils.NAME_FUNCTION;

/**
 * Класс <class>RecodeRuleSetResponseBean</class> реализует декоратор набора правил перекодирования
 *
 * @see Group
 * @see Dictionary
 * @see RecodeRuleSet
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
public class RecodeRuleSetResponseBean extends RecodeRuleSetBean {

    private static final long serialVersionUID = 6451127455822062905L;

    /** Функция создания декоратора */
    public static final Function<RecodeRuleSet, RecodeRuleSetResponseBean> CONVERT = new Function<RecodeRuleSet, RecodeRuleSetResponseBean>() {
        @Override
        public RecodeRuleSetResponseBean apply(RecodeRuleSet input) {
            return new RecodeRuleSetResponseBean(input, NAME_FUNCTION.apply(input.getRelative()));
        }
    };

    public RecodeRuleSetResponseBean(RecodeRuleSet delegate, String defaultRecordId) {
        super(delegate, defaultRecordId);
    }

    public Group getFromGroup() {
        return GROUP_TO_META_FIELD_INJECTOR.apply(getDelegate().getFrom());
    }

    public Dictionary getFromDictionary() {
        return DICTIONARY_TO_META_FIELD_INJECTOR.apply(getDelegate().getFrom());
    }

    public Group getToGroup() {
        return GROUP_TO_META_FIELD_INJECTOR.apply(getDelegate().getTo());
    }

    public Dictionary getToDictionary() {
        return DICTIONARY_TO_META_FIELD_INJECTOR.apply(getDelegate().getTo());
    }
}
