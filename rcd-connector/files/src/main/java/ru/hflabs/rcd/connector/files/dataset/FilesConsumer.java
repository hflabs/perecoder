package ru.hflabs.rcd.connector.files.dataset;

import com.google.common.collect.Lists;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.stream.DataSetProducerAdapter;
import org.dbunit.dataset.stream.IDataSetConsumer;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Класс <class>FilesConsumer</class> реализует формирование файлов на основе {@link org.dbunit.dataset.IDataSet таблиц}
 *
 * @param <W> сервис записи
 * @author Nazin Alexander
 */
public abstract class FilesConsumer<W> implements IDataSetConsumer {

    /** Целевая директория */
    protected final File targetDirectory;
    /** Текущий сервис записи */
    protected String[] currentHeaders;
    /** Текущий сервис записи */
    protected W currentWriter;
    /** Текущий файл */
    protected File currentFile;
    /** Коллекция целевых файлов */
    protected final List<File> files;
    /** Расширение файлов */
    private final String extension;

    public FilesConsumer(File targetDirectory, String extension) {
        this.targetDirectory = targetDirectory;
        this.extension = extension;
        this.currentFile = null;
        this.files = Lists.newArrayList();
    }

    public List<File> getFiles() {
        return files;
    }

    /**
     * Создает и возвращает текущий сервис записи
     *
     * @return Возвращает созданный сервис записи
     */
    protected abstract W createWriter() throws IOException;

    /**
     * Выполняет закрытие сервиса записи
     *
     * @param writer текущий сервис записи
     */
    protected abstract void closeWriter(W writer);

    /**
     * Выполняет запись колонок
     */
    protected abstract void writeHeader() throws IOException;

    @Override
    public void startTable(ITableMetaData metaData) throws DataSetException {
        // Формируем целевой файл
        currentFile = new File(targetDirectory, String.format("%s.%s", metaData.getTableName(), extension));
        try {
            // Формируем сервис записи файла
            currentWriter = createWriter();
            // Формируем название колонок
            Column[] columns = metaData.getColumns();
            currentHeaders = new String[columns.length];
            for (int i = 0; i < currentHeaders.length; i++) {
                currentHeaders[i] = columns[i].getColumnName();
            }
            // Записываем название колонок
            writeHeader();
        } catch (IOException ex) {
            throw new DataSetException(ex);
        }
    }

    @Override
    public void endTable() throws DataSetException {
        currentHeaders = null;
        closeWriter(currentWriter);
        currentWriter = null;
        files.add(currentFile);
        currentFile = null;
    }

    @Override
    public void startDataSet() throws DataSetException {
        if (targetDirectory.exists()) {
            if (targetDirectory.isFile()) {
                throw new DataSetException(String.format("Can't store data set to existed file '%s'", targetDirectory.getPath()));
            }
        } else if (!targetDirectory.mkdirs()) {
            throw new DataSetException("Can't create destination directory '" + targetDirectory.getPath() + "'");
        }
    }

    @Override
    public void endDataSet() throws DataSetException {
        // do nothing
    }

    public List<File> write(IDataSet dataSet) throws DataSetException {
        DataSetProducerAdapter provider = new DataSetProducerAdapter(dataSet);
        provider.setConsumer(this);
        provider.produce();
        return files;
    }
}
