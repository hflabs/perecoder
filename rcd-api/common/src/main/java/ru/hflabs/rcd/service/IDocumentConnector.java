package ru.hflabs.rcd.service;

import ru.hflabs.rcd.model.connector.ConnectorConfiguration;
import ru.hflabs.rcd.model.connector.TransferDictionaryDescriptor;
import ru.hflabs.rcd.model.connector.TransferRuleDescriptor;

/**
 * Интерфейс <class>IDocumentConnector</class> декларирует методы сервиса формирования документов
 *
 * @see TransferDictionaryDescriptor
 * @see TransferRuleDescriptor
 */
public interface IDocumentConnector<T extends ConnectorConfiguration, R> {

    /**
     * Формирует контент коллекции справочников
     *
     * @param settings настройки формирования коллекции
     * @return Возвращает сформированную коллекцию справочников
     */
    TransferDictionaryDescriptor readDictionaries(T settings);

    /**
     * Сохраняет контент коллекции справочников
     *
     * @param settings настройки коннектора
     * @param descriptor дескриптор сохранения
     * @return Возвращает артефакт сохранения
     */
    R writeDictionaries(T settings, TransferDictionaryDescriptor descriptor);

    /**
     * Формирует коллекцию правил перекодирования
     *
     * @param settings настройки для формирования коллекции
     * @return Возвращает коллекцию правил перекодирования
     */
    TransferRuleDescriptor readRecodeRules(T settings);

    /**
     * Сохраняет контент правил перекодирования
     *
     * @param settings настройки коннектора
     * @param descriptor дескриптор сохранения
     * @return Возвращает артефакт сохранения
     */
    R writeRecodeRules(T settings, TransferRuleDescriptor descriptor);
}
