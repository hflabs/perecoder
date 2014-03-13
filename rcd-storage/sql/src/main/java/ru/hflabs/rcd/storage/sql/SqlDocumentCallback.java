package ru.hflabs.rcd.storage.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.hflabs.rcd.exception.constraint.IllegalPrimaryKeyException;
import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.change.ChangeType;
import ru.hflabs.rcd.storage.ChangeDocumentCallbackAdapter;
import ru.hflabs.util.core.date.StopWatch;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;

/**
 * Класс <class>SqlDocumentCallback</class> реализует базовый адаптер для изменения документов через БД
 *
 * @author Nazin Alexander
 */
public abstract class SqlDocumentCallback<E extends Identifying> extends ChangeDocumentCallbackAdapter<E> {

    private static final Logger LOG = LoggerFactory.getLogger(SqlDocumentCallback.class);
    /** Размер массовых операций по умолчанию */
    private static final int DEFAULT_BATCH_SIZE = 1000;

    /** Название операции */
    private final String operationName;
    /** Менеджер сущностей */
    @PersistenceContext
    private EntityManager entityManager;
    /** Количество объектов для массовых операций */
    private int batchSize;

    public SqlDocumentCallback(String operationName) {
        this.operationName = operationName;
        this.batchSize = DEFAULT_BATCH_SIZE;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * Выполняет операцию с документом
     *
     * @param object модифицируемый документ
     * @return Возвращает модифицированный документ
     */
    protected abstract E doWith(E object);

    @Override
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public Collection<E> doModify(Collection<E> objects) {
        int count = 0;
        StopWatch watch = new StopWatch();
        try {
            for (E object : objects) {
                doWith(object);
                if ((++count % batchSize) == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            }
        } finally {
            LOG.debug("Finished '{}' with {} objects. Time: {} sec", new Object[]{operationName, objects.size(), watch.get() / 1000D});
        }
        return objects;
    }

    /**
     * Класс <class>CreateDocumentCallback</class> реализует провайдер создания документов
     *
     * @see SqlDocumentCallback
     */
    public static final class CreateDocumentCallback<E extends Identifying> extends SqlDocumentCallback<E> {

        public CreateDocumentCallback() {
            super(ChangeType.CREATE.name());
        }

        @Override
        protected E doWith(E object) {
            try {
                getEntityManager().persist(object);
                return object;
            } catch (EntityExistsException ex) {
                throw new IllegalPrimaryKeyException(ex.getMessage(), ex.getCause());
            }
        }
    }

    /**
     * Класс <class>UpdateDocumentCallback</class> реализует провайдер обновления документов
     *
     * @see SqlDocumentCallback
     */
    public static final class UpdateDocumentCallback<E extends Identifying> extends SqlDocumentCallback<E> {

        public UpdateDocumentCallback() {
            super(ChangeType.UPDATE.name());
        }

        @Override
        protected E doWith(E object) {
            return getEntityManager().merge(object);
        }
    }

    /**
     * Класс <class>CloseDocumentCallback</class> реализует провайдер закрытия документов
     *
     * @see SqlDocumentCallback
     */
    public static final class CloseDocumentCallback<E extends Identifying> extends SqlDocumentCallback<E> {

        public CloseDocumentCallback() {
            super(ChangeType.CLOSE.name());
        }

        @Override
        protected E doWith(E object) {
            getEntityManager().remove(
                    (getEntityManager().contains(object)) ? object : getEntityManager().merge(object)
            );
            return object;
        }
    }
}
