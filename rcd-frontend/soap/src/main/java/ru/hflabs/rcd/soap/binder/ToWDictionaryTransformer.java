package ru.hflabs.rcd.soap.binder;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.dozer.Mapper;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.soap.model.WDictionary;
import ru.hflabs.rcd.soap.model.WGroup;
import ru.hflabs.rcd.soap.model.WMetaField;
import ru.hflabs.util.spring.core.convert.converter.MapperConverter;

/**
 * Класс <class>ToWDictionaryTransformer</class> реализует сервис трансформации {@link Dictionary} в {@link WDictionary}
 *
 * @author Nazin Alexander
 */
public class ToWDictionaryTransformer extends MapperConverter<Dictionary, WDictionary> {

    /** Сервис преобразования группы справочников */
    private Function<Group, WGroup> toWGroupTransformer;
    /** Сервис преобразования МЕТА-поля справочника */
    private Function<MetaField, WMetaField> toWMetaFieldTransformer;

    public ToWDictionaryTransformer(Mapper mapper) {
        super(mapper, WDictionary.class);
    }

    public void setToWGroupTransformer(Function<Group, WGroup> toWGroupTransformer) {
        this.toWGroupTransformer = toWGroupTransformer;
    }

    public void setToWMetaFieldTransformer(Function<MetaField, WMetaField> toWMetaFieldTransformer) {
        this.toWMetaFieldTransformer = toWMetaFieldTransformer;
    }

    @Override
    public WDictionary convert(Dictionary source) {
        WDictionary result = super.convert(source);
        {
            result.setGroup(toWGroupTransformer.apply(source.getRelative()));
            result.getMetaField().addAll(Collections2.transform(source.getDescendants(), toWMetaFieldTransformer));
        }
        return result;
    }
}
