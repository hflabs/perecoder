package ru.hflabs.rcd.backend.console.exports;

import ru.hflabs.rcd.backend.console.RunTemplate;
import ru.hflabs.rcd.backend.console.exports.handlers.dictionary.ExportDictionariesCommand;

/**
 * Класс <class>ExportDictionaries</class> реализует приложение экспорта справочников
 *
 * @author Nazin Alexander
 */
public final class ExportDictionaries extends ExportDocument<ExportDictionariesCommand> {

    public static void main(String[] args) {
        RunTemplate.parseCmdArguments(ExportDictionaries.class, new ExportDictionariesCommand(), args);
    }
}
