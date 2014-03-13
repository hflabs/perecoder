package ru.hflabs.rcd.backend.console.imports.handlers.dictionary;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import lombok.Getter;
import lombok.Setter;
import ru.hflabs.rcd.backend.console.preference.FilePreference;
import ru.hflabs.util.core.FormatUtil;

/**
 * Класс <class>ImportDictionariesCommand</class> описывает настройки импорта справочников
 *
 * @author Nazin Alexander
 */
@Getter
@Setter
@Parameters(commandNames = ImportDictionariesCommand.COMMAND_NAME, commandDescription = "import dictionaries")
public class ImportDictionariesCommand extends FilePreference {

    public static final String COMMAND_NAME = "impd";

    /** Название группы справочников */
    @Parameter(names = {"-g", "--group"}, description = "source system name")
    private String groupName;
    /** Название справочника */
    @Parameter(names = {"-n", "--name"}, description = "dictionary name")
    private String dictionaryName;
    /** Описание справочника */
    private String dictionaryDescription;
    /** Флаг инвертированной обработки директории */
    @Parameter(names = {"-i", "--inverse"}, description = "inverse detect group and dictionary names")
    private boolean inverse = false;

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

    public void setDictionaryDescription(String dictionaryDescription) {
        this.dictionaryDescription = FormatUtil.parseString(dictionaryDescription);
    }
}
