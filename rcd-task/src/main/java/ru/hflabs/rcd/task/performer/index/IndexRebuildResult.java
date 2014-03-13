package ru.hflabs.rcd.task.performer.index;

import ru.hflabs.rcd.model.task.TaskParameterDefinition;
import ru.hflabs.rcd.model.task.TaskResultStatus;
import ru.hflabs.rcd.task.performer.ParametersHolder;
import ru.hflabs.rcd.task.performer.TaskResultDetails;

import java.util.Collection;

/**
 * Класс <class>IndexRebuildResult</class> реализует декоратор результатов перестроения индекса
 *
 * @see ParametersHolder
 */
public class IndexRebuildResult extends ParametersHolder {

    /** Коллекция перестроенных индексов */
    public static final transient TaskParameterDefinition<Collection<Index>> INDEXES = new TaskParameterDefinition<>("indexes", null);

    public IndexRebuildResult() {
        super();
    }

    public IndexRebuildResult(Collection<Index> indexes) {
        setIndexes(indexes);
    }

    public void setIndexes(Collection<Index> indexes) {
        injectParameter(INDEXES.name, indexes);
    }

    /**
     * Класс <class>Content</class> содержит результат перестроения индекса
     *
     * @see IndexRebuildResult
     */
    public static class Index extends TaskResultDetails {

        /** Целевой класс индекса */
        public static final transient TaskParameterDefinition<String> TARGET_CLASS = new TaskParameterDefinition<>("targetClass", null);
        /** Общее количество документов в индексе */
        public static final transient TaskParameterDefinition<Integer> DOCUMENT_COUNT = new TaskParameterDefinition<>("documentCount", 0);

        public Index() {
            this(TaskResultStatus.UNKNOWN, null, -1);
        }

        public Index(TaskResultStatus status, String targetClass, int documentCount) {
            setStatus(status);
            setTargetClass(targetClass);
            setDocumentCount(documentCount);
        }

        public String getTargetClass() {
            return retrieveParameter(TARGET_CLASS.name, String.class, TARGET_CLASS.value);
        }

        public void setTargetClass(String targetClass) {
            injectParameter(TARGET_CLASS.name, targetClass);
        }

        public int getDocumentCount() {
            return retrieveParameter(DOCUMENT_COUNT.name, Integer.class, DOCUMENT_COUNT.value);
        }

        public void setDocumentCount(int documentCount) {
            injectParameter(DOCUMENT_COUNT.name, documentCount);
        }
    }
}
