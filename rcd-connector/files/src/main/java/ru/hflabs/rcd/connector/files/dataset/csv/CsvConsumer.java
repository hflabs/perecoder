package ru.hflabs.rcd.connector.files.dataset.csv;

import org.apache.commons.io.IOUtils;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;
import ru.hflabs.rcd.connector.files.FilesByExtensionFilter;
import ru.hflabs.rcd.connector.files.dataset.FilesConsumer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Класс <class>CsvConsumer</class> реализует формирование CSV файлов на основе {@link org.dbunit.dataset.IDataSet таблиц}
 *
 * @author Nazin Alexander
 */
public class CsvConsumer extends FilesConsumer<ICsvListWriter> {

    /** Кодировка файла */
    private final Charset encoding;
    /** Настройки чтения файлов */
    private final CsvPreference preference;

    public CsvConsumer(Charset encoding, CsvPreference preference, File targetDirectory) {
        super(targetDirectory, FilesByExtensionFilter.CSV);
        this.encoding = encoding;
        this.preference = preference;
    }

    @Override
    protected ICsvListWriter createWriter() throws IOException {
        return new CsvListWriter(
                Channels.newWriter(new FileOutputStream(currentFile).getChannel(), encoding.name()),
                preference
        );
    }

    @Override
    protected void writeHeader() throws IOException {
        currentWriter.writeHeader(currentHeaders);
    }

    @Override
    protected void closeWriter(ICsvListWriter writer) {
        IOUtils.closeQuietly(writer);
    }

    @Override
    public void row(Object[] values) throws DataSetException {
        try {
            currentWriter.write(values);
        } catch (IOException ex) {
            throw new DataSetException(ex);
        }
    }

    public static List<File> write(IDataSet dataSet, Charset encoding, CsvPreference preference, File directory) throws DataSetException {
        return new CsvConsumer(encoding, preference, directory).write(dataSet);
    }

    public static List<File> write(ITable table, Charset encoding, CsvPreference preference, File directory) throws DataSetException {
        return write(new DefaultDataSet(table), encoding, preference, directory);
    }
}
