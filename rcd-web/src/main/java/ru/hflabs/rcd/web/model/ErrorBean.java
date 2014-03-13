package ru.hflabs.rcd.web.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * Класс <class>ErrorBean</class> реализует декоратор ошибок при выполнении запроса
 *
 * @author Nazin Alexander
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ErrorBean implements Serializable {

    private static final long serialVersionUID = -6507876639566627819L;

    /*
     * Название полей с идентификаторами
     */
    public static final String GLOBAL_ERRORS = "globalErrors";
    public static final String FIELD_ERRORS = "fieldErrors";

    /** Код сообщения по умолчанию */
    public static final String UNEXPECTED_ERROR_KEY = Throwable.class.getSimpleName();

    /** Глобальные ошибки при выполнении запроса */
    private Collection<String> globalErrors;
    /** Ошибки валидации полей при выполнении запроса */
    private Map<String, String> fieldErrors;

    public ErrorBean() {
        this(null);
    }

    public ErrorBean(Collection<String> globalErrors) {
        this(globalErrors, null);
    }

    public ErrorBean(Collection<String> globalErrors, Map<String, String> fieldErrors) {
        setGlobalErrors(globalErrors);
        setFieldErrors(fieldErrors);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
