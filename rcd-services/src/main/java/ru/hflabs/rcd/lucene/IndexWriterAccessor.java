package ru.hflabs.rcd.lucene;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;

import java.io.IOException;

/**
 * Интерфейс <class>IndexWriterAccessor</class> декларирует методы доступа к сервису записи в индекс
 *
 * @author Nazin Alexander
 */
public interface IndexWriterAccessor {

    /**
     * @param directory директория индекса
     * @return Возвращает сервис записи в индекс
     */
    IndexWriter retrieveWriter(Directory directory) throws IOException;

    /**
     * Выполняет фиксирование изменений в сервисе записи
     *
     * @param writer сервис записи
     * @param changeCount количество изменений
     */
    void commitWriter(IndexWriter writer, int changeCount) throws IOException;

    /**
     * Выполняет откат изменений в сервисе записи
     *
     * @param writer сервис записи
     */
    void rollbackWriter(IndexWriter writer) throws IOException;
}
