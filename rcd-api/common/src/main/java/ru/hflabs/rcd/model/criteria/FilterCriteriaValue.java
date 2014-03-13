package ru.hflabs.rcd.model.criteria;

import lombok.Getter;
import lombok.Setter;
import ru.hflabs.rcd.Constants;
import ru.hflabs.rcd.term.Condition;

import java.io.Serializable;
import java.util.*;

/**
 * Класс <class>FilterCriteriaValue</class> реализует базовый класс, содержащий информацию о значении фильтра
 *
 * @see FilterCriteria
 */
public abstract class FilterCriteriaValue<T> implements Serializable, Cloneable {

    private static final long serialVersionUID = -4423912487209616208L;

    /** Экземпляр пустого фильтра */
    public static final EmptyValue EMPTY_VALUE = new EmptyValue();
    /** Экземпляр не пустого фильтра */
    public static final NotEmptyValue NOT_EMPTY_VALUE = new NotEmptyValue();

    /** Значение фильтра */
    @Getter
    @Setter
    protected T value;
    /** Условие фильтра */
    @Getter
    @Setter
    private Condition condition = Condition.AND;

    public FilterCriteriaValue<T> injectCondition(Condition targetFilterCondition) {
        setCondition(targetFilterCondition);
        return this;
    }

    /**
     * Класс <class>ClauseValue</class> реализует ассоциативный фильтр значений
     *
     * @see FilterCriteriaValue
     */
    public static class ClauseValue extends FilterCriteriaValue<Map<String, FilterCriteriaValue<?>>> {

        private static final long serialVersionUID = 941901433966624416L;

        public ClauseValue(Map<String, FilterCriteriaValue<?>> values) {
            setValue(values);
        }
    }

    /**
     * Класс <code>EmptyValue</code> реализует фильтр для <b>пустых</b> значений
     *
     * @see FilterCriteriaValue
     */
    public static class EmptyValue extends FilterCriteriaValue<Void> {

        private static final long serialVersionUID = -3928837106062064288L;

        public EmptyValue() {
        }
    }

    /**
     * Класс <code>NotEmptyValue</code> реализует фильтр для <b>не пустых</b> значений
     *
     * @see FilterCriteriaValue
     */
    public static class NotEmptyValue extends FilterCriteriaValue<Void> {

        private static final long serialVersionUID = 9215072119417680630L;

        public NotEmptyValue() {
        }
    }

    /**
     * Класс <code>BooleanValue</code> реализует фильтр для логического типа
     *
     * @see FilterCriteriaValue
     */
    public static class BooleanValue extends FilterCriteriaValue<Boolean> {

        private static final long serialVersionUID = 2496480355347655744L;

        public BooleanValue(boolean value) {
            setValue(value);
        }
    }

    /**
     * Класс <class>EnumValue</class> реализует фильтр для перечесляемого типа
     *
     * @param <E> класс перечисляемого типа
     * @see FilterCriteriaValue
     */
    public static class EnumValues<E extends Enum<E>> extends FilterCriteriaValue<Set<E>> {

        private static final long serialVersionUID = -4160664667542031469L;

        /** Условие ассоциации значений */
        @Getter
        @Setter
        private Condition clauseCondition = Condition.OR;

        public EnumValues(E value) {
            this(value, Condition.AND);
        }

        public EnumValues(E value, Condition clauseCondition) {
            this(EnumSet.of(value), clauseCondition);
        }

        public EnumValues(Set<E> values) {
            this(values, Condition.OR);
        }

        public EnumValues(Set<E> values, Condition clauseCondition) {
            setValue(values);
            setClauseCondition(clauseCondition);
        }
    }

    /**
     * Класс <code>StringValue</code> реализует фильтр для строки
     *
     * @see FilterCriteriaValue
     */
    public static class StringValue extends FilterCriteriaValue<String> {

        private static final long serialVersionUID = 3403747781224133564L;

        public StringValue(String value) {
            setValue(value);
        }
    }

    /**
     * Класс <code>DateValue</code> реализует фильтр для даты
     *
     * @see FilterCriteriaValue
     */
    public static class DateValue extends FilterCriteriaValue<Date> {

        private static final long serialVersionUID = 5468589044634146431L;

        /** Дата окончания */
        @Getter
        private Date end;

        public DateValue(Date start, Date end) {
            setStart(start);
            setEnd(end);
        }

        public Date getStart() {
            return getValue();
        }

        public void setStart(Date start) {
            setValue(start != null ? createCalendar(start).getTime() : start);
        }

        public void setEnd(Date end) {
            if (end != null) {
                // Получаем календарь
                Calendar calendar = createCalendar(end);
                // Устанавливаем конец дня для даты
                calendar.add(Calendar.DATE, 1);
                calendar.add(Calendar.MILLISECOND, -1);
                // Устанавливаем дату
                this.end = calendar.getTime();
            } else {
                // Устанавливаем дату
                this.end = end;
            }
        }

        /**
         * Возвращает экземпляр календаря
         *
         * @return Возвращает экземпляр календаря
         */
        private static Calendar getCalendarInstance() {
            return Calendar.getInstance(TimeZone.getTimeZone(Constants.DEFAULT_TIME_ZONE));
        }

        /**
         * Создает и возвращает экземпляр календаря по дате, у которой заполнены только год/месяц/дата
         *
         * @param currentDate текущая дата
         * @return Возвращает экземпляр календаря по дате
         */
        private static Calendar createCalendar(Date currentDate) {
            assert currentDate != null : "Date must not be NULL";
            // Создаем текущий календарь
            Calendar currentCalendar = getCalendarInstance();
            currentCalendar.setTime(currentDate);
            // Создаем календарь с полями
            Calendar calendar = getCalendarInstance();
            calendar.clear();
            calendar.set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), currentCalendar.get(Calendar.DATE));
            return calendar;
        }
    }


    /**
     * Класс <code>StringsValue</code> реализует фильтр для коллекции строк
     *
     * @see FilterCriteriaValue
     */
    public static class StringsValue extends FilterCriteriaValue<Collection<String>> {

        private static final long serialVersionUID = 8398533870545393647L;

        /** Условие ассоциации значений */
        @Getter
        @Setter
        private Condition clauseCondition = Condition.OR;

        public StringsValue(Collection<String> values) {
            setValue(values);
        }

        public StringsValue(Collection<String> values, Condition clauseCondition) {
            this(values);
            setClauseCondition(clauseCondition);
        }
    }
}
