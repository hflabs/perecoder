package ru.hflabs.rcd.storage.sql;

import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.change.ChangeType;
import ru.hflabs.rcd.service.IStorageService;
import ru.hflabs.rcd.storage.ChangeDocumentCallback;
import ru.hflabs.rcd.storage.ChangeServiceTemplate;
import ru.hflabs.util.core.collection.IteratorUtil;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.*;

/**
 * Класс <class>SqlStorageService</class> реализует сервис хранилища документов в реляционной СУБД
 *
 * @author Nazin Alexander
 */
public class SqlStorageService<E extends Identifying> extends ChangeServiceTemplate<E> implements IStorageService<E> {

    /** Карта процессоров модификации документов */
    private Map<ChangeType, ChangeDocumentCallback<E>> changeDocumentCallbacks;
    /** Менеджер сущностей */
    @PersistenceContext
    private EntityManager entityManager;

    public SqlStorageService(Class<E> documentClass) {
        super(documentClass);
        this.changeDocumentCallbacks = Collections.emptyMap();
    }

    public void setChangeDocumentCallbacks(Map<ChangeType, ChangeDocumentCallback<E>> changeDocumentCallbacks) {
        this.changeDocumentCallbacks = changeDocumentCallbacks;
    }

    @Override
    public Integer totalCount() {
        // Формируем запрос количества
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        query = query.select(builder.count(query.from(retrieveTargetClass())));
        // Выполняем запрос и возвращаем результат
        return entityManager.createQuery(query).getSingleResult().intValue();
    }

    @Override
    public List<E> getAll() {
        // Формируем запрос
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> query = builder.createQuery(retrieveTargetClass());
        query = query.select(query.from(retrieveTargetClass()));
        // Выполняем запрос и формируем итератор результата
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public final Iterator<List<E>> iterateAll(int fetchSize, int cacheSize) {
        return IteratorUtil.toPageIterator(getAll().iterator(), fetchSize);
    }

    /**
     * Обработку события модификации
     *
     * @param changeType тип изменения
     * @param changed коллекция изменений
     */
    protected void handleModifyEvent(ChangeType changeType, Collection<E> changed) {
        // Получаем процессор модификации
        ChangeDocumentCallback<E> callback = changeDocumentCallbacks.get(changeType);
        // Выполняем модификацию
        if (callback != null) {
            callback.afterModify(
                    callback.doModify(
                            callback.beforeModify(changed)
                    )
            );
        }
    }

    @Override
    protected Collection<E> handleSelfCreateEvent(Collection<E> changed) {
        handleModifyEvent(ChangeType.CREATE, changed);
        return changed;
    }

    @Override
    protected Collection<E> handleSelfUpdateEvent(Collection<E> changed) {
        handleModifyEvent(ChangeType.UPDATE, changed);
        return changed;
    }

    @Override
    protected Collection<E> handleSelfRestoreEvent(Collection<E> changed) {
        handleModifyEvent(ChangeType.RESTORE, changed);
        return changed;
    }

    @Override
    protected Collection<E> handleSelfCloseEvent(Collection<E> changed) {
        handleModifyEvent(ChangeType.CLOSE, changed);
        return changed;
    }
}
