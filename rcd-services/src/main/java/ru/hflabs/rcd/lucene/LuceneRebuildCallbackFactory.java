package ru.hflabs.rcd.lucene;

import org.springframework.core.Ordered;
import ru.hflabs.rcd.service.ISingleClassObserver;

/**
 * Класс <class>RebuildLuceneCallbackFactory</class> декларирует методы фабрики доступа к процессору полного перестроения индекса
 *
 * @author Nazin Alexander
 */
public interface LuceneRebuildCallbackFactory<E> extends ISingleClassObserver<E>, Ordered {

    /**
     * @return Возвращает текущее количество документов в индексе
     */
    int totalDocumentCount();

    /**
     * @return Возвращает <code>TRUE</code>, если индекс поврежден
     */
    boolean isCorrupted();

    /**
     * Выполняет полное перестроение индекса
     *
     * @return Возвращает количество документов в индексе
     */
    int executeRebuild();
}
