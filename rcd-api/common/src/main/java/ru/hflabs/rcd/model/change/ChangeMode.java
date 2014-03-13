package ru.hflabs.rcd.model.change;

/**
 * Класс <class>ChangeMode</class> реализует перечисление доступных режимов изменений сущностей
 */
public enum ChangeMode {

    /** Стандартный режим - используется по умолчанию */
    DEFAULT,
    /** Изолированный - игнорирование всех взаимосвязанных сущностей */
    ISOLATED
}
