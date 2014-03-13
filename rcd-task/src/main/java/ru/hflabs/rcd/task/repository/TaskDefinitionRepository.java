package ru.hflabs.rcd.task.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.exception.constraint.task.IllegalCronSyntaxException;
import ru.hflabs.rcd.exception.search.UnknownTaskDefinitionException;
import ru.hflabs.rcd.model.definition.ModelDefinition;
import ru.hflabs.rcd.model.definition.ModelFieldDefinition;
import ru.hflabs.rcd.model.task.TaskDefinition;
import ru.hflabs.rcd.model.task.TaskDescriptor;
import ru.hflabs.rcd.model.task.TaskParameterDefinition;
import ru.hflabs.rcd.service.IServiceFactory;
import ru.hflabs.rcd.service.ITaskDefinitionRepository;
import ru.hflabs.util.core.collection.IteratorUtil;
import ru.hflabs.util.spring.Assert;
import ru.hflabs.util.spring.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Класс <class>TaskDefinitionRepository</class> реализует сервис работы с репозиторием задач, объявленных в контексте приложения
 *
 * @author Nazin Alexander
 */
@Setter
public class TaskDefinitionRepository implements ITaskDefinitionRepository, BeanFactoryAware, InitializingBean, DisposableBean {

    /** Пустой контекст триггера. Используется для вычисления времени следующего запуска задачи */
    private static final TriggerContext EMPTY_TRIGGER_CONTEXT = new SimpleTriggerContext();

    /** Фабрика создания классов */
    @Setter(AccessLevel.NONE)
    private ListableBeanFactory beanFactory;
    /** Сервис конвертации объектов */
    private ObjectMapper objectMapper;
    /** Фабрика описания моделей */
    private IServiceFactory<ModelDefinition, Class<?>> modelDefinitionFactory;
    /** Коллекция поддерживаемых дескрипторов */
    private Map<String, TaskDefinition> definitions;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof ListableBeanFactory) {
            this.beanFactory = (ListableBeanFactory) beanFactory;
        } else {
            throw new FactoryBeanNotInitializedException(String.format("BeanFactory must be instance of '%s'", ListableBeanFactory.class.getSimpleName()));
        }
    }

    @Override
    public Class<TaskDefinition> retrieveTargetClass() {
        return TaskDefinition.class;
    }

    @Override
    public Integer totalCount() {
        return definitions.size();
    }

    @Override
    public List<TaskDefinition> getAll() {
        return ImmutableList.copyOf(definitions.values());
    }

    @Override
    public Iterator<List<TaskDefinition>> iterateAll(int fetchSize, int cacheSize) {
        return IteratorUtil.toPageIterator(getAll().iterator(), fetchSize);
    }

    @Override
    public TaskDefinition findUniqueByNamedPath(String path, boolean quietly) {
        Assert.isTrue(StringUtils.hasText(path), "Task descriptor name must not be NULL or EMPTY");
        TaskDefinition definition = definitions.get(path);
        if (definition == null && !quietly) {
            throw new UnknownTaskDefinitionException(path);
        }
        return definition;
    }

    @Override
    public <T extends Map<String, Object>> T convertTaskParameters(Class<T> parametersClass, Map<String, Object> parameters) {
        Assert.notNull(parametersClass, "Task parameters class must not be NULL");
        return objectMapper.convertValue(parameters, parametersClass);
    }

    @Override
    public Map<String, Object> convertTaskResults(Map<String, Object> results) {
        return objectMapper.convertValue(results, new TypeReference<Map<String, Object>>() {
        });
    }

    @Override
    public TaskDescriptor populateParameters(TaskDescriptor descriptor) {
        Assert.notNull(descriptor, "Task descriptor must not be NULL");
        TaskDefinition definition = findUniqueByNamedPath(descriptor.getName(), false);
        {
            descriptor.setName(definition.getId());
            descriptor.setParameters(
                    convertTaskParameters(
                            definition.getParametersClass(),
                            descriptor.getParameters() != null ? descriptor.getParameters() : definition.getDefaultParameters())
            );
        }
        return descriptor;
    }

    @Override
    public TaskDescriptor populateScheduleDate(TaskDescriptor descriptor) {
        Assert.notNull(descriptor, "Task descriptor must not be NULL");
        String cronExpression = descriptor.getCron();
        if (StringUtils.hasText(cronExpression)) {
            try {
                Trigger trigger = new CronTrigger(cronExpression);
                try {
                    descriptor.setNextScheduledDate(trigger.nextExecutionTime(EMPTY_TRIGGER_CONTEXT));
                } catch (IllegalArgumentException ex) {
                    descriptor.setNextScheduledDate(null);
                }
            } catch (IllegalArgumentException ex) {
                throw new IllegalCronSyntaxException(ex);
            }
        } else {
            descriptor.setNextScheduledDate(null);
        }
        return descriptor;
    }

    @Override
    public TaskDescriptor populate(TaskDescriptor descriptor) {
        descriptor = populateParameters(descriptor);
        descriptor = populateScheduleDate(descriptor);
        return descriptor;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Формируем описание дескриптора задачи
        ModelDefinition taskDescriptorModel = modelDefinitionFactory.retrieveService(TaskDescriptor.class);
        // Формируем описание для каждой задачи, которая объявлена в контексте
        ImmutableMap.Builder<String, TaskDefinition> result = ImmutableMap.builder();
        for (TaskDefinition definition : beanFactory.getBeansOfType(TaskDefinition.class, true, true).values()) {
            // Формируем параметры задачи по умолчанию
            final Map<String, Object> defaultParameters = Maps.newLinkedHashMap();
            ReflectionUtil.doWithFields(
                    definition.getParametersClass(),
                    new ReflectionUtils.FieldCallback() {
                        @Override
                        public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                            TaskParameterDefinition<?> defaultParameter = TaskParameterDefinition.class.cast(ReflectionUtil.get(field, null));
                            defaultParameters.put(defaultParameter.name, defaultParameter.value);
                        }
                    },
                    new ReflectionUtils.FieldFilter() {
                        @Override
                        public boolean matches(Field field) {
                            return ReflectionUtil.isPublicStaticFinal(field) && TaskParameterDefinition.class.isAssignableFrom(field.getType());
                        }
                    }
            );
            definition.setDefaultParameters(defaultParameters);
            // Формируем описание параметров
            ImmutableMap.Builder<String, ModelFieldDefinition> fieldDefinitions = ImmutableMap.builder();
            for (Map.Entry<String, ModelFieldDefinition> entry : modelDefinitionFactory.retrieveService(definition.getParametersClass()).getFields().entrySet()) {
                fieldDefinitions.put(
                        String.format("%s.%s", TaskDescriptor.PARAMETERS, entry.getKey()),
                        entry.getValue()
                );
            }

            // Добавляем поля дескриптора
            fieldDefinitions.putAll(taskDescriptorModel.getFields());
            definition.setFields(fieldDefinitions.build());

            result.put(definition.getId(), definition);
        }
        definitions = result.build();
    }

    @Override
    public void destroy() throws Exception {
        definitions = null;
    }
}
