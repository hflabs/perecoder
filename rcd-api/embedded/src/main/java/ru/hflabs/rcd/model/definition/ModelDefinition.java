package ru.hflabs.rcd.model.definition;

import lombok.Getter;
import lombok.Setter;
import ru.hflabs.rcd.model.Identifying;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;
import java.util.Map;

/**
 * Класс <class>ModelDefinition</class> описывает модель данных
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ModelDefinition implements Identifying, Serializable {

    private static final long serialVersionUID = 981575558336451853L;

    /** Идентификатор модели */
    private String id;
    /** Карта дескрипторов полей */
    private Map<String, ModelFieldDefinition> fields;
    /** Параметры полей по умолчанию */
    private Map<String, Object> defaultParameters;
    /** Доступные значения полей */
    private Map<String, Object> availableValues;

    @Override
    public void injectId(String targetId) {
        setId(targetId);
    }
}
