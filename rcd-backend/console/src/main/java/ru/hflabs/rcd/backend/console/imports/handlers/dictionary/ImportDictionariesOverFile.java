package ru.hflabs.rcd.backend.console.imports.handlers.dictionary;

import com.beust.jcommander.ParameterException;
import com.google.common.collect.Lists;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.backend.console.imports.handlers.ImportProcessorOverFileTemplate;
import ru.hflabs.rcd.connector.files.FilesConnectorConfiguration;
import ru.hflabs.rcd.exception.constraint.IllegalNameException;
import ru.hflabs.rcd.model.connector.TransferDictionaryDescriptor;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.service.IManagerService;
import ru.hflabs.rcd.service.ServiceUtils;
import ru.hflabs.util.spring.Assert;

import java.util.Collection;

import static ru.hflabs.rcd.accessor.Accessors.injectTrimmedName;

/**
 * Класс <class>ImportDictionariesOverFile</class> реализует процессор импорта справочника из файла
 *
 * @author Nazin Alexander
 */
public class ImportDictionariesOverFile extends ImportProcessorOverFileTemplate<ImportDictionariesCommand, Group> {

    /**
     * Выполняет формирование справочника
     *
     * @param dictionaryName название справочника
     * @param targetFile целевой файл
     * @param fileType тип файла
     * @param connectorPreference настройки чтения файла
     * @return Возвращает коллекцию сформированных справочников
     */
    protected Dictionary createDictionary(String dictionaryName, String targetFile, String fileType, FilesConnectorConfiguration connectorPreference) {
        Assert.isTrue(StringUtils.hasText(dictionaryName), "Dictionary name must not be empty", IllegalNameException.class);

        TransferDictionaryDescriptor descriptor = retrieveDocumentConnector(targetFile, fileType).readDictionaries(connectorPreference);
        Dictionary dictionary = (!descriptor.isEmpty()) ?
                ServiceUtils.extractSingleDocument(descriptor.getContent()) :
                new Dictionary();
        dictionary = injectTrimmedName(dictionary, dictionaryName);

        return dictionary;
    }

    @Override
    protected Collection<Group> doConvert(ImportDictionariesCommand preference, IManagerService managerService) throws Exception {
        Assert.isTrue(StringUtils.hasText(preference.getGroupName()), "Missing group name parameter", ParameterException.class);

        Dictionary dictionary = createDictionary(
                StringUtils.hasText(preference.getDictionaryName()) ? preference.getDictionaryName() : FilenameUtils.getBaseName(preference.getTargetPath()),
                preference.getTargetPath(),
                preference.getFileType(),
                createConnectorPreference(preference)
        );
        dictionary.setDescription(preference.getDictionaryDescription());

        Group group = new Group();
        group.setName(preference.getGroupName());
        group.setDescendants(Lists.newArrayList(dictionary));

        return managerService.storeGroups(Lists.newArrayList(group));
    }
}
