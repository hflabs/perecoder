package ru.hflabs.rcd.backend.console.imports.handlers;

import ru.hflabs.rcd.backend.console.preference.FilePreference;
import ru.hflabs.rcd.service.IManagerService;

/**
 * Интерфейс <class>ImportProcessor</class> декларирует методы обработчика импорта данных
 *
 * @author Nazin Alexander
 */
public interface ImportProcessor<P extends FilePreference, T> {

    /**
     * Выполняет импорт документов
     *
     * @param preference настройки импорта
     * @param managerService сервис управления документами
     * @return Возвращает дескриптор импорта
     */
    ImportDescriptor<T> processImport(P preference, IManagerService managerService);
}
