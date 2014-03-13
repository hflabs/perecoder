package ru.hflabs.rcd.history;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.hflabs.rcd.service.IDifferenceService;
import ru.hflabs.util.core.FormatUtil;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;
import java.util.Map;

/**
 * Класс <class>DifferenceServiceTemplate</class> реализует базовый сервис работы с информацией об изменении сущностей
 *
 * @author Nazin Alexander
 */
public abstract class DifferenceService<E> implements IDifferenceService<E> {

    /** Сервис конвертации объектов */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected static String formatObject(Object object) {
        if (object != null) {
            try {
                return OBJECT_MAPPER.writeValueAsString(object);
            } catch (JsonProcessingException ex) {
                throw new UndeclaredThrowableException(ex);
            }
        } else {
            return FormatUtil.EMPTY_STRING;
        }
    }

    protected static String formatCollection(Collection<?> collection) {
        return formatObject(collection);
    }

    protected static String formatMap(Map<?, ?> collection) {
        return formatObject(collection);
    }
}
