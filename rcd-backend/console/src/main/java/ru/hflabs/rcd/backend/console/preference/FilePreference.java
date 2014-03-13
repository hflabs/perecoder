package ru.hflabs.rcd.backend.console.preference;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.CharacterConverter;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.SystemUtils;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.backend.console.Command;
import ru.hflabs.rcd.connector.files.FilesConnectorConfiguration;
import ru.hflabs.util.io.IOUtils;
import ru.hflabs.util.spring.util.ReflectionUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Класс <class>FilePreference</class> содержит информацию о настройках работы с файлами
 *
 * @author Nazin Alexander
 */
@Getter
@Setter
public abstract class FilePreference extends Preference implements Command {

    /*
     * Название полей с идентификаторами
     */
    public static final String ENCODING = "encoding";
    public static final String DELIMITER = "delimiter";
    public static final String QUOTE = "quote";

    /** Кодировка по умолчанию */
    public static final String DEFAULT_ENCODING = IOUtils.DEFAULT_ENCODING;
    /** Разделитель данных по умолчанию */
    public static final char DEFAULT_DELIMITER = ';';
    /** Символ экранирования по умолчанию */
    public static final char DEFAULT_QUOTE = '"';

    @Parameter(description = "FILE", required = true)
    private List<String> pathToFile;

    @Parameter(names = {"--type"}, description = "type of file")
    private String fileType;
    @Parameter(names = {"-e", "--encoding"}, description = "target file encoding")
    private String encoding;
    @Parameter(names = {"-s", "--separator"}, description = "column separator", converter = CharacterConverter.class)
    private char delimiter;
    @Parameter(names = {"-q", "--quote"}, description = "value quote", converter = CharacterConverter.class)
    private char quote;

    public FilePreference() {
        this(DEFAULT_ENCODING);
    }

    protected FilePreference(String encoding) {
        this.encoding = encoding;
        this.delimiter = DEFAULT_DELIMITER;
        this.quote = DEFAULT_QUOTE;
    }

    public void setPathToFile(List<String> pathToFile) {
        this.pathToFile = pathToFile;
    }

    public String getTargetPath() {
        return pathToFile != null && !pathToFile.isEmpty() ? pathToFile.get(0) : null;
    }

    public void setTargetPath(String targetFile) {
        pathToFile = StringUtils.hasText(targetFile) ? Arrays.asList(targetFile) : null;
    }

    public File retrieveTargetFile() {
        return getTargetPath() != null ? new File(getTargetPath()) : null;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    /**
     * Выполняет определение кодировки файла
     *
     * @param pathToFile путь к файлу
     * @param predefined предопределенная кодировка
     * @return Возвращает определенную кодировку
     */
    public static String detectEncoding(String pathToFile, String predefined) {
        if (!StringUtils.hasText(predefined) || FilePreference.DEFAULT_ENCODING.equals(predefined)) {
            try {
                return IOUtils.detectEncoding(pathToFile, SystemUtils.FILE_ENCODING).name();
            } catch (IOException ex) {
                ReflectionUtil.rethrowRuntimeException(ex);
            }
        }
        return predefined;
    }

    /**
     * Создает настройки чтения файла
     *
     * @param pathToFile путь к файлу
     * @param preference настройки импорта
     * @return Возвращает созданные настройки чтения файла
     */
    public static FilesConnectorConfiguration createConnectorPreference(String pathToFile, FilePreference preference) {
        final FilesConnectorConfiguration connectorPreference = new FilesConnectorConfiguration(pathToFile);
        {
            connectorPreference.setEncoding(detectEncoding(pathToFile, preference.getEncoding()));
            connectorPreference.setQuote(preference.getQuote());
            connectorPreference.setDelimiter(preference.getDelimiter());
        }
        return connectorPreference;
    }
}
