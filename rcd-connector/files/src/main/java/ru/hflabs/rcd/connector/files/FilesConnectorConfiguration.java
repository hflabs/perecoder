package ru.hflabs.rcd.connector.files;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import ru.hflabs.rcd.model.connector.ConnectorConfiguration;

/**
 * Класс <class>FilesConnectorConfiguration</class> содержит информацию о настройках чтения документов на основе файла
 *
 * @see ru.hflabs.rcd.model.connector.ConnectorConfiguration
 */
@Getter
@Setter
public class FilesConnectorConfiguration implements ConnectorConfiguration {

    /** Путь к файлу/директории */
    private final String pathToFile;
    /** Кодировка файла */
    private String encoding;
    /** Символ экранирования */
    private char quote;
    /** Разделитель */
    private int delimiter;

    public FilesConnectorConfiguration(String pathToFile) {
        this.pathToFile = pathToFile;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("pathTo", getPathToFile())
                .append("encoding", getEncoding())
                .append("quote", getQuote())
                .append("delimiter", getDelimiter())
                .toString();
    }

    @Override
    public String identity() {
        return toString();
    }
}
