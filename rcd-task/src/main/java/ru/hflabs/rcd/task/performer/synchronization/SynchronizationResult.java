package ru.hflabs.rcd.task.performer.synchronization;

import com.google.common.collect.ImmutableList;
import ru.hflabs.rcd.model.task.TaskParameterDefinition;
import ru.hflabs.rcd.model.task.TaskResultStatus;
import ru.hflabs.rcd.task.performer.ParametersHolder;
import ru.hflabs.rcd.task.performer.TaskResultDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Класс <class>SynchronizationResult</class> реализует декоратор результатов задачи синхронизации справоников
 *
 * @see ParametersHolder
 */
public class SynchronizationResult extends ParametersHolder {

    /** Общее количество синхронизированных справочников */
    public static final transient TaskParameterDefinition<Integer> TOTAL_COUNT = new TaskParameterDefinition<>("totalCount", 0);
    /** Количество справочников, которые прошли синхронизацию */
    public static final transient TaskParameterDefinition<Integer> SUCCESS_COUNT = new TaskParameterDefinition<>("successCount", 0);
    /** Количество справочников, которые не прошли синхронизацию */
    public static final transient TaskParameterDefinition<Integer> ERROR_COUNT = new TaskParameterDefinition<>("errorCount", 0);
    /** Коллекция синхронизированных справочников */
    public static final transient TaskParameterDefinition<Collection<Dictionary>> DICTIONARIES = new TaskParameterDefinition<>("dictionaries", null);

    public SynchronizationResult() {
        super();
    }

    public SynchronizationResult(Collection<Dictionary> dictionaries) {
        this(dictionaries, Collections.<Dictionary>emptyList());
    }

    public SynchronizationResult(Collection<Dictionary> successDictionaries, Collection<Dictionary> errorDictionaries) {
        setSuccessCount(successDictionaries.size());
        setErrorCount(errorDictionaries.size());
        setTotalCount(successDictionaries.size() + errorDictionaries.size());
        setDictionaries(
                ImmutableList.<Dictionary>builder()
                        .addAll(successDictionaries)
                        .addAll(errorDictionaries)
                        .build()
        );
    }

    public void setTotalCount(int count) {
        injectParameter(TOTAL_COUNT.name, count);
    }

    public void setSuccessCount(int count) {
        injectParameter(SUCCESS_COUNT.name, count);
    }

    public void setErrorCount(int count) {
        injectParameter(ERROR_COUNT.name, count);
    }

    public void setDictionaries(Collection<Dictionary> dictionaries) {
        injectParameter(DICTIONARIES.name, dictionaries);
    }

    /**
     * Класс <class>Dictionary</class> содержит результат синхронизации справочника
     *
     * @see SynchronizationResult
     */
    public static class Dictionary extends TaskResultDetails {

        /** Название группы */
        public static final transient TaskParameterDefinition<String> GROUP_NAME = new TaskParameterDefinition<>("groupName", null);
        /** Название справочника */
        public static final transient TaskParameterDefinition<String> DICTIONARY_NAME = new TaskParameterDefinition<>("dictionaryName", null);

        public Dictionary(TaskResultStatus status, String groupName, String dictionaryName) {
            setStatus(status);
            setGroupName(groupName);
            setDictionaryName(dictionaryName);
        }

        public Dictionary(String groupName, String dictionaryName, Throwable exception) {
            this(TaskResultStatus.ERROR, groupName, dictionaryName);
            injectThrowable(exception);
        }

        public String getGroupName() {
            return retrieveParameter(GROUP_NAME.name, String.class, GROUP_NAME.value);
        }

        public void setGroupName(String groupName) {
            injectParameter(GROUP_NAME.name, groupName);
        }

        public String getDictionaryName() {
            return retrieveParameter(DICTIONARY_NAME.name, String.class, DICTIONARY_NAME.value);
        }

        public void setDictionaryName(String dictionaryName) {
            injectParameter(DICTIONARY_NAME.name, dictionaryName);
        }
    }
}
