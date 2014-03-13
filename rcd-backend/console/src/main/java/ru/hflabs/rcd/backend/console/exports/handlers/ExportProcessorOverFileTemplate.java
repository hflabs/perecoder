package ru.hflabs.rcd.backend.console.exports.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.backend.console.preference.FilePreference;
import ru.hflabs.rcd.connector.files.FilesByExtensionFilter;
import ru.hflabs.rcd.connector.files.FilesConnectorConfiguration;
import ru.hflabs.rcd.connector.files.FilesDocumentConnectorFactory;
import ru.hflabs.rcd.service.IDocumentConnector;
import ru.hflabs.rcd.service.IServiceFactory;

import java.io.File;
import java.util.List;

/**
 * Класс <class>ExportProcessorOverFileTemplate</class> реализует базовый процессор экспорта документов
 *
 * @author Nazin Alexander
 */
public abstract class ExportProcessorOverFileTemplate<P extends FilePreference> implements ExportProcessor<P> {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    /** Фабрика сохранения документов */
    protected final IServiceFactory<IDocumentConnector<FilesConnectorConfiguration, List<File>>, String> documentConnectorFactory;

    public ExportProcessorOverFileTemplate() {
        this.documentConnectorFactory = FilesDocumentConnectorFactory.getInstance();
    }

    /**
     * Возвращает сервис создания документов
     *
     * @param fileType тип файла
     * @return Возвращает сервис создания документов
     */
    protected IDocumentConnector<FilesConnectorConfiguration, List<File>> retrieveDocumentConnector(String fileType) {
        String targetFileType = StringUtils.hasText(fileType) ?
                fileType :
                FilesByExtensionFilter.CSV;
        return documentConnectorFactory.retrieveService(targetFileType);
    }
}
