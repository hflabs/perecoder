package ru.hflabs.rcd.model.notification;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import ru.hflabs.rcd.model.Named;
import ru.hflabs.rcd.model.annotation.Hashed;
import ru.hflabs.rcd.model.annotation.Indexed;
import ru.hflabs.rcd.model.document.DocumentTemplate;
import ru.hflabs.util.core.date.DateUtil;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Класс <class>Notification</class> содержит информацию об оповещении
 *
 * @see NotifyType
 * @see NotifyState
 */
@Getter
@Setter
@Indexed(
        id = Notification.PRIMARY_KEY,
        fields = {
                @Indexed.Field(Notification.HISTORY_ID),
                @Indexed.Field(Notification.CHANGE_TYPE),
                @Indexed.Field(Notification.CHANGE_DATE),
                @Indexed.Field(Notification.START_DATE),
                @Indexed.Field(Notification.END_DATE),
                @Indexed.Field(Notification.TYPE),
                @Indexed.Field(Notification.PROCESSING_DATE),
                @Indexed.Field(Notification.PROCESSING_STATE)
        }
)
@Hashed(ignore = {Notification.PRIMARY_KEY, Notification.HISTORY_ID})
public class Notification extends DocumentTemplate {

    private static final long serialVersionUID = -7785917265124094560L;

    /** Максимальный размер названия документа */
    public static final int DOCUMENT_NAME_LENGTH = Named.NAME_MAX_SIZE;

    /*
     * Название полей с идентификаторами
     */
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String TYPE = "type";

    public static final String RULE_SET_NAME = "ruleSetName";
    public static final String FROM_GROUP_NAME = "fromGroupName";
    public static final String FROM_DICTIONARY_NAME = "fromDictionaryName";
    public static final String FROM_VALUE = "fromValue";
    public static final String TO_GROUP_NAME = "toGroupName";
    public static final String TO_DICTIONARY_NAME = "toDictionaryName";
    public static final String COUNT = "count";

    public static final String PROCESSING_DATE = "processingDate";
    public static final String PROCESSING_STATE = "processingState";
    public static final String PROCESSING_AUTHOR = "processingAuthor";

    /** Дата начала агрегации событий */
    @NotNull
    private Date startDate;
    /** Дата окончания агрегации событий */
    @NotNull
    private Date endDate;
    /** Тип события */
    @NotNull
    private NotifyType type;

    /** Название набора правил перекодирования, к которому относится оповещение */
    @Size(max = DOCUMENT_NAME_LENGTH)
    private String ruleSetName;

    /** Название группы справочника источника, к которому относится оповещение */
    @Size(max = DOCUMENT_NAME_LENGTH)
    private String fromGroupName;
    /** Название справочника источника, к которому относится оповещение */
    @Size(max = DOCUMENT_NAME_LENGTH)
    private String fromDictionaryName;
    /** Значение справочника источника */
    private String fromValue;

    /** Название группы справочника назначения, к которому относится оповещение */
    @Size(max = DOCUMENT_NAME_LENGTH)
    private String toGroupName;
    /** Название справочника назначения, к которому относится оповещение */
    @Size(max = DOCUMENT_NAME_LENGTH)
    private String toDictionaryName;

    /** Количество событий */
    @NotNull
    private volatile Integer count;
    /** Дата обработки события */
    private Date processingDate;
    /** Текущее состояние события */
    @NotNull
    private NotifyState processingState;
    /** Автор обработки события */
    private String processingAuthor;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append(TYPE, getType())
                .append(COUNT, getCount())
                .append(PROCESSING_STATE, getProcessingState())
                .append(PROCESSING_DATE, DateUtil.formatDateTime(getProcessingDate()))
                .append(PROCESSING_AUTHOR, getProcessingAuthor())
                .toString();
    }
}
