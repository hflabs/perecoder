package ru.hflabs.rcd.model;

import com.google.common.collect.Lists;
import org.testng.annotations.Test;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.document.Record;
import ru.hflabs.rcd.model.notification.Notification;
import ru.hflabs.util.core.date.DateInterval;
import ru.hflabs.util.core.date.DateUtil;

import java.util.List;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static ru.hflabs.rcd.accessor.Accessors.linkRelative;

@Test
public class ModelUtilsTest {

    private static MetaField generateMetaField(String dictionaryId, int ordinal) {
        MetaField metaField = new MetaField();
        metaField.setId(UUID.randomUUID().toString());
        metaField.setDictionaryId(dictionaryId);
        metaField.setName(UUID.randomUUID().toString());
        metaField.setOrdinal(ordinal);
        return metaField;
    }

    private static Field generateField(String row, MetaField metaField) {
        Field field = linkRelative(metaField, new Field());
        field.setId(UUID.randomUUID().toString());
        field.setValue(UUID.randomUUID().toString());
        field.setName(row);
        return field;
    }

    private static void assertRecord(Record record, String expectedId, Field... expectedFields) {
        assertEquals(record.getId(), expectedId);
        assertNotNull(record.getFields());
        assertEquals(record.getFields().size(), expectedFields.length);
        int index = 0;
        for (Field field : record.getFields().values()) {
            assertEquals(field, expectedFields[index++]);
        }
    }

    public void createRecordsTest1() {
        MetaField metaField1 = generateMetaField("1", 0);
        MetaField metaField2 = generateMetaField("1", 1);

        Field field1 = generateField("1", metaField1);
        Field field2 = generateField("2", metaField1);

        Field field3 = generateField("1", metaField2);
        Field field4 = generateField("2", metaField2);

        List<Record> records = ModelUtils.createRecords(
                Lists.newArrayList(metaField2, metaField1),
                Lists.newArrayList(field1, field2, field4, field3)
        );
        assertNotNull(records);
        assertEquals(records.size(), 2);
        assertRecord(records.get(0), "1", field1, field3);
        assertRecord(records.get(1), "2", field2, field4);
    }

    public void createRecordsTest2() {
        MetaField metaField1 = generateMetaField("1", 0);
        MetaField metaField2 = generateMetaField("1", 1);

        Field field1 = generateField("1", metaField1);
        Field field2 = generateField("2", metaField2);

        List<Record> records = ModelUtils.createRecords(
                Lists.newArrayList(metaField2, metaField1),
                Lists.newArrayList(field1, field2)
        );
        assertNotNull(records);
        assertEquals(records.size(), 2);
        assertRecord(records.get(0), "1", field1, null);
        assertRecord(records.get(1), "2", null, field2);
    }

    public void createRecordsTest3() {
        MetaField metaField1 = generateMetaField("1", 0);
        MetaField metaField2 = generateMetaField("2", 1);

        Field field1 = generateField("1", metaField1);
        Field field2 = generateField("2", metaField2);
        Field field3 = generateField("2", generateMetaField("3", 0));

        List<Record> records = ModelUtils.createRecords(
                Lists.newArrayList(metaField1, metaField2),
                Lists.newArrayList(field1, field2, field3)
        );
        assertNotNull(records);
        assertEquals(records.size(), 2);
        assertRecord(records.get(0), "1", field1);
        assertRecord(records.get(1), "2", field2);
    }

    public void createNotificationIntervalTest1() throws Exception {
        Notification notification1 = new Notification();
        {
            notification1.setStartDate(DateUtil.parseDate("04.05.2013"));
            notification1.setEndDate(DateUtil.parseDate("05.05.2013"));
        }
        Notification notification2 = new Notification();
        {
            notification2.setStartDate(DateUtil.parseDate("02.05.2013"));
            notification2.setEndDate(DateUtil.parseDate("03.05.2013"));
        }
        DateInterval interval = ModelUtils.createNotificationInterval(Lists.newArrayList(notification1, notification2));
        assertEquals(interval.first, notification2.getStartDate());
        assertEquals(interval.second, notification1.getEndDate());
    }
}
