package ru.hflabs.rcd.lucene;

import org.apache.lucene.store.Directory;
import ru.hflabs.util.lucene.LuceneIndexManager;

/**
 * Интерфейс <class>NamedIndexManager</class> декларирует методы работы с сервисом записи в индекс, который привязан к определенной сущности
 *
 * @see LuceneIndexManager
 * @see LuceneDirectoryFactory
 */
public interface NamedIndexManager extends LuceneIndexManager {

    /**
     * Выполняет подготовку и инициализацию используемых ресурсов
     *
     * @param name название индексируемой сущности
     */
    void open(String name) throws Exception;

    /**
     * @return Возвращает директорию индекса
     */
    Directory retrieveDirectory();
}
