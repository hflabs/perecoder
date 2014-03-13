package ru.hflabs.rcd.event.recode;

import lombok.Getter;
import ru.hflabs.rcd.model.notification.NotifyType;
import ru.hflabs.rcd.model.path.DictionaryNamedPath;
import ru.hflabs.rcd.model.path.FieldNamedPath;

/**
 * Класс <class>RecodeFailedEvent</class> содержит информацию об ошибочной перекодировке
 *
 * @see ru.hflabs.rcd.model.notification.Notification
 */
@Getter
public class RecodeFailedEvent extends RecodeEvent {

    private static final long serialVersionUID = -3219409963358611551L;

    /** Именованный путь значения поля источника */
    private final FieldNamedPath fromPath;
    /** Именованный путь справочника */
    private final DictionaryNamedPath toPath;
    /** Тип события */
    private final NotifyType notifyType;
    /** Исключительная ситуация ошибочной перекодировки */
    private final Throwable cause;

    public RecodeFailedEvent(Object source, String ruleSetName, FieldNamedPath fromPath, DictionaryNamedPath toPath, NotifyType notifyType, Throwable cause) {
        super(source, ruleSetName);
        this.fromPath = fromPath;
        this.toPath = toPath;
        this.notifyType = notifyType;
        this.cause = cause;
    }
}
