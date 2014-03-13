package ru.hflabs.rcd.task.performer.dummy;

import ru.hflabs.rcd.task.performer.TaskResultDetails;

import java.util.Date;

/**
 * Класс <class>DummyTaskResult</class> реализует декоратор результатов пустой задачи
 *
 * @see TaskResultDetails
 */
public class DummyResult extends TaskResultDetails {

    public static final transient String LONG_VALUE = "longValue";
    public static final transient String INTEGER_VALUE = "integerValue";
    public static final transient String BOOLEAN_VALUE = "booleanValue";
    public static final transient String DATE_VALUE = "dateValue";

    public Long getLongValue() {
        return retrieveParameter(LONG_VALUE, Long.TYPE, 0L);
    }

    public void setLongValue(long longValue) {
        injectParameter(LONG_VALUE, longValue);
    }

    public Integer getIntegerValue() {
        return retrieveParameter(INTEGER_VALUE, Integer.TYPE, 0);
    }

    public void setIntegerValue(int integerValue) {
        injectParameter(INTEGER_VALUE, integerValue);
    }

    public Boolean getBooleanValue() {
        return retrieveParameter(BOOLEAN_VALUE, Boolean.TYPE, Boolean.FALSE);
    }

    public void setBooleanValue(Boolean booleanValue) {
        injectParameter(BOOLEAN_VALUE, booleanValue);
    }

    public Date getDateValue() {
        return retrieveParameter(DATE_VALUE, Date.class, null);
    }

    public void setDateValue(Date dateValue) {
        injectParameter(DATE_VALUE, dateValue);
    }
}
