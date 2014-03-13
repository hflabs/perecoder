package ru.hflabs.rcd.task.performer.synchronization.db;

import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import lombok.Setter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.connector.db.DataSourceConnector;
import ru.hflabs.rcd.model.connector.db.DataSourceDescriptor;
import ru.hflabs.rcd.service.IServiceFactory;
import ru.hflabs.util.core.Holder;
import ru.hflabs.util.io.IOUtils;

import javax.sql.DataSource;
import java.io.Closeable;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Driver;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Класс <class>DataSourceConnectorFactory</class> реализует фабрику создания сервиса работы с БД
 *
 * @author Nazin Alexander
 * @see ru.hflabs.rcd.connector.db.DataSourceConnector
 * @see DataSourceSyncParameters
 */
@Setter
public class DataSourceConnectorFactory implements IServiceFactory<DataSourceConnector, DataSourceSyncParameters> {

    /** Коллекция поддерживаемых JDBC драйверов */
    private Map<String, DataSourceDescriptor> descriptors;
    /** Кеш сервисов */
    private final Holder<DataSourceSyncParameters, DataSourceConnector> serviceHolder;

    public DataSourceConnectorFactory() {
        this.descriptors = Collections.emptyMap();
        this.serviceHolder = new ConnectorHolder();
    }

    @Override
    public DataSourceConnector retrieveService(DataSourceSyncParameters key) {
        return serviceHolder.getValue(key);
    }

    @Override
    public void destroyService(DataSourceSyncParameters key, DataSourceConnector service) {
        serviceHolder.removeValue(key);
    }

    /**
     * Создает и возвращает настройки соединения
     *
     * @param entries массив настроек
     * @return Возвращает сформированные настройки
     */
    private static Properties createConnectionProperties(String[] entries) {
        Properties properties = new Properties();
        for (String entry : entries) {
            if (entry.length() > 0) {
                int index = entry.indexOf('=');
                if (index > 0) {
                    String name = entry.substring(0, index);
                    String value = entry.substring(index + 1);
                    properties.setProperty(name, value);
                } else {
                    properties.setProperty(entry, "");
                }
            }
        }
        return properties;
    }

    /**
     * Выполняет проверку существования драйвера
     *
     * @param dataSourceDescriptor описание драйвера
     */
    private void checkDriverClass(DataSourceDescriptor dataSourceDescriptor) {
        try {
            Class<?> driverClass = Class.forName(dataSourceDescriptor.getDriverClass());
            Assert.isTrue(
                    Driver.class.isAssignableFrom(driverClass),
                    String.format("Driver class '%s' is not instance of '%s'", driverClass.getName(), Driver.class.getName())
            );
        } catch (ClassNotFoundException ex) {
            throw new UndeclaredThrowableException(ex);
        }
    }

    /**
     * Выполняет конфигурацию и создание пула соединений с БД
     *
     * @param dataSourceDescriptor описания БД
     * @param parameters параметры соединения
     * @return Возвращает пул соединений с БД
     */
    private DataSource createDataSource(DataSourceDescriptor dataSourceDescriptor, DataSourceSyncParameters parameters) {
        // Проверяем, что такой драйвер зарегистрирован
        checkDriverClass(dataSourceDescriptor);
        // Выполняем конфигурацию пула соединений
        BoneCPConfig configuration = new BoneCPConfig();
        // Driver properties
        {
            String[] driverProperties = StringUtils.split(parameters.getJdbcProperties(), ";");
            if (driverProperties != null) {
                configuration.setDriverProperties(createConnectionProperties(driverProperties));
            }
        }
        // URL
        {
            configuration.setJdbcUrl(parameters.getJdbcUrl());
            configuration.setUsername(parameters.getUsername());
            configuration.setPassword(parameters.getPassword());
        }
        // Connection properties
        {
            configuration.setDefaultAutoCommit(false);
            configuration.setConnectionTimeout(parameters.getTimeout(), TimeUnit.SECONDS);
            if (StringUtils.hasText(dataSourceDescriptor.getValidateQuery())) {
                configuration.setConnectionTestStatement(dataSourceDescriptor.getValidateQuery());
            }
            configuration.setPartitionCount(1);
        }
        // Создаем и возвращаем пул
        return new BoneCPDataSource(configuration);
    }

    /**
     * Создает и возвращает сервис работы с БД
     *
     * @param parameters параметры создания сервиса
     * @return Возвращает созданный сервис
     */
    private DataSourceConnector doCreateConnector(DataSourceSyncParameters parameters) {
        // Получаем описание драйвера по его имени
        DataSourceDescriptor dataSourceDescriptor = descriptors.get(parameters.getDriverName());
        Assert.notNull(dataSourceDescriptor, String.format("Data source with name '%s' not registered", parameters.getDriverName()));
        // Создаем пул соединений
        DataSource dataSource = createDataSource(dataSourceDescriptor, parameters);
        // Получаем название таблицы с описанием справочников
        String tableName = parameters.getTocTableName();
        Assert.notNull(tableName, "Name table of content must not be NULL");
        // Создаем и возвращаем пул
        return new DataSourceConnector(dataSource, tableName);
    }

    /**
     * Выполняет освобождение ресурсов сервисом
     *
     * @param service целевой сервис
     */
    private void doDestroyConnector(DataSourceConnector service) {
        DataSource dataSource = service.getDataSource();
        if (dataSource instanceof Closeable) {
            IOUtils.closeQuietly((Closeable) dataSource);
        }
    }

    /**
     * Класс <class>ConnectorHolder</class> реализует кеш сервисов работы с БД
     *
     * @author Nazin Alexander
     */
    private class ConnectorHolder extends Holder<DataSourceSyncParameters, DataSourceConnector> {

        @Override
        protected DataSourceConnector createValue(DataSourceSyncParameters key) {
            return doCreateConnector(key);
        }

        @Override
        public DataSourceConnector removeValue(DataSourceSyncParameters key) {
            w.lock();
            try {
                DataSourceConnector connector = super.removeValue(key);
                if (connector != null) {
                    doDestroyConnector(connector);
                }
                return connector;
            } finally {
                w.unlock();
            }
        }
    }
}
