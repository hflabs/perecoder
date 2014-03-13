package ru.hflabs.rcd.backend.console.exports.handlers.dictionary;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.CollectionUtils;
import ru.hflabs.rcd.backend.console.exports.handlers.ExportDescriptor;
import ru.hflabs.rcd.backend.console.exports.handlers.ExportProcessorOverFileTemplate;
import ru.hflabs.rcd.connector.files.FilesConnectorConfiguration;
import ru.hflabs.rcd.exception.ApplicationException;
import ru.hflabs.rcd.model.connector.TransferDictionaryDescriptor;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.model.path.DictionaryNamedPath;
import ru.hflabs.rcd.service.IDocumentConnector;
import ru.hflabs.rcd.service.IManagerService;
import ru.hflabs.util.io.IOUtils;
import ru.hflabs.util.spring.util.ReflectionUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Класс <class>ExportDictionariesOverDirectory</class> реализует процессор экспорта справочника в директорию
 *
 * @author Nazin Alexander
 */
public class ExportDictionariesOverDirectory extends ExportProcessorOverFileTemplate<ExportDictionariesCommand> {

    @Override
    public ExportDescriptor processExport(ExportDictionariesCommand preference, IManagerService managerService) {
        final File directory = preference.retrieveTargetFile();
        IDocumentConnector<FilesConnectorConfiguration, List<File>> documentConnector = retrieveDocumentConnector(preference.getFileType());
        Collection<Group> groups = managerService.dumpGroups(new DictionaryNamedPath(preference.getGroupName(), preference.getDictionaryName()));

        ImmutableList.Builder<Throwable> errorsBuilder = ImmutableList.builder();
        ImmutableMap.Builder<DictionaryNamedPath, File> dictionary2file = ImmutableMap.builder();
        for (Group group : groups) {
            if (!CollectionUtils.isEmpty(group.getDescendants())) {
                LOG.debug("Export group '%s'", group.getName());
                try {
                    FilesConnectorConfiguration connectorPreference = ExportDictionariesCommand.createConnectorPreference(
                            new File(directory, group.getName()).getCanonicalPath(), preference
                    );
                    Collection<Dictionary> dictionaries = group.getDescendants();
                    // Выполняем экспорт контента справочников
                    List<File> files = documentConnector.writeDictionaries(
                            connectorPreference,
                            new TransferDictionaryDescriptor(dictionaries, preference.isMeta(), preference.isHidden())
                    );
                    // Формируем коллекция соответствий справочника к его файлу
                    for (File file : files) {
                        dictionary2file.put(new DictionaryNamedPath(group.getName(), FilenameUtils.getBaseName(file.getCanonicalPath())), file);
                    }
                    LOG.debug("Group '%s' exported", group.getName());
                } catch (Throwable th) {
                    errorsBuilder.add(new ApplicationException(
                            String.format("Can't export dictionaries for group %s. Cause by: %s", group.getName(), th.getMessage()), th)
                    );
                }
            } else {
                LOG.debug("Group '%s' empty", group.getName());
            }
        }
        // Формируем дескриптор экспорта
        ExportDescriptor result = new ExportDescriptor(errorsBuilder.build(), directory, dictionary2file.build());
        try {
            result.setPathToArchive(
                    preference.isCompress() ?
                            IOUtils.compressDirectory(directory, new File(directory.getParent(), FilenameUtils.getBaseName(preference.getTargetPath()) + ".zip")) :
                            null
            );
        } catch (IOException ex) {
            ReflectionUtil.rethrowRuntimeException(ex);
        }
        return result;
    }
}
