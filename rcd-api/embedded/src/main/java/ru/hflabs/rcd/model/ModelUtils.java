package ru.hflabs.rcd.model;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;
import ru.hflabs.rcd.accessor.Accessors;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.*;
import ru.hflabs.rcd.model.notification.Notification;
import ru.hflabs.rcd.model.path.DictionaryNamedPath;
import ru.hflabs.rcd.model.path.DirectionNamedPath;
import ru.hflabs.rcd.model.path.FieldNamedPath;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;
import ru.hflabs.rcd.model.rule.Rule;
import ru.hflabs.util.core.EqualsUtil;
import ru.hflabs.util.core.MD5;
import ru.hflabs.util.core.date.DateInterval;
import ru.hflabs.util.spring.Assert;

import java.util.*;

import static ru.hflabs.rcd.accessor.Accessors.META_FIELD_TO_FIELD_INJECTOR;
import static ru.hflabs.rcd.accessor.Accessors.injectId;

/**
 * Класс <class>ModelUtils</class> реализует вспомогательные методы для работы с моделью
 *
 * @see Function
 * @see Accessors
 */
public abstract class ModelUtils {

    /** Функция выделения/установки первичного ключа документа */
    public static final Function<Identifying, String> ID_FUNCTION = new Function<Identifying, String>() {

        @Override
        public String apply(Identifying input) {
            return input != null ? input.getId() : null;
        }
    };
    /** Функция выделения идентификатора истории документа */
    public static final Function<Historical, String> HISTORY_ID_FUNCTION = new Function<Historical, String>() {

        @Override
        public String apply(Historical input) {
            return input != null ? input.getHistoryId() : null;
        }
    };
    /** Функция выделения идентификатора связанной сущности */
    public static final Function<ManyToOne<?>, String> RELATIVE_ID_FUNCTION = new Function<ManyToOne<?>, String>() {
        @Override
        public String apply(ManyToOne<?> input) {
            return input != null ? input.getRelativeId() : null;
        }
    };

    /** Сервис доступа к значению поля */
    public static final Function<Field, String> FIELD_VALUE = new Function<Field, String>() {
        @Override
        public String apply(Field input) {
            return input != null ? input.getValue() : null;
        }
    };

    /** Сервис доступа к идентификатору поля источника */
    public static final Function<Rule<?, ?, ?>, String> FROM_RULE_FIELD_ID = new Function<Rule<?, ?, ?>, String>() {
        @Override
        public String apply(Rule<?, ?, ?> input) {
            return input.getFromFieldId();
        }
    };
    /** Сервис доступа к идентификатору поля назначения */
    public static final Function<Rule<?, ?, ?>, String> TO_RULE_FIELD_ID = new Function<Rule<?, ?, ?>, String>() {
        @Override
        public String apply(Rule<?, ?, ?> input) {
            return input.getToFieldId();
        }
    };

    /** Функция выделения имени документа */
    public static final Function<Named, String> NAME_FUNCTION = new Function<Named, String>() {
        @Override
        public String apply(Named input) {
            return input != null ? input.getName() : null;
        }
    };
    /** Функция преобразования строки в нижний регистр */
    public static final Function<String, String> LOWER_CASE_FUNCTION = new Function<String, String>() {
        @Override
        public String apply(String input) {
            return input != null ? input.toLowerCase() : null;
        }
    };
    /** Формируем функцию преоразования имени в нижний регистр */
    public static final Function<Named, String> LOWER_CASE_NAME_FUNCTION = Functions.compose(LOWER_CASE_FUNCTION, NAME_FUNCTION);

    protected ModelUtils() {
        // embedded constructor
    }

