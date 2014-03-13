package ru.hflabs.rcd.connector.files;

import com.google.common.collect.ImmutableMap;
import ru.hflabs.rcd.exception.search.UnknownConnectorException;
import ru.hflabs.rcd.service.IDocumentConnector;
import ru.hflabs.rcd.service.IServiceFactory;
import ru.hflabs.util.spring.Assert;

import java.io.File;
import java.util.List;
import java.util.Map;

import static ru.hflabs.rcd.connector.files.FilesByExtensionFilter.CSV;
import static ru.hflabs.rcd.connector.files.FilesByExtensionFilter.XML;

/**
 * Класс <class>DocumentConnectorFactory</class> реализует фабрику сервисов формирования документов на основе файлов
 *
 * @author Nazin Alexander
 */
public class FilesDocumentConnectorFactory implements IServiceFactory<IDocumentConnector<FilesConnectorConfiguration, List<File>>, String> {

    /** Статический экземпляр фабрики */
    private static FilesDocumentConnectorFactory instance;
    /** Кеш сервисов */
    private final Map<String, IDocumentConnector<FilesConnectorConfiguration, List<File>>> services;

    private FilesDocumentConnectorFactory() {
        this.services = ImmutableMap.<String, IDocumentConnector<FilesConnectorConfiguration, List<File>>>of(
                CSV, new FilesDocumentConnectorTemplate.CsvDocumentConnector(),
                XML, new FilesDocumentConnectorTemplate.XmlDocumentConnector()
        );
    }

    /**
     * Возвращает экземпляр фабрики
     *
     * @return Возвращает экземпляр фабрики
     */
    public static synchronized FilesDocumentConnectorFactory getInstance() {
        if (instance == null) {
            instance = new FilesDocumentConnectorFactory();
        }
        return instance;
    }

    @Override
    public IDocumentConnector<FilesConnectorConfiguration, List<File>> retrieveService(String key) {
        IDocumentConnector<FilesConnectorConfiguration, List<File>> service = services.get(key);
        Assert.notNull(service, String.format("Document connector for type '%s' not found", key), UnknownConnectorException.class);
        return service;
    }

    @Override
    public void destroyService(String key, IDocumentConnector<FilesConnectorConfiguration, List<File>> service) {
        // do nothing
    }
}
