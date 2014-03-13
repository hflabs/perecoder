package ru.hflabs.rcd.model.connector;

import lombok.Getter;
import ru.hflabs.rcd.model.document.Dictionary;

import java.util.Collection;

/**
 * Класс <class>TransferDictionaryDescriptor</class> дескриптор трансфера справочников
 */
@Getter
public class TransferDictionaryDescriptor extends TransferDescriptor<Collection<Dictionary>> {

    /** Флаг включения структуры справочника */
    private final boolean withStructure;
    /** Флаг включения скрытых МЕТА-полей и данных справочника */
    private final boolean withHidden;

    public TransferDictionaryDescriptor(Collection<Dictionary> dictionaries, boolean withStructure, boolean withHidden) {
        super(dictionaries);
        this.withStructure = withStructure;
        this.withHidden = withHidden;
    }

    public boolean isEmpty() {
        return getContent().isEmpty();
    }
}
