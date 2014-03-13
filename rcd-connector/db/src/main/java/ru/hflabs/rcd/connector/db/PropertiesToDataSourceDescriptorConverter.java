package ru.hflabs.rcd.connector.db;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.model.connector.db.DataSourceDescriptor;
import ru.hflabs.util.core.FormatUtil;

import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.hflabs.rcd.model.Descriptioned.DESCRIPTION;
import static ru.hflabs.rcd.model.connector.db.DataSourceDescriptor.*;

/**
 * Класс <class>PropertiesToDataSourceDescriptorConverter</class> реализует трансформацию свойств в описание БД
 *
 * @author Nazin Alexander
 */
public class PropertiesToDataSourceDescriptorConverter implements Converter<Properties, Map<String, DataSourceDescriptor>> {

    /** Регулярное выражение разделения названия свойств */
    private static final Pattern KEY_PATTERN = Pattern.compile("(\\w+).(\\w+)", Pattern.CASE_INSENSITIVE);

    /**
     * Возвращает название класса по его имени
     *
     * @param className название класса
     * @return Возвращает класс
     */
    private static Class<?> retrieveClassByName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            throw new CannotLoadBeanClassException(null, null, className, ex);
        }
    }

    /**
     * Выполняет актуализацию свойств дескриптора
     *
     * @param descriptor целевой дескриптор
     * @param propertyValue значение свойства
     * @return Возвращает обновленный дескриптор
     */
    private static DataSourceDescriptor populateDataSourceDescriptor(DataSourceDescriptor descriptor, String propertyName, String propertyValue) {
        final String targetValue = FormatUtil.parseString(propertyValue);
        switch (propertyName) {
            case DESCRIPTION: {
                descriptor.setDescription(targetValue);
                break;
            }
            case URL_TEMPLATE: {
                descriptor.setUrlTemplate(targetValue);
                break;
            }
            case DRIVER_CLASS: {
                descriptor.setDriverClass(targetValue);
                break;
            }
            case VALIDATE_QUERY: {
                descriptor.setValidateQuery(targetValue);
                break;
            }
            default: {
                // do nothing
            }
        }
        return descriptor;
    }

    @Override
    public Map<String, DataSourceDescriptor> convert(Properties source) {
        Map<String, DataSourceDescriptor> result = Maps.newLinkedHashMap();

        for (Map.Entry<Object, Object> entry : source.entrySet()) {
            String key = String.valueOf(entry.getKey());

            Matcher matcher = KEY_PATTERN.matcher(key);
            if (matcher.matches()) {
                String dataSourceName = matcher.group(1);
                String dataSourceProperty = matcher.group(2);

                DataSourceDescriptor descriptor = result.get(dataSourceName);
                if (descriptor == null) {
                    descriptor = new DataSourceDescriptor();
                    descriptor.setName(dataSourceName);
                }
                descriptor = populateDataSourceDescriptor(descriptor, dataSourceProperty, (String) entry.getValue());

                result.put(dataSourceName, descriptor);
            }
        }

        // Проверяем, что для всех сформированных дескрипторов установлены необходимые свойства
        for (DataSourceDescriptor descriptor : result.values()) {
            Assert.isTrue(StringUtils.hasText(descriptor.getDriverClass()), String.format("Missing driver class in datasource '%s'", descriptor.getName()));
            Assert.isTrue(StringUtils.hasText(descriptor.getDescription()), String.format("Missing description in datasource '%s'", descriptor.getName()));
        }

        return result;
    }
}
