package ru.hflabs.rcd.backend.console.imports;

import com.beust.jcommander.ParameterException;
import ru.hflabs.rcd.backend.console.Command;
import ru.hflabs.rcd.backend.console.RunTemplate;
import ru.hflabs.rcd.backend.console.imports.handlers.ImportDescriptor;
import ru.hflabs.rcd.backend.console.imports.handlers.ImportProcessor;
import ru.hflabs.rcd.backend.console.preference.FilePreference;
import ru.hflabs.util.spring.Assert;

import java.io.File;

/**
 * Класс <class>ImportDocument</class> реализует приложение для импорта документов
 *
 * @author Nazin Alexander
 */
public abstract class ImportDocument<P extends FilePreference, T> extends RunTemplate<P, Command, ImportDescriptor<T>> {

    /** Процессор импорта документов на основе файла */
    private ImportProcessor<P, T> importOverFileProcessor;
    /** Процессор импорта документов на основе директории */
    private ImportProcessor<P, T> importOverDirectoryProcessor;

    public void setImportOverFileProcessor(ImportProcessor<P, T> importOverFileProcessor) {
        this.importOverFileProcessor = importOverFileProcessor;
    }

    public void setImportOverDirectoryProcessor(ImportProcessor<P, T> importOverDirectoryProcessor) {
        this.importOverDirectoryProcessor = importOverDirectoryProcessor;
    }

    @Override
    protected ImportDescriptor<T> doExecute(P preference, Command command) throws Exception {
        return importDocuments(preference);
    }

    /**
     * Выполняет импорт документов
     *
     * @param preference настройки импорта
     * @return Возвращает созданную коллекцию документов
     */
    public ImportDescriptor<T> importDocuments(P preference) throws Exception {
        Assert.notNull(preference, "Import preference not properly initialized", ParameterException.class);
        // Проверяем целевой файл
        final File targetFile = preference.retrieveTargetFile();
        Assert.notNull(targetFile, "Missing target file parameter", ParameterException.class);
        Assert.isTrue(
                targetFile.exists() && targetFile.canRead(),
                String.format("File by path '%s' must exist and have read permissions", targetFile.getCanonicalPath()), ParameterException.class
        );
        // Определяем операцию по типу файла и выполняем импорт
        return (targetFile.isFile()) ?
                importOverFileProcessor.processImport(preference, managerService) :
                importOverDirectoryProcessor.processImport(preference, managerService);
    }
}
