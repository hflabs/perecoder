package ru.hflabs.rcd.backend.console.task;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import ru.hflabs.rcd.backend.console.RunDescriptor;
import ru.hflabs.rcd.backend.console.RunTemplate;
import ru.hflabs.rcd.backend.console.preference.Preference;
import ru.hflabs.rcd.exception.ApplicationException;

import java.util.Collections;

/**
 * Класс <class>PropagateTaskDescriptor</class> реализует приложение делегирования событий серверу
 *
 * @author Nazin Alexander
 */
public final class PropagateTaskDescriptor extends RunTemplate<Preference, TaskCommand, RunDescriptor> {

    @Override
    protected RunDescriptor doExecute(Preference preference, TaskCommand command) throws Exception {
        RunDescriptor result = new RunDescriptor(Collections.<Throwable>emptyList());
        try {
            managerService.propagateEvent(command.createTaskDescriptor());
        } catch (Throwable ex) {
            result.setErrors(Lists.<Throwable>newArrayList(new ApplicationException(String.format("Can't rebuild. Cause by: %s", ex.getMessage()), ex)));
        }
        return result;
    }

    public static void main(String[] args) {
        parseCmdArguments(PropagateTaskDescriptor.class, ImmutableSet.<TaskCommand>of(new RebuildIndexTaskPreference()), args);
    }
}
