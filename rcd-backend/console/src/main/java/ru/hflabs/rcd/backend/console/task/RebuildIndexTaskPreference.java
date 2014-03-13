package ru.hflabs.rcd.backend.console.task;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.collect.Sets;
import ru.hflabs.rcd.event.ContextEvent;
import ru.hflabs.rcd.event.index.IndexRebuildEvent;
import ru.hflabs.rcd.model.document.*;
import ru.hflabs.rcd.model.notification.Notification;
import ru.hflabs.rcd.model.rule.RecodeRule;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;
import ru.hflabs.rcd.model.task.TaskDescriptor;
import ru.hflabs.rcd.model.task.TaskResult;
import ru.hflabs.util.core.FormatUtil;

import java.util.Set;

/**
 * Класс <class>RebuildIndexEventPreference</class> описывает параметры перестроения поисковых индексов
 *
 * @author Nazin Alexander
 */
@Parameters(commandNames = RebuildIndexTaskPreference.COMMAND_NAME, commandDescription = "rebuild indexes")
public class RebuildIndexTaskPreference implements TaskCommand {

    /** Название операции */
    public static final String COMMAND_NAME = "rebuild";

    @Parameter(names = {"-f", "--force"}, description = "force rebuild", arity = 1, hidden = true)
    private boolean force = true;

    @Parameter(names = {"-m", "--mode"}, description = "rebuild mode", hidden = true)
    private String rebuildMode = Mode.ALL.name();

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    public boolean isForce() {
        return force;
    }

    public Mode getRebuildMode() {
        Mode targetMode = FormatUtil.parseEnumQuietly(rebuildMode.toUpperCase(), false, Mode.class);
        return targetMode != null ? targetMode : Mode.ALL;
    }

    @Override
    public ContextEvent createTaskDescriptor() {
        IndexRebuildEvent event = new IndexRebuildEvent(this, getRebuildMode().getTargetClasses());
        event.setForce(isForce());
        return event;
    }

    /**
     * Класс <class>Mode</class> реализует перечисление режимов перестроения индексов
     *
     * @author Nazin Alexander
     */
    public enum Mode {

        /** Все индексы */
        ALL {
            @Override
            public Set<Class<?>> getTargetClasses() {
                return null;
            }
        },
        /** Справочники */
        DICTIONARIES {
            @Override
            public Set<Class<?>> getTargetClasses() {
                return Sets.<Class<?>>newHashSet(Group.class, Dictionary.class, MetaField.class, Field.class, Record.class);
            }
        },
        /** Правила */
        RULES {
            @Override
            public Set<Class<?>> getTargetClasses() {
                return Sets.<Class<?>>newHashSet(RecodeRuleSet.class, RecodeRule.class);
            }
        },
        /** Оповещения */
        NOTIFICATIONS {
            @Override
            public Set<Class<?>> getTargetClasses() {
                return Sets.<Class<?>>newHashSet(Notification.class);
            }
        },
        /** Задачи */
        TASKS {
            @Override
            public Set<Class<?>> getTargetClasses() {
                return Sets.<Class<?>>newHashSet(TaskDescriptor.class, TaskResult.class);
            }
        };

        /**
         * Возвращает коллекцию целевых классов для перестроения
         *
         * @return Возвращает коллекцию целевых классов для перестроения
         */
        public abstract Set<Class<?>> getTargetClasses();
    }
}
