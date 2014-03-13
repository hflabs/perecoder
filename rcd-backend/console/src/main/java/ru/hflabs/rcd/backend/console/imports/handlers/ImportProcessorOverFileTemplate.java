package ru.hflabs.rcd.backend.console.imports.handlers;

import com.beust.jcommander.ParameterException;
import com.google.common.collect.Lists;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.backend.console.preference.FilePreference;
import ru.hflabs.rcd.connector.files.FilesByExtensionFilter;
import ru.hflabs.rcd.connector.files.FilesConnectorConfiguration;
import ru.hflabs.rcd.connector.files.FilesDocumentConnectorFactory;
import ru.hflabs.rcd.exception.ApplicationException;
import ru.hflabs.rcd.service.IDocumentConnector;
import ru.hflabs.rcd.service.IManagerService;
import ru.hflabs.rcd.service.IServiceFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.List;

/**
 * Класс <class>ImportProcessorOverFileTemplate</class> реализует базовый процессор импорта документов
 *
 * @author Nazin Alexander
 */
public abstract class ImportProcessorOverFileTemplate<P extends FilePreference, T> implements ImportProcessor<P, T> {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    /** Сервис фильтрации файлов в директории */
    protected static final FileFilter DEFAULT_FILE_FILTER = FilesByExtensionFilter.CSV_FILTER;
    /** Фабрика создания документов */
    protected final IServiceFactory<IDocumentConnector<FilesConnectorConfiguration, List<File>>, String> documentConnectorFactory;

    public ImportProcessorOverFileTemplate() {
        this.documentConnectorFactory = FilesDocumentConnectorFactory.getInstance();
    }

    /**
     * Создает и возвращает фильтр файлов по их типу
     *
     * @param fileExtension расширение файлов
     * @return Возвращает фильтр файлов
     */
    protected static FileFilter createFileFilter(String fileExtension) {
        return StringUtils.hasText(fileExtension) ?
                new FilesByExtensionFilter(fileExtension) :
                DEFAULT_FILE_FILTER;
    }

    /**
     * Возвращает сервис создания документов
     *
     * @param targetFile целевой файл
     * @param fileType тип файла
     * @return Возвращает сервис создания документов
     */
    protected IDocumentConnector<FilesConnectorConfiguration, List<File>> retrieveDocumentConnector(String targetFile, String fileType) {
        String targetFileType = StringUtils.hasText(fileType) ?
                fileType :
                FilenameUtils.getExtension(targetFile);
        return documentConnectorFactory.retrieveService(targetFileType.toLowerCase());
    }

    /**
     * Создает настройки чтения файла
     *
     * @param preference настройки импорта
     * @return Возвращает созданные настройки чтения файла
     */
    protected FilesConnectorConfiguration createConnectorPreference(P preference) {
        return FilePreference.createConnectorPreference(preference.getTargetPath(), preference);
    }

    /**
     * Выполняет конвертацию
     *
     * @param preference настройки импорта
     * @param managerService сервис управления документами
     * @return Возвращает сформированную коллекцию документов
     */
    protected abstract Collection<T> doConvert(P preference, IManagerService managerService) throws Exception;

    /**
     * Формирует исключение о невозможности обработки файла
     *
     * @param pathToFile путь к целевому файлу
     * @param th исключение
     * @return Возвращает сформированное исключение
     */
    protected static ApplicationException createThrowable(String pathToFile, Throwable th) {
        return new ApplicationException(String.format("Can't process '%s'. Cause by: %s", pathToFile, th.getMessage()), th.getCause());
    }

    @Override
    public ImportDescriptor<T> processImport(P preference, IManagerService managerService) {
        final ImportDescriptor<T> result = new ImportDescriptor<T>();
        try {
            result.setDocuments(doConvert(preference, managerService));
        } catch (ParameterException ex) {
            result.setErrors(Lists.<Throwable>newArrayList(ex));
        } catch (Throwable th) {
            result.setErrors(Lists.<Throwable>newArrayList(createThrowable(preference.getTargetPath(), th)));
        }
        return result;
    }
}
