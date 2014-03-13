package ru.hflabs.rcd.backend.console.imports;

import ru.hflabs.rcd.backend.console.imports.handlers.rule.ImportRulesCommand;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;

/**
 * Класс <class>ImportRules</class> реализует приложение импорта правил перекодирования
 *
 * @author Nazin Alexander
 */
public final class ImportRules extends ImportDocument<ImportRulesCommand, RecodeRuleSet> {

    public static void main(String[] args) {
        parseCmdArguments(ImportRules.class, new ImportRulesCommand(), args);
    }
}
