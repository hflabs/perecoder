package ru.hflabs.rcd.model.task;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import ru.hflabs.rcd.model.Descriptioned;
import ru.hflabs.rcd.model.Named;
import ru.hflabs.rcd.model.annotation.Hashed;
import ru.hflabs.rcd.model.annotation.Indexed;
import ru.hflabs.rcd.model.document.DocumentTemplate;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.Map;

/**
 * Класс <class>TaskDescriptor</class> описывает параметры выполнения задачи
 */
@Getter
@Setter
@Indexed(
        id = TaskDescriptor.PRIMARY_KEY,
        fields = {
                @Indexed.Field(TaskDescriptor.HISTORY_ID),
                @Indexed.Field(TaskDescriptor.CHANGE_TYPE),
                @Indexed.Field(TaskDescriptor.CHANGE_DATE),
                @Indexed.Field(TaskDescriptor.NAME),
                @Indexed.Field(TaskDescriptor.CRON)
        }
)
@Hashed(ignore = {
        TaskDescriptor.PRIMARY_KEY, TaskDescriptor.HISTORY_ID, TaskDescriptor.NAME
})
public class TaskDescriptor extends DocumentTemplate implements Named, Descriptioned {

    private static final long serialVersionUID = -7930945169739535612L;

    /*
     * Название полей с идентификаторами
     */
    public static final String CRON = "cron";
    public static final String PARAMETERS = "parameters";

    /** Идентификатор исполнителя */
    @NotNull
    @Size(min = NAME_MIN_SIZE, max = NAME_MAX_SIZE)
    private String name;
    /** Описание задачи */
    @NotNull
    @Size(max = DESCRIPTION_SIZE)
    private String description;

    /** CRON тригер запуска или NULL */
    private String cron;
    /** Дата следующего запуска задачи */
    private transient Date nextScheduledDate;
    /** Параметры */
    private Map<String, Object> parameters;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append(NAME, getName())
                .append(DESCRIPTION, getDescription())
                .append(CRON, getCron())
                .toString();
    }
}
