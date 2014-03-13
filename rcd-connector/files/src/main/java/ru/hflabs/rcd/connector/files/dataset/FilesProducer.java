package ru.hflabs.rcd.connector.files.dataset;

import com.google.common.collect.Lists;
import org.apache.commons.io.FilenameUtils;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.stream.DefaultConsumer;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.dbunit.dataset.stream.IDataSetProducer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Класс <class>FilesProducer</class> реализует сервис формирования {@link org.dbunit.dataset.IDataSet таблиц} на основе файлов
 *
 * @author Nazin Alexander
 */
public abstract class FilesProducer implements IDataSetProducer {

    /** Сервис управления таблицами по умолчанию */
    public static final IDataSetConsumer EMPTY_CONSUMER = new DefaultConsumer();

    /** Целевой файл */
    protected final File targetFile;
    /** Сервис сравнения файлов */
    protected final Comparator<File> fileComparator;
    /** Сервис фильтрации файлов */
    protected final FileFilter fileFilter;
    /** Флаг необходимости приводить название таблицы в UPPER-case */
    protected boolean upperCaseTableName;
    /** Сервис управления таблицами */
    protected IDataSetConsumer consumer;

    public FilesProducer(FileFilter fileFilter, Comparator<File> fileComparator, File targetFile) {
        this.consumer = EMPTY_CONSUMER;
        this.fileComparator = fileComparator;
        this.upperCaseTableName = false;
        this.fileFilter = fileFilter;
        this.targetFile = targetFile;
    }

    @Override
    public void setConsumer(IDataSetConsumer consumer) throws DataSetException {
        this.consumer = consumer;
    }

    public void setUpperCaseTableName(boolean upperCaseTableName) {
        this.upperCaseTableName = upperCaseTableName;
    }

    /**
     * Выполняет формирование таблицы из файла
     *
     * @param file целевой файл
     */
    private void readTable(File file) throws DataSetException, IOException {
        String tableName = FilenameUtils.getBaseName(file.getPath());
        if (upperCaseTableName) {
            tableName = tableName.toUpperCase();
        }
        readTable(tableName, file);
    }

    /**
     * Выполняет чтение таблицы из файла
     *
     * @param tableName название таблицы
     * @param file целевой файл
     */
    protected abstract void readTable(String tableName, File file) throws DataSetException, IOException;

    @Override
    public void produce() throws DataSetException {
        consumer.startDataSet();
        try {
            if (targetFile.isDirectory()) {
                List<File> files = Lists.newArrayList(targetFile.listFiles(fileFilter));
                Collections.sort(files, fileComparator);
                for (File file : files) {
                    readTable(file);
                }
            } else {
                if (fileFilter.accept(targetFile)) {
                    readTable(targetFile);
                }
            }
            consumer.endDataSet();
        } catch (IOException ex) {
            throw new DataSetException(ex);
        }
    }
}
