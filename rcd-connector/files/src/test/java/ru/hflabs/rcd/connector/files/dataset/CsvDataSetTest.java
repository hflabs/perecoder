package ru.hflabs.rcd.connector.files.dataset;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.dbunit.dataset.CachedDataSet;
import org.dbunit.dataset.IDataSet;
import org.supercsv.prefs.CsvPreference;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.hflabs.rcd.connector.files.dataset.csv.CsvConsumer;
import ru.hflabs.rcd.connector.files.dataset.csv.CsvProducer;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class CsvDataSetTest extends DataSetTest {

    /** Названия тестируемых файлов */
    private static final String EXCEL_FILE = "dict.xls.csv";
    private static final String UNIX_FILE = "dict.wb.esc.csv";
    private static final String QUOTE_FILE = "dict.sqld.csv";

    @AfterClass
    private void deleteWorkingDirectory() throws Exception {
        FileUtils.deleteQuietly(workingDirectory);
    }

    @BeforeMethod
    private void clearWorkingDirectory() throws Exception {
        FileUtils.cleanDirectory(workingDirectory);
    }

    @DataProvider
    private Iterator<Object[]> createTestCases() throws Exception {
        // Получаем URL корневой директории
        final URL rootDirectoryUrl = getClass().getClassLoader().getResource(".");
        // Формируем параметры
        return Lists.newArrayList(
                new Object[]{new File(rootDirectoryUrl.getFile(), EXCEL_FILE), Charset.forName("CP1251"), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE},
                new Object[]{new File(rootDirectoryUrl.getFile(), UNIX_FILE), Charsets.UTF_8, CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE},
                new Object[]{new File(rootDirectoryUrl.getFile(), QUOTE_FILE), Charsets.UTF_8, CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE}
        ).iterator();
    }

    @Test(dataProvider = "createTestCases")
    public void testDataSet(File file, Charset encoding, CsvPreference preference) throws Exception {
        // Выполняем чтение
        IDataSet dataSet = new CachedDataSet(new CsvProducer(encoding, preference, file));
        // Проверяем таблицу
        String[] tableNames = dataSet.getTableNames();
        assertEquals(tableNames.length, 1);
        assertEquals(tableNames[0], FilenameUtils.getBaseName(file.getCanonicalPath()));
        assertTable(dataSet.getTable(tableNames[0]));
        // Выполняем запись
        List<File> files = CsvConsumer.write(dataSet, encoding, preference, workingDirectory);
        // Проверяем файл
        assertEquals(files.size(), 1);
        assertEquals(files.get(0).getName(), file.getName());
    }
}
