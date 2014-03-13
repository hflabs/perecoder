package ru.hflabs.rcd.lucene;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.ReaderManager;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.hflabs.util.io.IOUtils;
import ru.hflabs.util.lucene.LuceneModifierCallback;
import ru.hflabs.util.lucene.LuceneModifierUtil;
import ru.hflabs.util.lucene.LuceneUtil;

import java.io.IOException;

/**
 * Класс <class>LuceneIndexManagerTemplate</class> шаблон работы с поисковым индексом
 *
 * @author Nazin Alexander
 */
public class LuceneIndexManagerTemplate implements NamedIndexManager {

    /** Фабрика директорий */
    private LuceneDirectoryFactory luceneDirectoryFactory;
    /** Директория индекса */
    private Directory directory;
    /** Сервис доступа к индексу записи */
    private IndexWriterAccessor writerAccessor;

    public void setLuceneDirectoryFactory(LuceneDirectoryFactory luceneDirectoryFactory) {
        this.luceneDirectoryFactory = luceneDirectoryFactory;
    }

    public void setWriterAccessor(IndexWriterAccessor writerAccessor) {
        this.writerAccessor = writerAccessor;
    }

    /** Проверяет корректность инициализации и текущее состояние индекса */
    protected void checkConfiguration() {
        Assert.notNull(directory, "Index directory not properly initialized");
    }

    @Override
    public String retrieveIndexName() {
        checkConfiguration();
        return directory.toString();
    }

    @Override
    public SearcherManager createSearcherManager() {
        checkConfiguration();
        try {
            return new SearcherManager(directory, null);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    @Override
    public ReaderManager createReaderManager() {
        checkConfiguration();
        try {
            return new ReaderManager(directory);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public IndexWriter retrieveWriter() {
        checkConfiguration();
        try {
            return writerAccessor.retrieveWriter(directory);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public void commitWriter(IndexWriter writer, int changeCount) {
        try {
            writerAccessor.commitWriter(writer, changeCount);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public void rollbackWriter(IndexWriter writer) {
        try {
            writerAccessor.rollbackWriter(writer);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    @Override
    public void open(String name) throws Exception {
        directory = luceneDirectoryFactory.retrieveDirectory(name);
        open();
    }

    @Override
    public Directory retrieveDirectory() {
        checkConfiguration();
        return directory;
    }

    @Override
    public void open() throws Exception {
        checkConfiguration();
        // Если индекс не существует, то выполняем его создание для корректной инициализации сервиса чтения
        if (!DirectoryReader.indexExists(directory)) {
            LuceneModifierUtil.doWithCallback("initialize", this, new LuceneModifierCallback() {
                @Override
                public int process(IndexWriter writer) throws Exception {
                    writer.commit();
                    return -1;
                }
            });
        }
        LuceneUtil.unlockIndexDirectory(directory);
    }

    @Override
    public void close() throws Exception {
        IOUtils.closeQuietly(directory);
        directory = null;
    }
}
