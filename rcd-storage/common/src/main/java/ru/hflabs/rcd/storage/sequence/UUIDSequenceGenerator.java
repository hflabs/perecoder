package ru.hflabs.rcd.storage.sequence;

import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.service.ISequenceGenerator;

import java.util.UUID;

/**
 * Класс <class>UUIDSequenceGenerator</class> реализует генерацию уникальный идентификаторов через {@link UUID}
 *
 * @see UUID
 */
public class UUIDSequenceGenerator implements ISequenceGenerator {

    @Override
    public <E extends Identifying> String createIdentifier(Class<E> targetClass) {
        return UUID.randomUUID().toString();
    }
}
