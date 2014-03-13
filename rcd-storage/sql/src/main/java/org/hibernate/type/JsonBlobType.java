package org.hibernate.type;

import org.hibernate.MappingException;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.type.descriptor.java.JsonTypeDescriptor;
import org.hibernate.type.descriptor.sql.BlobTypeDescriptor;
import org.hibernate.usertype.DynamicParameterizedType;

import java.io.Serializable;
import java.util.Properties;

/**
 * Класс <class>JsonBlobType</class> реализует сервис привязки типа колонки БД к java объекту
 *
 * @author Nazin Alexander
 */
public class JsonBlobType<T> extends AbstractSingleColumnStandardBasicType<T> implements DynamicParameterizedType {

    private static final long serialVersionUID = 4247076915135915088L;

    public JsonBlobType() {
        super(BlobTypeDescriptor.DEFAULT, new JsonTypeDescriptor(Serializable.class));
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setParameterValues(Properties parameters) {
        if (parameters == null) {
            throw new MappingException("No class or return type defined for type: " + JsonBlobType.class.getName());
        }
        DynamicParameterizedType.ParameterType reader = (DynamicParameterizedType.ParameterType) parameters.get(PARAMETER_TYPE);
        if (reader != null) {
            setJavaTypeDescriptor(new JsonTypeDescriptor<T>(reader.getReturnedClass()));
        } else {
            String className = parameters.getProperty(RETURNED_CLASS);
            if (className == null) {
                throw new MappingException("No class name defined for type: " + JsonBlobType.class.getName());
            }
            try {
                setJavaTypeDescriptor(new JsonTypeDescriptor<T>(ReflectHelper.classForName(className)));
            } catch (ClassNotFoundException e) {
                throw new MappingException("Unable to load class from " + RETURNED_CLASS + " parameter", e);
            }
        }
    }
}
