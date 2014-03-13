package ru.hflabs.rcd.task.performer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

import javax.xml.bind.annotation.XmlTransient;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Класс <class>ParameterHolder</class> реализует контейнер параметров
 *
 * @author Nazin Alexander
 */
public class ParametersHolder implements Map<String, Object> {

    /** Сервис конвертации объектов */
    private static final transient ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** Настройка сервиса конвертации объектов */
    static {
        OBJECT_MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    }

    /** Репозиторий параметров */
    private final Map<String, Object> delegate;

    public ParametersHolder() {
        this(new LinkedHashMap<String, Object>());
    }

    public ParametersHolder(Map<String, Object> delegate) {
        this.delegate = delegate;
    }

    /**
     * Сохраняет параметр
     *
     * @param name название параметра
     * @param value значение параметра
     */
    protected <T> void injectParameter(String name, T value) {
        delegate.put(name, value);
    }

    /**
     * Возвращает параметр по его имени или <code>NULL</code>, если параметр не установлен
     *
     * @param name название параметра
     * @param targetClass ожидаемый класс параметра
     * @return Возвращает найденный параметр или <code>NULL</code>
     * @throws ClassCastException исключение, если найденный параметр не соответствует ожидаемому классу
     */
    protected <T> T retrieveParameter(String name, Class<T> targetClass) throws ClassCastException {
        return retrieveParameter(name, targetClass, null);
    }

    /**
     * Возвращает параметр по его имени
     *
     * @param name название параметра
     * @param targetClass ожидаемый класс параметра
     * @param defaultValue значение по умолчанию
     * @return Возвращает найденный параметр или <i>defaultValue</i>, если параметр не установлен
     * @throws ClassCastException исключение, если найденный параметр не соответствует ожидаемому классу
     */
    protected <T> T retrieveParameter(String name, Class<T> targetClass, T defaultValue) throws ClassCastException {
        // Получаем значение по его имени
        Object value = delegate.get(name);
        // Выполняем преобразование значения в ожидаемый тип
        if (value != null) {
            if (targetClass.equals(value.getClass())) {
                return targetClass.cast(value);
            } else {
                try {
                    return OBJECT_MAPPER.convertValue(value, targetClass);
                } catch (Exception ex) {
                    return defaultValue;
                }
            }
        } else {
            return defaultValue;
        }
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    @XmlTransient
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return delegate.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return delegate.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        delegate.putAll(m);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<String> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<Object> values() {
        return delegate.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return delegate.entrySet();
    }

    @Override
    public String toString() {
        if (OBJECT_MAPPER != null) {
            try {
                return OBJECT_MAPPER.writeValueAsString(delegate);
            } catch (JsonProcessingException ex) {
                return super.toString();
            }
        } else {
            return super.toString();
        }
    }

    @Override
    public int hashCode() {
        return delegate != null ? delegate.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ParametersHolder that = (ParametersHolder) o;

        if (delegate != null ? !Maps.difference(delegate, that.delegate).areEqual() : that.delegate != null) {
            return false;
        }

        return true;
    }
}
