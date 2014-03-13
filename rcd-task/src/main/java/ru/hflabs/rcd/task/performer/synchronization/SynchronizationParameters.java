package ru.hflabs.rcd.task.performer.synchronization;

import ru.hflabs.rcd.model.task.TaskParameterDefinition;
import ru.hflabs.rcd.task.performer.ParametersHolder;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Класс <class>SynchronizationParameters</class> реализует декоратор параметров синхронизации справочников
 *
 * @see ParametersHolder
 */
public abstract class SynchronizationParameters extends ParametersHolder {

    /** Таймаут соединения с сервисом в сек (для бесконечного значение используется 0) */
    public static final transient TaskParameterDefinition<Long> TIMEOUT = new TaskParameterDefinition<>("timeout", (long) (5 * 60));
    /** Количество потоков синхронизации справочников */
    public static final transient TaskParameterDefinition<Integer> POOL_SIZE = new TaskParameterDefinition<>("poolSize", -1);

    @NotNull
    @Min(value = 0)
    @Max(value = Long.MAX_VALUE)
    public Long getTimeout() {
        return retrieveParameter(TIMEOUT.name, Long.class);
    }

    public void setTimeout(Long timeout) {
        injectParameter(TIMEOUT.name, timeout);
    }

    @Min(value = -1)
    @Max(value = Integer.MAX_VALUE)
    public int getPoolSize() {
        return retrieveParameter(POOL_SIZE.name, Integer.class, POOL_SIZE.value);
    }

    public void setPoolSize(int poolSize) {
        injectParameter(POOL_SIZE.name, poolSize);
    }
}
