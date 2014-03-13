package ru.hflabs.rcd.backend.console.imports.handlers.rule;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import lombok.Getter;
import lombok.Setter;
import ru.hflabs.rcd.backend.console.preference.FilePreference;

/**
 * Класс <class>ImportRulesCommand</class> описывает настройки импорта правил перекодирования
 *
 * @author Nazin Alexander
 */
@Getter
@Setter
@Parameters(commandNames = ImportRulesCommand.COMMAND_NAME, commandDescription = "import rules")
public class ImportRulesCommand extends FilePreference {

    public static final String COMMAND_NAME = "impr";

    /** Название группы источника */
    @Parameter(names = {"-f", "--from"}, description = "from source system name")
    private String fromGroupName;
    /** Название группы назначения */
    @Parameter(names = {"-t", "--to"}, description = "to source system name")
    private String toGroupName;
    /** Название справочника источника */
    @Parameter(names = {"-n", "--name"}, description = "dictionary name")
    private String fromDictionaryName;
    /** Название справочника назначения */
    @Parameter(names = {"--tname"}, description = "to dictionary name")
    private String toDictionaryName;
    /** Значение целевого справочника по умолчанию */
    @Parameter(names = {"-d", "--default"}, description = "default destination value")
    private String defaultValue;

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }
}
