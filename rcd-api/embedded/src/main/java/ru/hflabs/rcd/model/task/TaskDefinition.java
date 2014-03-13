package ru.hflabs.rcd.model.task;

import lombok.Getter;
import lombok.Setter;
import ru.hflabs.rcd.model.Permissioned;
import ru.hflabs.rcd.model.definition.ModelDefinition;

import javax.xml.bind.annotation.XmlTransient;
import java.util.Map;

import static ru.hflabs.rcd.model.ModelUtils.hasPermission;

/**
 * Класс <class>TaskDefinition</class> описывает модель задачи
 *
 * @see ModelDefinition
 * @see Permissioned
 */
@Getter
@Setter
public class TaskDefinition extends ModelDefinition implements Permissioned {

    private static final long serialVersionUID = -8824129565701458933L;

    /** Права безопасности задачи */
    private int permissions;
    /** Класс параметров */
    private transient Class<? extends Map<String, Object>> parametersClass;

    public TaskDefinition() {
        this.permissions = Permissioned.PERMISSION_ALL;
    }

    @Override
    public void injectId(String targetId) {
        setId(targetId);
    }

    @XmlTransient
    public Class<? extends Map<String, Object>> getParametersClass() {
        return parametersClass;
    }

    public boolean isDeletable() {
        return hasPermission(this, Permissioned.PERMISSION_WRITE);
    }
}
