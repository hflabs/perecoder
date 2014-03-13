package ru.hflabs.rcd.connector.files.dataset.xml;

/**
 * Интерфейс <class>DictionaryTags</class> декларирует константы XML документа
 *
 * @author Nazin Alexander
 */
public interface DictionaryTags {

    /** Схема документа */
    String XSD_SCHEMA = "schema/dictionary-1.0.xsd";
    String XSD_NAMESPACE = "http://hflabs.ru/rcd/schema/dictionary";

    /** Атрибут с названием элемента */
    String NAME = "name";
    /** Справочник */
    String DICTIONARY = "dictionary";
    /** Запись справочника */
    String RECORD = "record";
    /** МЕТА-поле справочника */
    String META_FIELD = "metaField";
    /** Значение поля справочника */
    String FIELD = "field";
}
