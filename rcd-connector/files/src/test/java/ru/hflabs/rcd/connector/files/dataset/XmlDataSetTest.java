package ru.hflabs.rcd.connector.files.dataset;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.dbunit.dataset.CachedDataSet;
import org.dbunit.dataset.IDataSet;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import ru.hflabs.rcd.connector.files.dataset.xml.XmlConsumer;
import ru.hflabs.rcd.connector.files.dataset.xml.XmlProducer;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class XmlDataSetTest extends DataSetTest {

    /** Названия тестируемых файлов */
    private static final String XML_FILE = "dict.xml";

    @AfterClass
    private void deleteWorkingDirectory() throws Exception {
        FileUtils.deleteQuietly(workingDirectory);
    }

    @Test
    public void testDataSet() throws Exception {
        // Получаем URL корневой директории
        final URL rootDirectoryUrl = getClass().getClassLoader().getResource(".");
        final File targetFile = new File(rootDirectoryUrl.getFile(), XML_FILE);
        // Выполняем чтение
        IDataSet dataSet = new CachedDataSet(new XmlProducer(targetFile));

        // Проверяем таблицу
        String[] tableNames = dataSet.getTableNames();
        assertEquals(tableNames.length, 1);
        assertEquals(tableNames[0], FilenameUtils.getBaseName(targetFile.getCanonicalPath()));
        assertTable(dataSet.getTable(tableNames[0]));
        // Выполняем запись
        List<File> files = XmlConsumer.write(dataSet, workingDirectory);
        // Проверяем файл
        assertEquals(files.size(), 1);
        assertEquals(files.get(0).getName(), targetFile.getName());
    }
}
