package ru.hflabs.rcd.task;

import org.springframework.validation.Errors;
import ru.hflabs.rcd.exception.constraint.task.IllegalCronSyntaxException;
import ru.hflabs.rcd.exception.search.UnknownTaskDefinitionException;
import ru.hflabs.rcd.model.task.TaskDescriptor;
import ru.hflabs.rcd.service.ITaskDefinitionRepository;
import ru.hflabs.rcd.service.document.ChangeValidatorService;
import ru.hflabs.util.core.FormatUtil;

/**
 * Класс <class>TaskDescriptorValidator</class> реализует сервис валидации дескриптора задачи
 *
 * @author Nazin Alexander
 */
public class TaskDescriptorValidator extends ChangeValidatorService<TaskDescriptor> {

    /** Сервис работы с репозиторием предопределенных задач */
    private ITaskDefinitionRepository taskDefinitionRepository;

    public TaskDescriptorValidator() {
        super(TaskDescriptor.class);
    }

    public void setTaskDefinitionRepository(ITaskDefinitionRepository taskDefinitionRepository) {
        this.taskDefinitionRepository = taskDefinitionRepository;
    }

    @Override
    protected TaskDescriptor formatValue(TaskDescriptor target) {
        target.setName(FormatUtil.parseString(target.getName()));
        target.setCron(FormatUtil.parseString(target.getCron()));
        return super.formatValue(target);
    }

    /**
     * Выполняет валидацию параметров дескриптора
     *
     * @param errors контейнер ошибок
     * @param target проверяемый дескриптор
     */
    private void doValidateParameters(Errors errors, TaskDescriptor target) {
        errors.pushNestedPath(TaskDescriptor.PARAMETERS);
        try {
            doValidateAnnotations(errors, target.getParameters());
        } finally {
            errors.popNestedPath();
        }
    }

    /**
     * Выполняет валидацию на основе предопределенного дескриптора
     *
     * @param errors контейнер ошибок
     * @param target проверяемый дескриптор
     */
    private void doValidatePredefinedDescriptor(Errors errors, TaskDescriptor target) {
        try {
            // Выполняет заполнение динамических
            target = taskDefinitionRepository.populate(target);
            // Выполняем валидацию параметров
            doValidateParameters(errors, target);
        } catch (UnknownTaskDefinitionException ex) {
            rejectValue(errors, TaskDescriptor.NAME, ex, target.getName());
        } catch (IllegalCronSyntaxException ex) {
            rejectValue(errors, TaskDescriptor.CRON, ex, target.getCron());
        }
    }

    @Override
    protected void doValidate(Errors errors, TaskDescriptor target) {
        super.doValidate(errors, target);
        if (!errors.hasErrors()) {
            doValidatePredefinedDescriptor(errors, target);
        }
    }
}
