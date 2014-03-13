package ru.hflabs.rcd.model.change;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import ru.hflabs.rcd.model.EssenceTemplate;
import ru.hflabs.rcd.model.annotation.Indexed;
import ru.hflabs.util.core.date.DateUtil;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.Date;

/**
 * Класс <class>History</class> содержит информацию об истории изменения сущности
 *
 * @see ru.hflabs.rcd.model.Historical
 */
@Getter
@Setter
@Indexed(
        id = History.PRIMARY_KEY,
        fields = {
                @Indexed.Field(History.TARGET_ID),
                @Indexed.Field(History.TARGET_TYPE),
                @Indexed.Field(History.EVENT_TYPE),
                @Indexed.Field(History.EVENT_DATE),
                @Indexed.Field(History.EVENT_AUTHOR)
        }
)
public final class History extends EssenceTemplate {

    private static final long serialVersionUID = -5196925126737451364L;

    /*
     * Название полей с идентификаторами
     */
    public static final String TARGET_ID = "targetId";
    public static final String TARGET_TYPE = "targetType";
    public static final String EVENT_TYPE = "eventType";
    public static final String EVENT_DATE = "eventDate";
    public static final String EVENT_AUTHOR = "eventAuthor";

    /** Идентификатор целевого объекта */
    @NotNull
    @Size(max = PRIMARY_KEY_MAX_SIZE)
    private String targetId;
    /** Тип целевого объекта */
    @NotNull
    private String targetType;
    /** Тип события */
    @NotNull
    private ChangeType eventType;
    /** Дата события */
    @NotNull
    private Date eventDate;
    /** Автор события */
    @NotNull
    private String eventAuthor;
    /** Набор изменений */
    private transient Collection<Diff> diffs;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append(TARGET_ID, getTargetId())
                .append(TARGET_TYPE, getTargetType())
                .append(EVENT_TYPE, getEventType())
                .append(EVENT_DATE, DateUtil.formatDateTime(getEventDate()))
                .append(EVENT_AUTHOR, getEventAuthor())
                .toString();
    }
}
