package ru.hflabs.rcd.soap.binder;

import org.dozer.Mapper;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.soap.model.WMetaField;
import ru.hflabs.util.spring.core.convert.converter.MapperConverter;

/**
 * Класс <class>ToWMetaFieldTransformer</class> реализует сервис трансформации {@link MetaField} в {@link WMetaField}
 *
 * @author Nazin Alexander
 */
public class ToWMetaFieldTransformer extends MapperConverter<MetaField, WMetaField> {

    public ToWMetaFieldTransformer(Mapper mapper) {
        super(mapper, WMetaField.class);
    }

    @Override
    public WMetaField convert(MetaField source) {
        WMetaField result = super.convert(source);
        {
            result.setPrimary(source.isFlagEstablished(MetaField.FLAG_PRIMARY));
            result.setUnique(source.isFlagEstablished(MetaField.FLAG_UNIQUE));
        }
        return result;
    }
}
