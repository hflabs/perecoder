package ru.hflabs.rcd.connector.files.dataset.csv;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;
import ru.hflabs.rcd.connector.files.FilesByExtensionFilter;
import ru.hflabs.rcd.connector.files.FilesComparator;
import ru.hflabs.rcd.connector.files.dataset.FilesProducer;
import ru.hflabs.util.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;

/**
 * Класс <class>CsvProducer</class> реализует сервис формирования {@link org.dbunit.dataset.IDataSet таблиц} на основе CSV файла
 *
 * @author Nazin Alexander
 */
public class CsvProducer extends FilesProducer {

    /** Кодировка файла */
    private final Charset encoding;
    /** Настройки чтения файлов */
    private final CsvPreference preference;

    public CsvProducer(Charset encoding, CsvPreference preference, File targetFile) {
        this(encoding, preference, targetFile, new FilesComparator.ByName());
    }

    public CsvProducer(Charset encoding, CsvPreference preference, File targetFile, Comparator<File> fileComparator) {
        super(FilesByExtensionFilter.CSV_FILTER, fileComparator, targetFile);
        this.encoding = encoding;
        this.preference = preference;

    }

    /**
     * Выполняет формирование таблицы из файла
     *
     * @param tableName название таблицы
     * @param reader сервис чтения файла
     */
    protected void readTable(String tableName, ICsvListReader reader) throws DataSetException, IOException {
        String[] headers = reader.getHeader(true);
        if (headers == null) {
            throw new DataSetException(String.format("Table '%s' must contains headers", tableName));
        }
        Column[] columns = new Column[headers.length];
        for (int i = 0; i < headers.length; i++) {
            columns[i] = new Column(headers[i], DataType.UNKNOWN);
        }

        consumer.startTable(new DefaultTableMetaData(tableName, columns));
        List<String> row;
        while ((row = reader.read()) != null) {
            consumer.row(row.toArray());
        }
        consumer.endTable();
    }

    @Override
    protected void readTable(String tableName, File file) throws DataSetException, IOException {
        ICsvListReader reader = new CsvListReader(
                new BufferedReader(Channels.newReader(new FileInputStream(file).getChannel(), encoding.name())),
                preference
        );
        try {
            readTable(tableName, reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }
}
