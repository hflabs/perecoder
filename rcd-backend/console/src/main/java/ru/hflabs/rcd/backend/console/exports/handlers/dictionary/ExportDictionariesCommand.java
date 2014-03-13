package ru.hflabs.rcd.backend.console.exports.handlers.dictionary;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.SystemUtils;
import ru.hflabs.rcd.backend.console.preference.FilePreference;
import ru.hflabs.util.core.FormatUtil;

/**
 * Класс <class>ExportDictionariesCommand</class> содержит информацию о настройках экспорта справочников
 *
 * @author Nazin Alexander
 */
@Getter
@Setter
@Parameters(commandNames = ExportDictionariesCommand.COMMAND_NAME, commandDescription = "export dictionaries")
public class ExportDictionariesCommand extends FilePreference {

    public static final String COMMAND_NAME = "expd";

    /** Название группы справочников */
    @Parameter(names = {"-g", "--group"}, description = "source system name")
    private String groupName;
    /** Название справочника */
    @Parameter(names = {"-d", "--dictionary"}, description = "dictionary name")
    private String dictionaryName;
    /** Флаг необходимости экспорта МЕТА-полей справочника */
    @Parameter(names = {"-m", "--meta"}, description = "export dictionary structure")
    private boolean meta = false;
    /** Флаг необходимости экспорта скрытых МЕТА-полей справочника */
    @Parameter(names = {"--hidden"}, description = "export hidden dictionary META fields")
    private boolean hidden = false;
    /** Флаг необходимости сжатия результирующей директории */
    @Parameter(names = {"--zip"}, description = "compress result directory")
    private boolean compress = false;

    public ExportDictionariesCommand() {
        super(SystemUtils.FILE_ENCODING);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    public void setGroupName(String groupName) {
        this.groupName = FormatUtil.parseString(groupName);
    }

    public void setDictionaryName(String dictionaryName) {
        this.dictionaryName = FormatUtil.parseString(dictionaryName);
    }
}
