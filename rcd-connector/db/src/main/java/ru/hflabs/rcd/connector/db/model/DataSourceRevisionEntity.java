package ru.hflabs.rcd.connector.db.model;

import lombok.Getter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import ru.hflabs.util.core.EqualsUtil;
import ru.hflabs.util.core.FormatUtil;

import java.io.Serializable;

import static ru.hflabs.util.core.EqualsUtil.lowerCaseEquals;

/**
 * Класс <class>DataSourceDictionaryDefinition</class> содержит информацию об описании справочника в БД
 *
 * @author Nazin Alexander
 */
@Getter
public class DataSourceRevisionEntity implements Serializable {

    private static final long serialVersionUID = -4991545469808545804L;

    /*
     * Название полей с идентификаторами
     */
    public static final String GROUP_NAME = "groupName";
    public static final String DICTIONARY_NAME = "dictionaryName";
    public static final String DICTIONARY_DESCRIPTION = "dictionaryDescription";
    public static final String PK_NAME = "pkName";
    public static final String FLAG = "flag";
    public static final String RECORDS_QUERY = "recordsQuery";

    /** Название группы справочников */
    private String groupName;
    /** Название справочника */
    private String dictionaryName;
    /** Описание справочника */
    private String dictionaryDescription;
    /**
     * Флаги обработки справочника:<br/>
     * <ul>
     * <li>0 - актуален</li>
     * <li>1 - актуален, только для чтения</li>
     * <li>2 - удаление</li>
     * </ul>
     */
    private int flag = 0;

    /** Название колонки с первичным ключем справочника */
    private String pkName;
    /** SQL запрос получения записей */
    private String recordsQuery;

    public void setGroupName(String groupName) {
        this.groupName = FormatUtil.parseString(groupName);
    }

    public void setDictionaryName(String dictionaryName) {
        this.dictionaryName = FormatUtil.parseString(dictionaryName);
    }

    public void setDictionaryDescription(String dictionaryDescription) {
        this.dictionaryDescription = FormatUtil.parseString(dictionaryDescription);
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public void setPkName(String pkName) {
        this.pkName = FormatUtil.parseString(pkName);
    }

    public void setRecordsQuery(String recordsQuery) {
        this.recordsQuery = FormatUtil.parseString(recordsQuery);
    }

    @Override
    public int hashCode() {
        int result = EqualsUtil.lowerCaseHashCode(groupName);
        result = 31 * result + EqualsUtil.lowerCaseHashCode(dictionaryName);
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

        DataSourceRevisionEntity that = (DataSourceRevisionEntity) o;

        return lowerCaseEquals(groupName, that.groupName) && lowerCaseEquals(dictionaryName, that.dictionaryName);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append(GROUP_NAME, getGroupName())
                .append(DICTIONARY_NAME, getDictionaryName())
                .toString();
    }
}
