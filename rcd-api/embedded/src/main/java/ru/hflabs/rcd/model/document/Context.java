package ru.hflabs.rcd.model.document;

/**
 * Интерфейс <class>Context</class> декларирует методы контекста документа
 *
 * @see DocumentContext
 */
public interface Context<NP, I> {

    /**
     * @return Возвращает целевую сущность контекста
     */
    I getEssence();

    /**
     * @return Возвращает именованный путь контекста
     */
    NP getNamedPath();

    /**
     * @return Возвращает контекст документа
     */
    DocumentContext getDocumentContext();
}
