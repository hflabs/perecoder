package ru.hflabs.rcd.backend.console.imports.handlers.dictionary;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.backend.console.imports.handlers.ImportDescriptor;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.service.IManagerService;

import java.io.File;
import java.util.Collection;

import static ru.hflabs.rcd.accessor.Accessors.injectTrimmedName;
import static ru.hflabs.rcd.accessor.Accessors.linkDescendants;

/**
 * Класс <class>ImportDictionariesOverDirectory</class> реализует процессор импорта справочника из директории
 *
 * @author Nazin Alexander
 */
public class ImportDictionariesOverDirectory extends ImportDictionariesOverFile {

    /**
     * Выполняет загрузку основываясь на том, что:<br/>
     * <ul>
     * <li>название директории является именем группы справочников</li>
     * <li>название файла является именем справочника</li>
     * </ul>
     *
     * @param preference настройки импорта
     * @param managerService сервис управления документами
     */
    private ImportDescriptor<Group> processDirectImport(File directory, ImportDictionariesCommand preference, IManagerService managerService) {
        ImmutableList.Builder<Throwable> errorsBuilder = ImmutableList.builder();
        ImmutableList.Builder<Dictionary> dictionariesBuilder = ImmutableList.builder();
        for (File file : directory.listFiles(createFileFilter(preference.getFileType()))) {
            try {
                Dictionary dictionary = createDictionary(
                        FilenameUtils.getBaseName(file.getCanonicalPath()),
                        file.getCanonicalPath(),
                        preference.getFileType(),
                        ImportDictionariesCommand.createConnectorPreference(file.getCanonicalPath(), preference)
                );
                dictionariesBuilder.add(dictionary);
            } catch (Throwable th) {
                errorsBuilder.add(createThrowable(file.getPath(), th));
            }
        }

        ImportDescriptor<Group> importDescriptor = new ImportDescriptor<Group>();
        Collection<Dictionary> dictionaries = dictionariesBuilder.build();
        if (!CollectionUtils.isEmpty(dictionaries)) {
            Group group = new Group();
            {
                group = injectTrimmedName(
                        group,
                        StringUtils.hasText(preference.getGroupName()) ?
                                preference.getGroupName() :
                                FilenameUtils.getBaseName(directory.getPath())
                );
                group = linkDescendants(group, dictionaries);
            }
            // Выполняем сохранение сформированной группы
            try {
                importDescriptor.setDocuments(managerService.storeGroups(Lists.newArrayList(group)));
            } catch (Throwable th) {
                errorsBuilder.add(createThrowable(directory.getPath(), th));
            }
        }
        importDescriptor.setErrors(errorsBuilder.build());

        return importDescriptor;
    }

    /**
     * Выполняет загрузку основываясь на том, что:<br/>
     * <ul>
     * <li>название директории является именем справочника</li>
     * <li>название файла является именем группы</li>
     * </ul>
     *
     * @param preference настройки импорта
     * @param managerService сервис управления документами
     */
    private ImportDescriptor<Group> processInverseImport(File directory, ImportDictionariesCommand preference, IManagerService managerService) {
        ImmutableList.Builder<Throwable> errorsBuilder = ImmutableList.builder();
        ImmutableList.Builder<Group> groupsBuilder = ImmutableList.builder();

        String dictionaryName = StringUtils.hasText(preference.getDictionaryName()) ? preference.getDictionaryName() : FilenameUtils.getBaseName(directory.getPath());
        for (File file : directory.listFiles(createFileFilter(preference.getFileType()))) {
            try {
                Dictionary dictionary = createDictionary(
                        dictionaryName,
                        file.getCanonicalPath(),
                        preference.getFileType(),
                        ImportDictionariesCommand.createConnectorPreference(file.getCanonicalPath(), preference)
                );
                Group group = new Group();
                {
                    group = injectTrimmedName(group, FilenameUtils.getBaseName(file.getCanonicalPath()));
                    group = linkDescendants(group, Lists.newArrayList(dictionary));
                }
                groupsBuilder.addAll(managerService.storeGroups(Lists.newArrayList(group)));
            } catch (Throwable th) {
                errorsBuilder.add(createThrowable(file.getPath(), th));
            }
        }

        return new ImportDescriptor<>(groupsBuilder.build(), errorsBuilder.build());
    }

    @Override
    public ImportDescriptor<Group> processImport(ImportDictionariesCommand preference, IManagerService managerService) {
        File directory = preference.retrieveTargetFile();
        return preference.isInverse() ?
                processInverseImport(directory, preference, managerService) :
                processDirectImport(directory, preference, managerService);
    }
}
