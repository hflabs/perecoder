package ru.hflabs.rcd.web.model.transfer;

import ru.hflabs.rcd.backend.console.exports.handlers.dictionary.ExportDictionariesCommand;

/**
 * Класс <class>DownloadDictionaryDescriptor</class> реализует декоратор, содержащий информацию о настройках экспорта справочника
 *
 * @see ExportDictionariesCommand
 */
public class DownloadDictionaryDescriptor extends TransferDictionaryDescriptor<ExportDictionariesCommand> {

    private static final long serialVersionUID = 5935432677138987073L;

    public DownloadDictionaryDescriptor() {
        super(new ExportDictionariesCommand());
    }
}
