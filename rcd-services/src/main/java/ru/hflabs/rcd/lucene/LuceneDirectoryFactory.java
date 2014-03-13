package ru.hflabs.rcd.lucene;

import org.apache.lucene.store.Directory;

import java.io.IOException;

/**
 * Интерфейс <class>LuceneDirectoryFactory</class> декларирует методы получения директории индекса
 *
 * @author Nazin Alexander
 */
public interface LuceneDirectoryFactory {

    /**
     * Возвращает директорию хранения индекса для сущности
     *
     * @param name название сущности индекса
     * @return Возвращает директорию хранения
     */
    Directory retrieveDirectory(String name) throws IOException;
}
