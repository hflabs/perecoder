package ru.hflabs.rcd.task.performer.dummy;

import org.springframework.util.StringUtils;
import ru.hflabs.rcd.model.task.TaskProgress;
import ru.hflabs.rcd.model.task.TaskResultStatus;
import ru.hflabs.rcd.task.performer.TaskPerformerTemplate;
import ru.hflabs.rcd.task.performer.TaskProgressHolder;
import ru.hflabs.util.security.SecurityUtil;

import java.util.Date;

/**
 * Класс <class>DummyTaskPerformer</class> реализует пустого исполнителя задачи
 *
 * @author Nazin Alexander
 */
public class DummyTaskPerformer extends TaskPerformerTemplate<DummyParameters, DummyResult> {

    @Override
    public Class<DummyParameters> retrieveParameterClass() {
        return DummyParameters.class;
    }

    @Override
    public Class<DummyResult> retrieveResultClass() {
        return DummyResult.class;
    }

    @Override
    protected DummyResult doPerformTask(DummyParameters parameters) throws Exception {
        DummyResult result = new DummyResult();
        changeProgress(TaskProgress.INFINITE_PROGRESS, "Starting", "starting");
        TaskProgressHolder context = new TaskProgressHolder(new Date(), SecurityUtil.getCurrentUserName(), parameters.getCount());
        for (int i = 1; i <= parameters.getCount() && !isCancelled(); i++) {
            changeProgress(context.nextStep(), "Iteration {0}", "iteration", i);
            TaskProgressHolder subProgress = new TaskProgressHolder(parameters.getDelay(), context);
            for (int j = 0; j < parameters.getDelay(); j++) {
                changeProgress(subProgress.nextStep(), "Iteration {0}", "iteration", i);
                Thread.sleep(1L);
            }
            result.setLongValue(i);
            result.setIntegerValue(i);
        }
        result.setBooleanValue(isCancelled());
        result.setDateValue(new Date());
        if (isCancelled()) {
            result.setStatus(TaskResultStatus.CANCELED);
        } else {
            if (StringUtils.hasText(parameters.getErrorMessage())) {
                result.injectThrowable(new IllegalArgumentException(parameters.getErrorMessage()));
                result.setStatus(TaskResultStatus.ERROR);
            } else {
                changeProgress(TaskProgress.MAX_PROGRESS, "Finishing", "finished");
                result.setStatus(TaskResultStatus.FINISHED);
            }
        }
        return result;
    }
}
