package ru.hflabs.rcd.lucene.directory;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SingleInstanceLockFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import ru.hflabs.rcd.lucene.LuceneDirectoryFactory;

import java.io.File;
import java.io.IOException;

/**
 * Класс <class>FileDirectoryFactory</class> реализует фабрику директорий индекса на основе файловой системы
 *
 * @author Nazin Alexander
 */
public class LuceneFileDirectoryFactory implements InitializingBean, LuceneDirectoryFactory {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    /** Базовая директория индексов */
    private File location;

    public void setLocation(File location) {
        this.location = location;
    }

    @Override
    public Directory retrieveDirectory(String name) throws IOException {
        return (location != null) ?
                FSDirectory.open(new File(location, name), new SingleInstanceLockFactory()) :
                new RAMDirectory();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (location != null) {
            Assert.isTrue(location.isDirectory() || location.mkdirs(), "Directory '%s' must have read and write permissions");
        } else {
            LOG.info("Index location not properly configured. All indexes will be stored in memory");
        }
    }
}
