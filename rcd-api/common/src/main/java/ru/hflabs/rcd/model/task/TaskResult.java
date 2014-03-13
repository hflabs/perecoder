package ru.hflabs.rcd.model.task;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import ru.hflabs.rcd.model.EssenceTemplate;
import ru.hflabs.rcd.model.annotation.Indexed;
import ru.hflabs.util.core.date.DateUtil;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.Map;

/**
 * Класс <class>TaskResult</class> описывает результат выполнения задачи
 */
@Getter
@Setter
@Indexed(
        id = TaskResult.PRIMARY_KEY,
        fields = {
                @Indexed.Field(TaskResult.DESCRIPTOR_ID),
                @Indexed.Field(TaskResult.OWNER),
                @Indexed.Field(TaskResult.AUTHOR),
                @Indexed.Field(TaskResult.REGISTRATION_DATE),
                @Indexed.Field(TaskResult.START_DATE),
                @Indexed.Field(TaskResult.END_DATE),
                @Indexed.Field(TaskResult.STATUS)
        }
)
public class TaskResult extends EssenceTemplate {

    private static final long serialVersionUID = -4506107636665213495L;

    /*
     * Название полей с идентификаторами
     */
    public static final String DESCRIPTOR_ID = "descriptorId";
    public static final String OWNER = "owner";
    public static final String AUTHOR = "author";
    public static final String REGISTRATION_DATE = "registrationDate";
    public static final String START_DATE = "startDate";
    public static final String END_DATE = "endDate";
    public static final String STATUS = "status";

    /** Максимальный размер идентификатора */
    public static final int ERROR_MESSAGE_MAX_SIZE = 2000;

    /** Идентификатор дескриптора задачи */
    @Size(max = PRIMARY_KEY_MAX_SIZE)
    private String descriptorId;
    /** Идентификатор сервиса запуска */
    @NotNull
    private String owner;
    /** Автор запуска задачи */
    @NotNull
    private String author;

    /** Дата регистрации в планировщике */
    @NotNull
    private Date registrationDate;
    /** Дата запуска */
    @NotNull
    private Date startDate;
    /** Дата завершения */
    @NotNull
    private Date endDate;

    /** Общий статус выполнения */
    @NotNull
    private TaskResultStatus status = TaskResultStatus.UNKNOWN;
    /** Сообщение об ошибке */
    @Size(max = ERROR_MESSAGE_MAX_SIZE)
    private String errorMessage;

    /** Параметры выполнения */
    private Map<String, Object> parameters;
    /** Результаты выполнения */
    private Map<String, Object> content;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append(DESCRIPTOR_ID, getDescriptorId())
                .append(OWNER, getOwner())
                .append(AUTHOR, getAuthor())
                .append(REGISTRATION_DATE, DateUtil.formatDateTime(getRegistrationDate()))
                .append(START_DATE, DateUtil.formatDateTime(getStartDate()))
                .append(END_DATE, DateUtil.formatDateTime(getEndDate()))
                .append(STATUS, getStatus())
                .toString();
    }
}
