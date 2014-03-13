package ru.hflabs.rcd.backend.console.exports.handlers;

import ru.hflabs.rcd.backend.console.RunDescriptor;
import ru.hflabs.rcd.model.path.DictionaryNamedPath;

import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * Класс <class>ExportDescriptor</class> реализует дескриптор выгрузки документов
 *
 * @author Nazin Alexander
 */
public class ExportDescriptor extends RunDescriptor {

    /** Путь к директории */
    private final File directory;
    /** Путь к архиву */
    private File pathToArchive;
    /** Коллекция соответствий справочника к его файлу */
    private final Map<DictionaryNamedPath, File> files;

    public ExportDescriptor(Collection<Throwable> errors, File directory, Map<DictionaryNamedPath, File> files) {
        this(errors, directory, null, files);
    }

    public ExportDescriptor(Collection<Throwable> errors, File directory, File pathToArchive, Map<DictionaryNamedPath, File> files) {
        super(errors);
        this.directory = directory;
        this.pathToArchive = pathToArchive;
        this.files = files;
    }

    public File getDirectory() {
        return directory;
    }

    public File getPathToArchive() {
        return pathToArchive;
    }

    public void setPathToArchive(File pathToArchive) {
        this.pathToArchive = pathToArchive;
    }

    public Map<DictionaryNamedPath, File> getFiles() {
        return files;
    }
}
