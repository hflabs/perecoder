package ru.hflabs.rcd.model.connector;

import lombok.Getter;

/**
 * Класс <class>TransferDescriptor</class> описывает настройки трансфера документов
 */
@Getter
public class TransferDescriptor<D> {

    /** Контент документов */
    private final D content;

    public TransferDescriptor(D content) {
        this.content = content;
    }
}
