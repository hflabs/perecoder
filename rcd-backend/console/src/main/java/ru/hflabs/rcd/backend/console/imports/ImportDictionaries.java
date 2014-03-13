package ru.hflabs.rcd.backend.console.imports;

import ru.hflabs.rcd.backend.console.RunTemplate;
import ru.hflabs.rcd.backend.console.imports.handlers.dictionary.ImportDictionariesCommand;
import ru.hflabs.rcd.model.document.Group;

/**
 * Класс <class>ImportDictionaries</class> реализует приложение импорта справочников
 *
 * @author Nazin Alexander
 */
public final class ImportDictionaries extends ImportDocument<ImportDictionariesCommand, Group> {

    public static void main(String[] args) {
        RunTemplate.parseCmdArguments(ImportDictionaries.class, new ImportDictionariesCommand(), args);
    }
}
