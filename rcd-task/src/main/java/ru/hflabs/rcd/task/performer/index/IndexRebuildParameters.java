package ru.hflabs.rcd.task.performer.index;

import ru.hflabs.rcd.model.task.TaskParameterDefinition;
import ru.hflabs.rcd.task.performer.ParametersHolder;

import java.util.Collection;

/**
 * Класс <class>IndexRebuildParameters</class> реализует декоратор параметров перестроения индекса
 *
 * @see ru.hflabs.rcd.task.performer.ParametersHolder
 */
public class IndexRebuildParameters extends ParametersHolder {

    /** Целевые классы */
    public static final transient TaskParameterDefinition<Collection<String>> TARGET = new TaskParameterDefinition<>("target", null);
    /** Флаг принудительного пререстроения */
    public static final transient TaskParameterDefinition<Boolean> FORCE = new TaskParameterDefinition<>("force", Boolean.FALSE);

    public void setTarget(Collection<String> target) {
        injectParameter(TARGET.name, target);
    }

    @SuppressWarnings("unchecked")
    public Collection<String> getTarget() {
        return retrieveParameter(TARGET.name, Collection.class, TARGET.value);
    }

    public boolean isForce() {
        return retrieveParameter(FORCE.name, Boolean.class, FORCE.value);
    }

    public void setForce(boolean force) {
        injectParameter(FORCE.name, force);
    }
}
