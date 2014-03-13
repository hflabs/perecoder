package ru.hflabs.rcd.backend.console.exports.handlers;

import ru.hflabs.rcd.backend.console.preference.FilePreference;
import ru.hflabs.rcd.service.IManagerService;

/**
 * Интерфейс <class>ExportProcessor</class> декларирует методы обработчика экспорта данных
 *
 * @author Nazin Alexander
 */
public interface ExportProcessor<P extends FilePreference> {

    /**
     * Выполняет экспорт документов
     *
     * @param preference настройки экспорта
     * @param managerService сервис управления документами
     * @return Возвращает дескриптор экспорта
     */
    ExportDescriptor processExport(P preference, IManagerService managerService);
}
