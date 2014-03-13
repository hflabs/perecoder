package org.hibernate.type.descriptor.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.BinaryStream;
import org.hibernate.engine.jdbc.internal.BinaryStreamImpl;
import org.hibernate.type.SerializationException;
import org.hibernate.type.descriptor.WrapperOptions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

/**
 * Класс <class>JsonTypeDescriptor</class> реализует дескриптор сериализации/десиреализации объектов в массив байт через {@link ObjectMapper}
 *
 * @see ObjectMapper#readValue(byte[], Class)
 * @see ObjectMapper#writeValueAsBytes(Object)
 */
public class JsonTypeDescriptor<T> extends AbstractTypeDescriptor<T> {

    private static final long serialVersionUID = -4107124312751304395L;

    /** Сервис преобразования объектов */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public JsonTypeDescriptor(Class<T> type) {
        super(type);
    }

    protected byte[] toBytes(T value) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(value);
        } catch (Exception ex) {
            throw new SerializationException("could not serialize", ex);
        }
    }

    protected T fromBytes(byte[] bytes) {
        try {
            return OBJECT_MAPPER.readValue(bytes, getJavaTypeClass());
        } catch (Exception ex) {
            throw new SerializationException("could not deserialize", ex);
        }
    }

    @Override
    public String toString(T value) {
        return PrimitiveByteArrayTypeDescriptor.INSTANCE.toString(toBytes(value));
    }

    @Override
    public T fromString(String string) {
        return fromBytes(PrimitiveByteArrayTypeDescriptor.INSTANCE.fromString(string));
    }

    @Override
    public boolean areEqual(T one, T another) {
        if (one == another) {
            return true;
        }
        if (one == null || another == null) {
            return false;
        }
        return one.equals(another) || PrimitiveByteArrayTypeDescriptor.INSTANCE.areEqual(toBytes(one), toBytes(another));
    }

    @Override
    public int extractHashCode(T value) {
        return PrimitiveByteArrayTypeDescriptor.INSTANCE.extractHashCode(toBytes(value));
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public <X> X unwrap(T value, Class<X> type, WrapperOptions options) {
        if (value == null) {
            return null;
        } else if (byte[].class.isAssignableFrom(type)) {
            return (X) toBytes(value);
        } else if (InputStream.class.isAssignableFrom(type)) {
            return (X) new ByteArrayInputStream(toBytes(value));
        } else if (BinaryStream.class.isAssignableFrom(type)) {
            return (X) new BinaryStreamImpl(toBytes(value));
        } else if (Blob.class.isAssignableFrom(type)) {
            return (X) options.getLobCreator().createBlob(toBytes(value));
        }

        throw unknownUnwrap(type);
    }

    @Override
    public <X> T wrap(X value, WrapperOptions options) {
        if (value == null) {
            return null;
        } else if (byte[].class.isInstance(value)) {
            return fromBytes((byte[]) value);
        } else if (InputStream.class.isInstance(value)) {
            return fromBytes(DataHelper.extractBytes((InputStream) value));
        } else if (Blob.class.isInstance(value)) {
            try {
                return fromBytes(DataHelper.extractBytes(((Blob) value).getBinaryStream()));
            } catch (SQLException e) {
                throw new HibernateException(e);
            }
        }
        throw unknownWrap(value.getClass());
    }
}
