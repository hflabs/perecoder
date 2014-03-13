package ru.hflabs.rcd.model.change;

/**
 * Класс <class>ChangeType</class> реализует перечисление доступных типов изменений сущностей
 */
public enum ChangeType {

    /** Игнорирование изменений */
    IGNORE,
    /** Нет изменений */
    SKIP,
    /** Создание */
    CREATE,
    /** Обновление */
    UPDATE,
    /** Восстановление */
    RESTORE,
    /** Закрытие */
    CLOSE;

    /*
     * Предопределенные наборы
     */
    public static final ChangeType[] ACTUAL_SET = new ChangeType[]{ChangeType.SKIP, ChangeType.CREATE, ChangeType.UPDATE, ChangeType.RESTORE};
    public static final ChangeType[] CHANGED_SET = new ChangeType[]{ChangeType.CREATE, ChangeType.UPDATE, ChangeType.RESTORE};
    public static final ChangeType[] CLOSED_SET = new ChangeType[]{ChangeType.CLOSE};
}
