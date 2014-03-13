package ru.hflabs.rcd.connector.files.converter;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

/**
 * Класс <class>DataSetProviderTest</class>
 *
 * @author Nazin Alexander
 */
public abstract class DataSetConverterTest {

    /**
     * Проверяет, что объект можно сериализовать
     *
     * @param object проверяемый объект
     */
    public static void assertObjectSerialization(Object object) throws Exception {
        try (ObjectOutputStream stream = new ObjectOutputStream(new ByteArrayOutputStream())) {
            stream.writeObject(object);
            stream.flush();
        }
    }
}
