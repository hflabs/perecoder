package ru.hflabs.rcd;

import lombok.Getter;
import ru.hflabs.util.io.IOUtils;

import java.io.File;

/**
 * Класс <class>Directories</class> содержит информацию об используемых внешних директориях
 *
 * @see System#getProperty(String)
 */
@Getter
public final class Directories {

    /** Корневая директория */
    public static final Directories ROOT_FOLDER = retrieveDirectoryByRelativeDirectory(null, "rcd", "rcd.root.folder", "RCD root directory", null);

    /** Директория хранения внеменных данных */
    public static final Directories TMP_FOLDER = retrieveDirectoryByRelativeDirectory(ROOT_FOLDER, "tmp", "rcd.tmp.folder", "RCD tmp directory", System.getProperty("java.io.tmpdir"));

    /** Директория с исходными динамическими классами */
    public static final Directories RUNTIME_FOLDER_SOURCE = retrieveDirectoryByRelativeDirectory(null, "runtime", "rcd.runtime.folder.source", "RCD runtime sources directory", null);
    /** Директория скомпилированных классов */
    public static final Directories RUNTIME_FOLDER_COMPILED = retrieveDirectoryByRelativeDirectory(null, "runtime", "rcd.runtime.folder.compiled", "RCD runtime compiled code directory", null);

    /** Директория с конфигурационными параметрами */
    public static final Directories PROPERTIES_FOLDER = retrieveDirectoryByRelativeDirectory(ROOT_FOLDER, "conf", "rcd.conf.folder", "RCD configuration directory", null);

    /** Директория хранения данных */
    public static final Directories STORAGE_FOLDER = retrieveDirectoryByRelativeDirectory(ROOT_FOLDER, "storage", "rcd.storage.folder", "RCD storage directory", null);

    /** Ключ JVM */
    public final String key;
    /** Описание директории */
    public final String description;
    /** Путь к директории */
    public final File location;

    /**
     * @param key ключ JVM
     * @param description описание директории
     * @param location путь к директории
     */
    public Directories(String key, String description, File location) {
        this.key = key;
        this.description = description;
        this.location = location;
    }

    /**
     * Создает и возвращает относительную директорию по принциму:<br/>
     * <ul>
     * <li>Если в параметрах JVM указан ключ - то создать директорию по этому ключу</li>
     * <li>Если в параметрах JVM не указан ключ - то создать директорию относительно родительско с указанными названием</li>
     * <li>Если в параметрах JVM не указан ключ и относительная директория - то создать директорию со значением по умолчанию</li>
     * </ul>
     *
     * @param directory относительная директория
     * @param defaultName название директории по умолчанию
     * @param key ключ JVM
     * @param description описание директории
     * @return Возвращает относительную директорию
     */
    public static Directories retrieveDirectoryByRelativeDirectory(Directories directory, String defaultName, String key, String description, String defaultValue) {
        // Получаем ключ, который указан принудительно
        String location = System.getProperty(key);
        // Если принудительный ключ не задан, то используем относительный или умолчательный
        if (location == null) {
            location = (directory != null && directory.location != null) ?
                    new File(directory.location, defaultName).getAbsolutePath() :
                    defaultValue;
        }
        // Создаем и возвращаем внешнюю директорию
        return new Directories(key, description, IOUtils.getFolder(location, true));
    }
}
