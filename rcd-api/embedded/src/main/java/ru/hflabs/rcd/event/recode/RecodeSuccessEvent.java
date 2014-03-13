package ru.hflabs.rcd.event.recode;

import lombok.Getter;
import ru.hflabs.rcd.model.path.FieldNamedPath;

/**
 * Класс <class>RecodeSuccessEvent</class> содержит информацию об успешной перекодировке
 *
 * @see RecodeEvent
 */
@Getter
public class RecodeSuccessEvent extends RecodeEvent {

    private static final long serialVersionUID = 3713251024003831774L;

    /** Именованный путь значения поля источника */
    private final FieldNamedPath fromPath;
    /** Именованный путь значения поля назначения */
    private final FieldNamedPath toPath;

    public RecodeSuccessEvent(Object source, String ruleSetName, FieldNamedPath fromPath, FieldNamedPath toPath) {
        super(source, ruleSetName);
        this.fromPath = fromPath;
        this.toPath = toPath;
    }
}
