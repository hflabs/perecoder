package ru.hflabs.rcd.lucene.transaction;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ReflectionUtils;
import ru.hflabs.rcd.lucene.IndexWriterAccessor;
import ru.hflabs.util.io.IOUtils;

import java.io.IOException;

/**
 * Класс <class>TransactionalIndexWriterAccessor</class> реализует сервис доступа к транзакционному индексу записи
 *
 * @author Nazin Alexander
 * @see org.springframework.transaction.annotation.Transactional
 */
public class TransactionalIndexWriterAccessor extends TransactionSynchronizationAdapter implements IndexWriterAccessor {

    /** Конфигурация сервис записи */
    private IndexWriterConfig indexWriterConfig;

    public void setIndexWriterConfig(IndexWriterConfig indexWriterConfig) {
        this.indexWriterConfig = indexWriterConfig;
    }

    /**
     * Создает и возвращает сервис записи в индекс
     *
     * @param directory директория индекса
     * @return Возвращает сервис записи в индекс
     */
    private IndexWriter createIndexWriter(Directory directory) throws IOException {
        return new IndexWriter(directory, indexWriterConfig.clone());
    }

    @Override
    public IndexWriter retrieveWriter(Directory directory) throws IOException {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            // Получаем адаптер синхронизации из менеджера
            IndexWriterTransactionSynchronization synchronization = (IndexWriterTransactionSynchronization) TransactionSynchronizationManager.getResource(directory);
            if (synchronization == null) {
                synchronization = new IndexWriterTransactionSynchronization(new IndexWriter(directory, indexWriterConfig.clone()));
                TransactionSynchronizationManager.bindResource(directory, synchronization);
            }
            // Выполняем перерегистрацию адаптера
            TransactionSynchronizationManager.registerSynchronization(synchronization);
            // Возвращаем актуальный сервис записи
            return synchronization.getWriter();
        } else {
            return createIndexWriter(directory);
        }
    }

    @Override
    public void commitWriter(IndexWriter writer, int changeCount) throws IOException {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            try {
                writer.commit();
            } finally {
                IOUtils.closeQuietly(writer);
            }
        }
    }

    @Override
    public void rollbackWriter(IndexWriter writer) throws IOException {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            try {
                writer.rollback();
            } finally {
                IOUtils.closeQuietly(writer);
            }
        }
    }

    /**
     * Класс <class>IndexWriterTransactionSynchronization</class> реализует адаптер синхронизации транзакций с сервисом записи в индекс
     *
     * @author Nazin Alexander
     */
    private class IndexWriterTransactionSynchronization extends TransactionSynchronizationAdapter {

        /** Сервис записи в индекс */
        private IndexWriter writer;

        private IndexWriterTransactionSynchronization(IndexWriter writer) {
            this.writer = writer;
        }

        public IndexWriter getWriter() {
            return writer;
        }

        @Override
        public void beforeCommit(boolean readOnly) {
            if (!readOnly) {
                try {
                    writer.prepareCommit();
                } catch (Exception ex) {
                    ReflectionUtils.rethrowRuntimeException(ex);
                }
            }
        }

        @Override
        public void afterCommit() {
            try {
                writer.commit();
            } catch (Exception ex) {
                ReflectionUtils.rethrowRuntimeException(ex);
            }
        }

        @Override
        public void afterCompletion(int status) {
            try {
                if (status != STATUS_COMMITTED) {
                    writer.rollback();
                } else {
                    writer.close(false);
                }
            } catch (Throwable th) {
                ReflectionUtils.rethrowRuntimeException(th);
            } finally {
                TransactionSynchronizationManager.unbindResource(writer.getDirectory());
                writer = null;
            }
        }
    }
}
