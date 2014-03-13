package ru.hflabs.rcd.model;

import ru.hflabs.rcd.model.change.ChangeType;
import ru.hflabs.rcd.model.change.History;

import java.util.Date;

/**
 * Интерфейс <class>Historical</class> декларирует методы объекта, который обладает историей
 *
 * @see History
 */
public interface Historical {

    /** Название поля с идентификатором истории */
    String HISTORY_ID = "historyId";
    /** Название поля с типом изменения */
    String CHANGE_TYPE = "changeType";
    /** Название поля с датой изменения */
    String CHANGE_DATE = "changeDate";

    /**
     * @return Возвращает символическое название объекта истории
     */
    String getHistoryName();

    /**
     * @return Возвращает идентификатор истории
     */
    String getHistoryId();

    /**
     * Устаналивает идентификатор истории
     *
     * @param historyId идентификатор
     */
    void setHistoryId(String historyId);

    /**
     * @return Возвращает последнее событие объекта
     */
    ChangeType getChangeType();

    /**
     * @return Возвращает последнюю дату модификации объекта
     */
    Date getChangeDate();

    /**
     * @return Возвращает событие истории
     */
    History getHistory();

    /**
     * Устанавливает событие истории
     *
     * @param history событие
     */
    void setHistory(History history);
}
