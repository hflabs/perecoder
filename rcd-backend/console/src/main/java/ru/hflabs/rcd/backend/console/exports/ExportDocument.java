package ru.hflabs.rcd.backend.console.exports;

import com.beust.jcommander.ParameterException;
import ru.hflabs.rcd.backend.console.Command;
import ru.hflabs.rcd.backend.console.RunTemplate;
import ru.hflabs.rcd.backend.console.exports.handlers.ExportDescriptor;
import ru.hflabs.rcd.backend.console.exports.handlers.ExportProcessor;
import ru.hflabs.rcd.backend.console.preference.FilePreference;
import ru.hflabs.util.spring.Assert;

import java.io.File;

/**
 * Класс <class>ExportDictionaries</class> реализует приложение для экспорта документов
 *
 * @author Nazin Alexander
 */
public abstract class ExportDocument<P extends FilePreference> extends RunTemplate<P, Command, ExportDescriptor> {

    /** Процессор экспорта документов в директорию */
    private ExportProcessor<P> exportProcessor;

    public void setExportProcessor(ExportProcessor<P> exportProcessor) {
        this.exportProcessor = exportProcessor;
    }

    @Override
    protected ExportDescriptor doExecute(P preference, Command command) throws Exception {
        return exportDocuments(preference);
    }

    /**
     * Выполняет импорт документов
     *
     * @param preference настройки импорта
     * @return Возвращает созданную коллекцию документов
     */
    public ExportDescriptor exportDocuments(P preference) throws Exception {
        Assert.notNull(preference, "Export preference not properly initialized", ParameterException.class);
        // Проверяем целевую директорию
        File targetDirectory = preference.retrieveTargetFile();
        Assert.notNull(targetDirectory, "Missing target file parameter", ParameterException.class);
        Assert.isTrue(
                (targetDirectory.exists() && targetDirectory.isDirectory() && targetDirectory.canWrite()) || (!targetDirectory.exists() && targetDirectory.mkdirs()),
                String.format("Directory by path '%s' must exist and have write permissions", targetDirectory.getCanonicalPath()), ParameterException.class
        );
        // Выполняем экспорт
        return exportProcessor.processExport(preference, managerService);
    }
}
