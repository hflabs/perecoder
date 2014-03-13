package ru.hflabs.rcd.connector.db;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.connector.db.converter.DataSourceDictionaryConverter;
import ru.hflabs.rcd.connector.db.model.DataSourceRevisionEntity;
import ru.hflabs.rcd.exception.constraint.CollisionDataException;
import ru.hflabs.rcd.exception.transfer.IncompleteDataException;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.service.INamedPathService;
import ru.hflabs.rcd.service.IStorageService;
import ru.hflabs.util.core.collection.IteratorUtil;
import ru.hflabs.util.jdbc.JDBCUtil;
import ru.hflabs.util.spring.Assert;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static ru.hflabs.rcd.accessor.Accessors.*;

/**
 * Класс <class>DataSourceRevisionEntityService</class> реализует сервис доступа к ревизиям справочников в БД
 *
 * @author Nazin Alexander
 */
public class DataSourceConnector extends JdbcTemplate implements IStorageService<DataSourceRevisionEntity>, INamedPathService<DataSourceRevisionEntity, Dictionary> {

    /** Функция преобразования строки в underscore регистр */
    private static final Function<String, String> UNDERSCORE_FUNCTION = new Function<String, String>() {
        @Override
        public String apply(String input) {
            return JDBCUtil.underscoreColumn(input);
        }
    };

    /** Шаблон запроса получения контента данных */
    private static final String DATA_QUERY_TEMPLATE = "SELECT %s FROM %s";
    /** Шаблон запроса получения количества данных */
    private static final String COUNT_QUERY_TEMPLATE = "SELECT count(*) FROM %s";

    /** Сервис получения структуры справочников из БД */
    private final RowMapper<DataSourceRevisionEntity> revisionRowMapper;
    /** Название таблицы с данными */
    private final String tableName;

    public DataSourceConnector(DataSource dataSource, String tableName) {
        super(dataSource);
        this.revisionRowMapper = new BeanPropertyRowMapper<>(DataSourceRevisionEntity.class, true);
        this.tableName = tableName;
    }

    /**
     * Формирует и возвращает запрос получения данных
     *
     * @param tocTableName название таблицы
     * @return Возвращает сформированный SQL запрос
     */
    private static String createDataSql(String tocTableName) {
        String columns = StringUtils.collectionToCommaDelimitedString(
                Collections2.transform(Arrays.asList(
                        DataSourceRevisionEntity.GROUP_NAME,
                        DataSourceRevisionEntity.DICTIONARY_NAME,
                        DataSourceRevisionEntity.DICTIONARY_DESCRIPTION,
                        DataSourceRevisionEntity.PK_NAME,
                        DataSourceRevisionEntity.FLAG,
                        DataSourceRevisionEntity.RECORDS_QUERY
                ), UNDERSCORE_FUNCTION)
        );
        return String.format(DATA_QUERY_TEMPLATE, columns, tocTableName);
    }

    @Override
    public Class<DataSourceRevisionEntity> retrieveTargetClass() {
        return DataSourceRevisionEntity.class;
    }

    @Override
    public Dictionary findUniqueByNamedPath(DataSourceRevisionEntity path, boolean quietly) {
        // Проверяем маркер описания
        if (path.getFlag() == 2) {
            return null;
        }
        Group group = injectName(new Group(), path.getGroupName());
        Dictionary dictionary = new Dictionary();
        {
            dictionary = injectName(dictionary, path.getDictionaryName());
            dictionary = injectDescription(dictionary, path.getDictionaryDescription());
            dictionary = linkRelative(group, dictionary);
        }

        return linkDescendants(dictionary, query(path.getRecordsQuery(), new DataSourceDictionaryConverter(path.getPkName())));
    }

    @Override
    public Integer totalCount() {
        return queryForObject(String.format(COUNT_QUERY_TEMPLATE, tableName), Integer.class);
    }

    @Override
    public List<DataSourceRevisionEntity> getAll() {
        List<DataSourceRevisionEntity> entities = query(createDataSql(tableName), revisionRowMapper);

        Set<DataSourceRevisionEntity> result = Sets.newLinkedHashSetWithExpectedSize(entities.size());
        for (DataSourceRevisionEntity entity : entities) {
            // Название группы справочников
            Assert.notNull(
                    entity.getGroupName(),
                    String.format("Missing group name for definition '%s'", entity),
                    IncompleteDataException.class
            );
            // Название справочника
            Assert.notNull(
                    entity.getDictionaryName(),
                    String.format("Missing dictionary name for definition '%s'", entity),
                    IncompleteDataException.class
            );
            // Если справочник актуален, то
            if (entity.getFlag() != 2) {
                // Название первичного ключа
                Assert.notNull(
                        entity.getPkName(),
                        String.format("Missing primary key name for definition '%s'", entity),
                        IncompleteDataException.class
                );
                // Запрос получения данных
                Assert.notNull(
                        entity.getRecordsQuery(),
                        String.format("Missing records query for definition '%s'", entity),
                        IncompleteDataException.class
                );
            }
            // Проверяем, что описание уникально
            Assert.isTrue(
                    result.add(entity),
                    String.format("Duplicate definition '%s'", entity),
                    CollisionDataException.class
            );
        }

        return Lists.newArrayList(result);
    }

    @Override
    public Iterator<List<DataSourceRevisionEntity>> iterateAll(int fetchSize, int cacheSize) {
        return IteratorUtil.toPageIterator(getAll().iterator(), fetchSize);
    }
}
