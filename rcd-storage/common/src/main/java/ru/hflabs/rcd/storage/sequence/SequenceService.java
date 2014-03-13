package ru.hflabs.rcd.storage.sequence;

import org.springframework.util.StringUtils;
import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.service.ISequenceGenerator;
import ru.hflabs.rcd.service.ISequenceService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static ru.hflabs.rcd.accessor.Accessors.injectId;

/**
 * Класс <class>SequenceService</class> реализует сервис генерации и заполнения уникальных идентификаторов
 *
 * @author Nazin Alexander
 */
public class SequenceService implements ISequenceService {

    /** Сервис генерации идентификаторов по умолчанию */
    private ISequenceGenerator sequenceGenerator;

    public SequenceService(ISequenceGenerator sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public <E extends Identifying> String createIdentifier(Class<E> targetClass) {
        return sequenceGenerator.createIdentifier(targetClass);
    }

    @Override
    public <E extends Identifying> E fillIdentifier(E object, boolean overrideExisted) {
        return (object != null && (overrideExisted || !StringUtils.hasText(object.getId()))) ?
                injectId(object, createIdentifier(object.getClass())) :
                object;
    }

    @Override
    public <E extends Identifying> Collection<E> fillIdentifiers(Collection<E> objects, boolean overrideExisted) {
        final List<E> result = new ArrayList<>(objects.size());
        for (E object : objects) {
            result.add(fillIdentifier(object, overrideExisted));
        }
        return result;
    }
}
