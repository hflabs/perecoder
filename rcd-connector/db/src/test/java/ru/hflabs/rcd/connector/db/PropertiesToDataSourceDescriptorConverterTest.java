package ru.hflabs.rcd.connector.db;

import org.springframework.core.convert.converter.Converter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.hflabs.rcd.model.connector.db.DataSourceDescriptor;

import java.sql.Driver;
import java.util.Map;
import java.util.Properties;

import static org.testng.Assert.*;
import static ru.hflabs.rcd.model.Descriptioned.DESCRIPTION;
import static ru.hflabs.rcd.model.connector.db.DataSourceDescriptor.*;

@Test
public class PropertiesToDataSourceDescriptorConverterTest {

    private static final String EXISTED_PROPERTIES = "connector_db.properties";
    private static final String NOT_EXISTED_PROPERTY = "notExistedProperty";
    private static final String HSQL_DRIVER_CLASS = org.hsqldb.jdbcDriver.class.getName();
    private static final String HSQL_VALIDATE_QUERY = "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS";

    private Converter<Properties, Map<String, DataSourceDescriptor>> converter;

    @BeforeClass
    private void createInstances() {
        converter = new PropertiesToDataSourceDescriptorConverter();
    }

    private static Properties injectProperties(String name, Properties target) {
        target.put(name + "." + DESCRIPTION, name + "_" + DESCRIPTION);
        target.put(name + "." + URL_TEMPLATE, name + "_" + URL_TEMPLATE);
        target.put(name + "." + DRIVER_CLASS, HSQL_DRIVER_CLASS);
        target.put(name + "." + VALIDATE_QUERY, HSQL_VALIDATE_QUERY);
        target.put(name + ".notExistedProperty", name + "_" + NOT_EXISTED_PROPERTY);
        return target;
    }

    private static void assertDataSourceDescriptor(DataSourceDescriptor actual, String name) {
        assertNotNull(actual);
        assertEquals(actual.getName(), name);
        assertEquals(actual.getDescription(), name + "_" + DESCRIPTION);
        assertEquals(actual.getUrlTemplate(), name + "_" + URL_TEMPLATE);
        assertEquals(actual.getDriverClass(), HSQL_DRIVER_CLASS);
        assertEquals(actual.getValidateQuery(), HSQL_VALIDATE_QUERY);
    }

    public void testConvertAbstract() throws Exception {
        Properties properties = new Properties();

        // first dataSource
        String first = "first";
        properties = injectProperties(first, properties);
        // second dataSource
        String second = "second";
        properties = injectProperties(second, properties);

        Map<String, DataSourceDescriptor> descriptors = converter.convert(properties);
        assertNotNull(descriptors);
        // check first
        assertDataSourceDescriptor(descriptors.remove(first), first);
        // check second
        assertDataSourceDescriptor(descriptors.remove(second), second);

        assertTrue(descriptors.isEmpty());
    }

    public void testConvertExisted() throws Exception {
        Properties properties = new Properties();
        properties.load(
                getClass().getClassLoader().getResourceAsStream(EXISTED_PROPERTIES)
        );
        Map<String, DataSourceDescriptor> descriptors = converter.convert(properties);
        assertNotNull(descriptors);
        assertFalse(descriptors.isEmpty());
        // check driver classes
        for (DataSourceDescriptor descriptor : descriptors.values()) {
            Class<?> driverClass = Class.forName(descriptor.getDriverClass());
            assertNotNull(driverClass);
            assertTrue(Driver.class.isAssignableFrom(driverClass));
        }
    }
}
