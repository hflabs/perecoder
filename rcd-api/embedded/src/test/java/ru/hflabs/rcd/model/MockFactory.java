package ru.hflabs.rcd.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import ru.hflabs.rcd.model.change.ChangeType;
import ru.hflabs.rcd.model.change.History;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.*;
import ru.hflabs.rcd.model.rule.RecodeRule;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;
import ru.hflabs.rcd.model.task.TaskDescriptor;
import ru.hflabs.rcd.model.task.TaskResultStatus;
import ru.hflabs.util.core.Pair;

import java.util.*;

import static ru.hflabs.rcd.accessor.Accessors.*;
import static ru.hflabs.rcd.model.ModelUtils.*;

/**
 * Класс <class>MockFactory</class> реализует фабрику создания <i>mock</i> объектов
 */
public abstract class MockFactory {

    protected MockFactory() {
    }

    public static History createMockHistory() {
        final History history = new History();
        history.setId(UUID.randomUUID().toString());
        history.setEventType(ChangeType.IGNORE);
        history.setEventDate(new Date());
        return history;
    }

    private static <T extends DocumentTemplate> T generateDocumentTemplate(T document) {
        document.injectId(UUID.randomUUID().toString());
        document.setHistory(createMockHistory());
        return document;
    }

    public static Group createMockGroup() {
        final Group group = generateDocumentTemplate(new Group());
        group.setName(UUID.randomUUID().toString());
        group.setDescription(UUID.randomUUID().toString());
        group.setOwner(UUID.randomUUID().toString());
        return group;
    }

    public static Dictionary createMockDictionary(Group group) {
        final Dictionary dictionary = generateDocumentTemplate(new Dictionary());
        dictionary.setName(UUID.randomUUID().toString());
        dictionary.setDescription(UUID.randomUUID().toString());
        return linkRelative(group, dictionary);
    }

    public static MetaField createMockMetaField(Dictionary dictionary) {
        final MetaField metaField = generateDocumentTemplate(new MetaField());
        metaField.setName(UUID.randomUUID().toString());
        metaField.setDescription(UUID.randomUUID().toString());
        return linkRelative(dictionary, metaField);
    }

    public static Pair<Collection<MetaField>, Collection<Record>> createMockRecords(Dictionary dictionary, int metaFieldCount, int recordCount) {
        Collection<MetaField> metaFields = new ArrayList<>(metaFieldCount);
        for (int i = 0; i < metaFieldCount; i++) {
            metaFields.add(createMockMetaField(dictionary));
        }
        Collection<Field> fields = new ArrayList<>(metaFieldCount * recordCount);
        for (int i = 0; i < recordCount; i++) {
            for (MetaField metaField : metaFields) {
                Field field = createMockField(metaField);
                field.setName(String.valueOf(i));
                fields.add(field);
            }
        }
        return Pair.valueOf(metaFields, createRecords(dictionary.getId(), metaFields, fields));
    }

    public static Field createMockField(MetaField metaField) {
        final Field field = generateDocumentTemplate(new Field());
        field.setName(UUID.randomUUID().toString());
        field.setValue(UUID.randomUUID().toString());
        return linkRelative(metaField, field);
    }

    public static Group generateMockGroup(String groupName, int dictionaryCount, int metaFieldCount, int rowCount) {
        Group group = createMockGroup();
        group.setName(groupName);
        {
            Collection<Dictionary> dictionaries = new ArrayList<>(dictionaryCount);
            for (int i = 0; i < dictionaryCount; i++) {
                Dictionary dictionary = createMockDictionary(group);
                dictionary = injectName(dictionary, groupName + "_" + i);
                Pair<Collection<MetaField>, Collection<Record>> records = createMockRecords(dictionary, metaFieldCount, rowCount);
                dictionary.setDescendants(records.first);
                dictionary.setRecords(records.second);
                dictionaries.add(dictionary);
            }
            group = linkDescendants(group, dictionaries);
        }
        return group;
    }

    public static RecodeRuleSet createMockRecodeRuleSet() {
        MetaField fromMetaField = createMockMetaField(createMockDictionary(createMockGroup()));
        MetaField toMetaField = createMockMetaField(createMockDictionary(createMockGroup()));
        Field defaultField = createMockField(toMetaField);
        return createMockRecodeRuleSet(fromMetaField, toMetaField, defaultField);
    }

    public static RecodeRuleSet createMockRecodeRuleSet(MetaField fromMetaField, MetaField toMetaField, Field defaultField) {
        return generateDocumentTemplate(new RecodeRuleSet())
                .injectFromNamedPath(createMetaFieldNamedPath(fromMetaField))
                .injectFrom(fromMetaField)
                .injectToNamedPath(createMetaFieldNamedPath(toMetaField))
                .injectTo(toMetaField)
                .injectDefaultField(defaultField);
    }

    public static RecodeRule createMockRecodeRule(RecodeRuleSet ruleSet, Field fromField, Field toField) {
        return generateDocumentTemplate(new RecodeRule())
                .injectRecodeRuleSet(ruleSet)
                .injectFromNamedPath(createFieldNamedPath(fromField))
                .injectFrom(fromField)
                .injectToNamedPath(createFieldNamedPath(toField))
                .injectTo(toField);
    }

    public static Map<String, Object> createMockTaskParameters() {
        return Maps.newLinkedHashMap(
                ImmutableMap.<String, Object>builder()
                        .put("stringValue", "string")
                        .put("longValue", 0L)
                        .put("integerValue", 0)
                        .put("dateValue", new Date().getTime())
                        .put("enumValue", TaskResultStatus.UNKNOWN.name())
                        .build()
        );
    }

    public static TaskDescriptor createMockTaskDescriptor() {
        final TaskDescriptor taskDescriptor = generateDocumentTemplate(new TaskDescriptor());
        taskDescriptor.setName("mockTaskPerformer");
        taskDescriptor.setDescription(UUID.randomUUID().toString());
        taskDescriptor.setCron("* * * * *");
        taskDescriptor.setNextScheduledDate(new Date());
        taskDescriptor.setParameters(createMockTaskParameters());
        return taskDescriptor;
    }
}
