package ru.hflabs.rcd.model.connector.db;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import ru.hflabs.rcd.model.Descriptioned;
import ru.hflabs.rcd.model.Named;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Класс <class>DataSourceDescriptor</class> описывает информацию о драйвере
 *
 * @see java.sql.Driver
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.PROPERTY)
public final class DataSourceDescriptor implements Named, Descriptioned {

    private static final long serialVersionUID = 2493309931953697894L;

    /*
     * Название полей с идентификаторами
     */
    public static final String URL_TEMPLATE = "urlTemplate";
    public static final String DRIVER_CLASS = "driverClass";
    public static final String VALIDATE_QUERY = "validateQuery";

    /** Название драйвера */
    private String name;
    /** Описание драйвера */
    private String description;
    /** Шаблон URL-а соединения */
    private String urlTemplate;
    /** Название класса драйвера */
    private transient String driverClass;
    /** Запрос валидации соединения */
    private transient String validateQuery;

    @XmlTransient
    public String getDriverClass() {
        return driverClass;
    }

    @XmlTransient
    public String getValidateQuery() {
        return validateQuery;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append(NAME, getName())
                .append(DESCRIPTION, getDescription())
                .toString();
    }
}
