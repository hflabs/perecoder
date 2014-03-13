package ru.hflabs.rcd.soap.binder;

import com.google.common.base.Function;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.document.Record;
import ru.hflabs.rcd.soap.model.WField;
import ru.hflabs.rcd.soap.model.WRecord;

import java.util.Map;

/**
 * Класс <class>ToWRecordTransformer</class> реализует сервис трансформации {@link Record} в {@link WRecord}
 *
 * @author Nazin Alexander
 */
public class ToWRecordTransformer implements Function<Record, WRecord> {

    @Override
    public WRecord apply(Record input) {
        WRecord result = new WRecord();
        result.setId(input.getId());
        Map<String, Field> fields = input.getFields();
        if (fields != null) {
            for (Map.Entry<String, Field> entry : fields.entrySet()) {
                WField wField = new WField();
                wField.setName(entry.getKey());
                wField.setValue(entry.getValue().getValue());
                result.getField().add(wField);
            }
        }
        return result;
    }
}
