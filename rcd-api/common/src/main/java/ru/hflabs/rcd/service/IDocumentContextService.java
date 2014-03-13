package ru.hflabs.rcd.service;

import ru.hflabs.rcd.model.document.DocumentContext;

import java.util.Collection;
import java.util.Set;

/**
 * Интерфейс <class>IDocumentContextService</class> декларирует методы для получения контекста документа
 *
 * @see DocumentContext
 */
public interface IDocumentContextService<NP> {

    /**
     * @param namedPath коллекция именованных путей пути
     * @return Возвращает коллекцию контекстов именнованных значений
     */
    Collection<DocumentContext> findDocumentContexts(Set<NP> namedPath);
}
