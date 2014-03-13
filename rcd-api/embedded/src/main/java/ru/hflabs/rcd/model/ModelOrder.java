package ru.hflabs.rcd.model;

import com.google.common.collect.ImmutableMap;
import ru.hflabs.rcd.model.change.History;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.notification.Notification;
import ru.hflabs.rcd.model.rule.RecodeRule;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;
import ru.hflabs.rcd.model.task.TaskDescriptor;
import ru.hflabs.rcd.model.task.TaskResult;

import java.util.Map;

/**
 * Класс <class>ModelOrder</class> содержит информацию о зависимостях сущностей, которые <b>ВАЖЕН</b>ы при обходе слушателей
 *
 * @see org.springframework.core.Ordered#getOrder()
 */
public abstract class ModelOrder {

    /** Порядок по умолчанию */
    public static final Integer DEFAULT_ORDER = -10000;
    /** Карта приоритетов */
    private static final Map<Class<?>, Integer> ORDERS = ImmutableMap.<Class<?>, Integer>builder()
            // common
            .put(History.class, -5000)
            .put(Notification.class, -3000)
                    // documents
            .put(Group.class, -2000)
            .put(Dictionary.class, -1900)
            .put(MetaField.class, -1800)
            .put(Field.class, -1700)
            .put(RecodeRuleSet.class, -1500)
            .put(RecodeRule.class, -1400)
                    // tasks
            .put(TaskDescriptor.class, 0)
            .put(TaskResult.class, 0)
            .build();

    protected ModelOrder() {
        // embedded constructor
    }

    /**
     * @param targetClass целевой класс сущности
     * @return Возвращает заданный приоритет сущности по ее классу
     */
    public static Integer getOrder(Class<?> targetClass) {
        return ORDERS.containsKey(targetClass) ? ORDERS.get(targetClass) : DEFAULT_ORDER;
    }
}
