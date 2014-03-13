package com.beust.jcommander.defaultprovider;

import com.beust.jcommander.IDefaultProvider;

import java.util.Properties;

/**
 * Класс <class>PropertyDefaultProvider</class> реализует провайдер загрузки значений по умолчанию
 *
 * @author Nazin Alexander
 * @see PropertyFileDefaultProvider
 */
public class PropertyDefaultProvider implements IDefaultProvider {

    /** Набор свойств по умолчанию */
    private Properties properties;

    public PropertyDefaultProvider(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String getDefaultValueFor(String optionName) {
        int index = 0;
        while (index < optionName.length() && !Character.isLetterOrDigit(optionName.charAt(index))) {
            index++;
        }
        String key = optionName.substring(index);
        return properties.getProperty(key);
    }
}