    @SuppressWarnings("unchecked")
    public static <T extends Identifying> Function<T, String> idFunction() {
        return (Function<T, String>) ID_FUNCTION;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Named> Function<T, String> nameFunction() {
        return (Function<T, String>) NAME_FUNCTION;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Named> Function<T, String> lowerNameFunction() {
        return (Function<T, String>) LOWER_CASE_NAME_FUNCTION;
    }

    /**
     * @param metaFields коллекция МЕТА-полей
     * @return Возвращает отсортированную коллекцию МЕТА-полей
     */
    public static List<MetaField> sortMetaFieldsByOrdinal(Collection<MetaField> metaFields) {
        List<MetaField> sortedMetaFields = Lists.newArrayList(metaFields);
        Collections.sort(sortedMetaFields, Comparators.META_FIELD_ORDINAL_COMPARATOR);
        return sortedMetaFields;
    }

    /**
     * @param metaFields коллекция предендентов
     * @return Возвращает найденное первичное МЕТА-поле или <code>NULL</code>
     */
    public static MetaField retrievePrimaryMetaField(Collection<MetaField> metaFields) {
        if (!CollectionUtils.isEmpty(metaFields)) {
            final Collection<MetaField> fields = Collections2.filter(metaFields, new Predicate<MetaField>() {
                @Override
                public boolean apply(MetaField input) {
                    return input.isFlagEstablished(MetaField.FLAG_PRIMARY);
                }
            });
            if (fields.size() == 1) {
                return fields.iterator().next();
            }
        }
        // Первичное МЕТА-поле не найдено
        return null;
    }

    /**
     * @param metaFields коллекция предендентов
     * @return Возвращает <b>НЕ скрытые</b> МЕТА-поля
     */
    public static Collection<MetaField> retrieveNotHiddenMetaFields(Collection<MetaField> metaFields) {
        return !CollectionUtils.isEmpty(metaFields) ?
                Collections2.filter(metaFields, new Predicate<MetaField>() {
                    @Override
                    public boolean apply(MetaField input) {
                        return !input.isFlagEstablished(MetaField.FLAG_HIDDEN);
                    }
                }) :
                Collections.<MetaField>emptyList();
    }

    /**
     * Выполняет удаление скрытых МЕТА-полей из записей
     *
     * @param records коллекция записей
     * @return Возвращает модифицированные записи
     */
    public static Collection<Record> removeHiddenMetaFields(Collection<Record> records) {
        ImmutableList.Builder<Record> result = ImmutableList.builder();
        for (Record record : records) {
            Map<String, Field> filtered = Maps.newLinkedHashMap(Maps.filterEntries(record.getFields(), new Predicate<Map.Entry<String, Field>>() {
                @Override
                public boolean apply(Map.Entry<String, Field> input) {
                    Field field = input.getValue();
                    if (field != null) {
                        MetaField metaField = META_FIELD_TO_FIELD_INJECTOR.apply(input.getValue());
                        if (metaField != null) {
                            return !metaField.isFlagEstablished(MetaField.FLAG_HIDDEN);
                        }
                    }
                    return true;
                }
            }));
            record.setFields(filtered);
            result.add(record);
        }
        return result.build();
    }

    /**
     * Создает коллекцию записей справочника по МЕТА-полям с заполненными значениями
     *
     * @param dictionaryId идентификатор справочника
     * @param metaFields коллекция МЕТА-полей справочника с заполненными {@link Field значениями}
     * @return Возвращает сформированные записи
     */
    public static Collection<Record> createRecords(String dictionaryId, Collection<MetaField> metaFields) {
        Map<String, Record> id2record = new LinkedHashMap<>();
        for (MetaField metaField : sortMetaFieldsByOrdinal(metaFields)) {
            String metaFieldName = metaField.getName();
            Collection<Field> fields = metaField.getDescendants();
            if (!CollectionUtils.isEmpty(fields)) {
                for (Field field : fields) {
                    String recordId = NAME_FUNCTION.apply(field);

                    Record record = id2record.get(recordId);
                    if (record == null) {
                        record = injectId(new Record(), recordId);
                        record.setDictionaryId(dictionaryId);
                    }

                    Map<String, Field> recordFields = record.getFields();
                    if (recordFields == null) {
                        recordFields = new LinkedHashMap<>();
                    }

                    recordFields.put(metaFieldName, field);
                    record.injectFields(recordFields);
                    id2record.put(recordId, record);
                }
            }
        }
        return Lists.newArrayList(id2record.values());
    }

    /**
     * Создает коллекцию записей по МЕТА-полям и значениям справочника
     *
     * @param dictionaryId идентификатор справочника
     * @param metaFields МЕТА-поля справочника
     * @param fields значения полей записи справочника
     * @return Возвращает сформированные записи
     */
    public static Collection<Record> createRecords(final String dictionaryId, Collection<MetaField> metaFields, Collection<Field> fields) {
        List<MetaField> sortedMetaFields = sortMetaFieldsByOrdinal(Collections2.filter(metaFields, new Predicate<MetaField>() {
            @Override
            public boolean apply(MetaField input) {
                return EqualsUtil.equals(dictionaryId, input.getDictionaryId());
            }
        }));

        Map<String, Collection<Field>> row2fields = Multimaps.index(fields, NAME_FUNCTION).asMap();
        ImmutableList.Builder<Record> resultBuilder = ImmutableList.builder();
        for (Map.Entry<String, Collection<Field>> entry : row2fields.entrySet()) {
            Map<String, Field> metaFieldId2Field = Maps.uniqueIndex(entry.getValue(), new Function<Field, String>() {
                @Override
                public String apply(Field input) {
                    return input.getMetaFieldId();
                }
            });

            Map<String, Field> content = new LinkedHashMap<String, Field>(sortedMetaFields.size());
            for (MetaField metaField : sortedMetaFields) {
                content.put(metaField.getName(), metaFieldId2Field.get(metaField.getId()));
            }

            Record record = new Record().injectFields(content);
            record.setId(entry.getKey());
            record.setDictionaryId(dictionaryId);

            resultBuilder.add(record);
        }

        return Lists.newArrayList(resultBuilder.build());
    }

    /**
     * Создает коллекцию записей по МЕТА-полям и значениям полей справочников
     *
     * @param metaFields МЕТА-поля справочников
     * @param fields значения полей записи справочников
     * @return Возвращает сформированные записи
     */
    public static List<Record> createRecords(Collection<MetaField> metaFields, Collection<Field> fields) {
        Map<String, Collection<MetaField>> dictionaryId2MetaFields = Multimaps.index(metaFields, new Function<MetaField, String>() {
            @Override
            public String apply(MetaField input) {
                return input.getDictionaryId();
            }
        }).asMap();

        ImmutableList.Builder<Record> resultBuilder = ImmutableList.builder();
        for (Map.Entry<String, Collection<MetaField>> entry : dictionaryId2MetaFields.entrySet()) {
            final Set<String> metaFieldIDs = Sets.newHashSet(Collections2.transform(entry.getValue(), ID_FUNCTION));
            Collection<Field> targetFields = Collections2.filter(fields, new Predicate<Field>() {
                @Override
                public boolean apply(Field input) {
                    return metaFieldIDs.contains(input.getMetaFieldId());
                }
            });
            resultBuilder.addAll(createRecords(entry.getKey(), entry.getValue(), targetFields));
        }

        return Lists.newArrayList(resultBuilder.build());
    }

    /**
     * Выполняет извлечение значений полей МЕТА-полей
     *
     * @param metaFields коллекция МЕТА-полей
     * @return Возвращает коллекцию значений полей
     */
    public static Collection<Field> extractFieldsFromMetaFields(Collection<MetaField> metaFields) {
        ImmutableList.Builder<Field> fields = ImmutableList.builder();
        for (MetaField metaField : metaFields) {
            fields.addAll(metaField.getDescendants());
        }
        return fields.build();
    }

    /**
     * Выполняет извлечение значений полей из записей
     *
     * @param records коллекция записей справочника
     * @return Возвращает коллекцию значений полей
     */
    public static Collection<Field> extractFieldsFromRecords(Collection<Record> records) {
        ImmutableList.Builder<Field> fields = ImmutableList.builder();
        for (Record object : records) {
            if (!CollectionUtils.isEmpty(object.getFields())) {
                fields.addAll(object.getFields().values());
            }
        }
        return Lists.newArrayList(fields.build());
    }

    /**
     * Выполняет формирование коллекци групп справочников из справочников
     *
     * @param dictionaries коллекция справочников с заполненными группами
     * @return Возвращает коллекцию групп справочников
     */
    public static Collection<Group> extractGroups(Collection<Dictionary> dictionaries) {
        Map<Group, Collection<Dictionary>> group2dictionaries = Multimaps.index(dictionaries, Accessors.GROUP_TO_DICTIONARY_INJECTOR).asMap();
        for (Map.Entry<Group, Collection<Dictionary>> entry : group2dictionaries.entrySet()) {
            entry.getKey().setDescendants(entry.getValue());
        }
        return Lists.newArrayList(group2dictionaries.keySet());
    }

    /**
     * Рассчитывает и возвращает идентификатор записи на основе значения первичного ключа
     *
     * @param field значение первичного ключа записи
     * @return Возвращает идентификатор записи
     */
    public static String createRecordId(Field field) {
        return MD5.asHex(StringUtils.lowerCase(formatFieldValue(field)));
    }

    /**
     * Выполняет форматирование значения поля
     *
     * @param field значение поля
     * @return Возвращает отформатированное значение или <code>NULL</code>, если поле имеет пустую длинну
     */
    public static String formatFieldValue(Field field) {
        return !StringUtils.isEmpty(field.getValue()) ?
                field.getValue() :
                null;
    }

    /**
     * Выпоняет установку битов в значение <code>1</code>
     *
     * @param current текущие значение
     * @param target целевые значения
     * @return Возвращает модифицированное значение
     */
    public static int establishBits(int current, int... target) {
        int result = current;
        for (int f : target) {
            result = result | f;
        }
        return result;
    }

    /**
     * Выполняет установку битов в значение <code>0</code>
     *
     * @param current текущие значение
     * @param target целевые значения
     * @return Возвращает модифицированное значение
     */
    public static int resetBits(int current, int... target) {
        int result = current;
        for (int f : target) {
            result = result & ~f;
        }
        return result;
    }

    /**
     * Выполняет установку бита в указанное значение
     *
     * @param value значение
     * @param current текущие значение битов
     * @param targetBit целевой бит
     * @return Возвращает модифицированное значение
     */
    public static int changeBit(boolean value, int current, int targetBit) {
        return value ? establishBits(current, targetBit) : resetBits(current, targetBit);
    }

    /**
     * Проверяет и возвращает <code>TRUE</code>, если целевой бит установлен в <code>1</code>
     *
     * @param current текущее значение битов
     * @param targetBit проверяемый бит
     * @return Возвращает флаг проверки
     */
    public static boolean isBitEstablished(int current, int targetBit) {
        return (current & targetBit) == targetBit;
    }

    /**
     * Проверяет в возвращает <code>TRUE</code>, если указанный объект обладает указанными правами
     *
     * @param target проверяемый объект
     * @param targetPermission проверяемые наборы прав
     * @return Возвращает флаг проверки
     */
    public static <T extends Permissioned> boolean hasPermission(T target, int targetPermission) {
        return target != null && isBitEstablished(target.getPermissions(), targetPermission);
    }

    /**
     * Создает и возвращает именованный путь для справочника
     *
     * @param dictionary целевой справочник
     * @return Возвращает именованный путь справочника
     */
    public static DictionaryNamedPath createDictionaryNamedPath(Dictionary dictionary) {
        Assert.notNull(dictionary, "Dictionary must not be NULL");
        Assert.notNull(dictionary.getRelative(), "Group must not be NULL");
        return new DictionaryNamedPath(dictionary.getRelative().getName(), dictionary.getName());
    }

    /**
     * Создает и возвращает именованный путь для МЕТА-поля
     *
     * @param metaField целевое МЕТА-поле
     * @return Возвращает именованный путь МЕТА-поля
     */
    public static MetaFieldNamedPath createMetaFieldNamedPath(MetaField metaField) {
        Assert.notNull(metaField, "Meta field must not be NULL");
        return new MetaFieldNamedPath(createDictionaryNamedPath(metaField.getRelative()), metaField.getName());
    }

    /**
     * Создает и возвращает именованный путь значения поля
     *
     * @param field целевое значение поля
     * @return Возвращает именованный поть значения поля
     */
    public static FieldNamedPath createFieldNamedPath(Field field) {
        return field != null ?
                new FieldNamedPath(createMetaFieldNamedPath(field.getRelative()), field.getValue()) :
                null;
    }

    /**
     * Создает и возвращает ключ, уникально идентифицирующий правило
     *
     * @param rule правило
     * @return Возвращает уникальный ключ правила
     */
    public static <NP extends MetaFieldNamedPath> DirectionNamedPath<NP> createRulePath(Rule<NP, ?, ?> rule) {
        Assert.notNull(rule.getFromNamedPath(), String.format("From path must not be NULL"));
        Assert.notNull(rule.getToNamedPath(), String.format("To path must not be NULL"));
        return new DirectionNamedPath<>(rule.getFromNamedPath(), rule.getToNamedPath());
    }

    /**
     * Создает коллекцию именованных путей по коллекции правил
     *
     * @param rules коллекция правил
     * @return Возвращает коллекцию именованных путей
     */
    public static <NP extends MetaFieldNamedPath, R extends Rule<NP, ?, R>> Set<NP> createNamedPath(Collection<R> rules) {
        ImmutableSet.Builder<NP> builder = ImmutableSet.builder();
        for (Rule<NP, ?, ?> rule : rules) {
            builder.add(rule.getFromNamedPath());
            builder.add(rule.getToNamedPath());
        }
        return builder.build();
    }

    /**
     * Выполняет валидацию именованного пути справочника на предмет заполненности полей
     *
     * @param path именованный путь
     * @throws IllegalArgumentException Исключительная ситуация валидации
     */
    public static DictionaryNamedPath validateDictionaryNamedPath(DictionaryNamedPath path) throws IllegalArgumentException {
        Assert.notNull(path, "Named path must not be NULL");
        Assert.isTrue(StringUtils.isNotEmpty(path.getGroupName()), "Group name must not be NULL or EMPTY");
        Assert.isTrue(StringUtils.isNotEmpty(path.getDictionaryName()), "Dictionary name must not be NULL or EMPTY");
        return path;
    }

    /**
     * Выполняет валидацию именованного пути МЕТА-поля на предмет заполненности полей
     *
     * @param path именованный путь
     * @throws IllegalArgumentException Исключительная ситуация валидации
     */
    public static MetaFieldNamedPath validateMetaFieldNamedPath(MetaFieldNamedPath path) throws IllegalArgumentException {
        validateDictionaryNamedPath(path);
        Assert.isTrue(StringUtils.isNotEmpty(path.getFieldName()), "Field name must not be NULL or EMPTY");
        return path;
    }

    /**
     * Возвращает интервал дат начала и окончания агрегации событий
     *
     * @param notifications коллекция событий
     * @return Возвращает интервал дат
     */
    public static DateInterval createNotificationInterval(Collection<Notification> notifications) {
        Date minStartDate = null;
        Date maxEndDate = null;
        for (Notification notification : notifications) {
            if (minStartDate == null || minStartDate.after(notification.getStartDate())) {
                minStartDate = notification.getStartDate();
            }
            if (maxEndDate == null || maxEndDate.before(notification.getEndDate())) {
                maxEndDate = notification.getEndDate();
            }
        }
        return new DateInterval(minStartDate, maxEndDate);
    }
}
