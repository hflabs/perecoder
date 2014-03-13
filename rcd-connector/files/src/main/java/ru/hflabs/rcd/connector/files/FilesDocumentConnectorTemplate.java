package ru.hflabs.rcd.connector.files;

import org.dbunit.dataset.CachedDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.supercsv.prefs.CsvPreference;
import ru.hflabs.rcd.connector.files.converter.DictionariesConverter;
import ru.hflabs.rcd.connector.files.converter.RecodeRulesConverter;
import ru.hflabs.rcd.connector.files.dataset.csv.CsvConsumer;
import ru.hflabs.rcd.connector.files.dataset.csv.CsvProducer;
import ru.hflabs.rcd.connector.files.dataset.xml.XmlConsumer;
import ru.hflabs.rcd.connector.files.dataset.xml.XmlProducer;
import ru.hflabs.rcd.exception.constraint.ConstraintException;
import ru.hflabs.rcd.model.connector.TransferDescriptor;
import ru.hflabs.rcd.model.connector.TransferDictionaryDescriptor;
import ru.hflabs.rcd.model.connector.TransferRuleDescriptor;
import ru.hflabs.rcd.service.IDocumentConnector;
import ru.hflabs.util.io.IOUtils;
import ru.hflabs.util.spring.Assert;
import ru.hflabs.util.spring.core.convert.converter.ReverseConverter;
import ru.hflabs.util.spring.util.ReflectionUtil;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

/**
 * Класс <class>FilesDocumentConnector</class> реализует сервис доступа к документам на основе файлов
 *
 * @see IDataSet
 */
public abstract class FilesDocumentConnectorTemplate implements IDocumentConnector<FilesConnectorConfiguration, List<File>> {

    /** Сервис преобразования DataSet-а в коллекцию справочников */
    private final ReverseConverter<IDataSet, TransferDictionaryDescriptor> dictionariesConverter;
    /** Сервис преобразования DataSet-а в коллекцию правил перекодирования */
    private final ReverseConverter<IDataSet, TransferRuleDescriptor> recodeRulesConverter;

    protected FilesDocumentConnectorTemplate() {
        this.dictionariesConverter = new DictionariesConverter();
        this.recodeRulesConverter = new RecodeRulesConverter();
    }

    /**
     * Создает {@link IDataSet} на основе файла
     *
     * @param preference настройки чтения
     * @return Возвращает созданный {@link IDataSet}
     */
    protected abstract IDataSet readDataSet(FilesConnectorConfiguration preference) throws DataSetException;

    /**
     * Сохраняет {@link IDataSet} в указанную директорию
     *
     * @param preference настройки записи
     * @param dataSet сохраняемый {@link IDataSet}
     */
    protected abstract List<File> writeDataSet(FilesConnectorConfiguration preference, IDataSet dataSet) throws DataSetException;

    /**
     * Выполняет формирование и проверку целевого файла чтения
     *
     * @param pathToFile путь к файлу
     * @return Возвращает файл
     */
    protected File createSource(String pathToFile) {
        final File file = new File(pathToFile);
        Assert.isTrue(file.exists() && file.isFile(), String.format("File '%s' not exist", pathToFile), ConstraintException.class);
        Assert.isTrue(file.canRead(), String.format("'%s' must have read permissions", pathToFile), ConstraintException.class);
        return file;
    }

    /**
     * Выполняет формирование и проверку целевой директории записи
     *
     * @param pathToDirectory путь к директории
     * @return Возвращает директорию
     */
    protected File createDestination(String pathToDirectory) {
        final File file = new File(pathToDirectory);
        Assert.isTrue(file.exists() && file.isDirectory() || !file.exists() && file.mkdirs(), String.format("Directory '%s' not exist", pathToDirectory), ConstraintException.class);
        Assert.isTrue(file.canWrite(), String.format("'%s' must have write permissions", pathToDirectory), ConstraintException.class);
        return file;
    }

    /**
     * Выполняет конвертацию {@link IDataSet}-а в объект
     *
     * @param converter сервис конвертации
     * @param settings настройки
     * @return Возвращает результирующий объект конвертации
     */
    private <D> D doConvert(ReverseConverter<IDataSet, D> converter, FilesConnectorConfiguration settings) {
        try {
            return converter.convert(readDataSet(settings));
        } catch (DataSetException ex) {
            ReflectionUtil.rethrowRuntimeException(ex);
            return null;
        }
    }

    @Override
    public TransferDictionaryDescriptor readDictionaries(FilesConnectorConfiguration settings) {
        return doConvert(dictionariesConverter, settings);
    }

    @Override
    public TransferRuleDescriptor readRecodeRules(FilesConnectorConfiguration settings) {
        return doConvert(recodeRulesConverter, settings);
    }

    /**
     * Выполняет конвертацию объекта в {@link IDataSet} и сохраняет его
     *
     * @param converter сервис конвертации
     */
    private <TD extends TransferDescriptor<?>> List<File> doConvert(ReverseConverter<IDataSet, TD> converter, FilesConnectorConfiguration settings, TD descriptor) {
        try {
            return writeDataSet(settings, converter.reverseConvert(descriptor));
        } catch (DataSetException ex) {
            ReflectionUtil.rethrowRuntimeException(ex);
        }
        return Collections.emptyList();
    }

    @Override
    public List<File> writeDictionaries(FilesConnectorConfiguration settings, TransferDictionaryDescriptor descriptor) {
        return doConvert(dictionariesConverter, settings, descriptor);
    }

    @Override
    public List<File> writeRecodeRules(FilesConnectorConfiguration settings, TransferRuleDescriptor descriptor) {
        return doConvert(recodeRulesConverter, settings, descriptor);
    }

    /**
     * Класс <class>CsvDocumentConnector</class> реализует формирование документов на основе директории с CSV файлами
     *
     * @see org.dbunit.dataset.csv.CsvDataSet
     */
    public static final class CsvDocumentConnector extends FilesDocumentConnectorTemplate {

        @Override
        protected IDataSet readDataSet(FilesConnectorConfiguration settings) throws DataSetException {
            return new CachedDataSet(
                    new CsvProducer(
                            Charset.forName(settings.getEncoding()),
                            new CsvPreference.Builder(settings.getQuote(), settings.getDelimiter(), IOUtils.LINE_SEPARATOR).build(),
                            createSource(settings.getPathToFile())
                    )
            );
        }

        @Override
        protected List<File> writeDataSet(FilesConnectorConfiguration settings, IDataSet dataSet) throws DataSetException {
            return CsvConsumer.write(
                    dataSet,
                    Charset.forName(settings.getEncoding()),
                    new CsvPreference.Builder(settings.getQuote(), settings.getDelimiter(), IOUtils.LINE_SEPARATOR).build(),
                    createDestination(settings.getPathToFile())
            );
        }
    }

    /**
     * Класс <class>XmlDocumentConnector</class> реализует формирование документов на основе директории с Xml файлами
     *
     * @see org.dbunit.dataset.xml.XmlDataSet
     */
    public static final class XmlDocumentConnector extends FilesDocumentConnectorTemplate {

        @Override
        protected IDataSet readDataSet(FilesConnectorConfiguration settings) throws DataSetException {
            return new CachedDataSet(
                    new XmlProducer(
                            createSource(settings.getPathToFile())
                    )
            );
        }

        @Override
        protected List<File> writeDataSet(FilesConnectorConfiguration settings, IDataSet dataSet) throws DataSetException {
            return XmlConsumer.write(
                    dataSet,
                    createDestination(settings.getPathToFile())
            );
        }
    }
}
