package ru.hflabs.rcd.service.document;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.hflabs.rcd.event.modify.ChangeEvent;
import ru.hflabs.rcd.model.Essence;
import ru.hflabs.rcd.model.Historical;
import ru.hflabs.rcd.model.ModelUtils;
import ru.hflabs.rcd.model.change.*;
import ru.hflabs.rcd.model.criteria.FilterActivity;
import ru.hflabs.rcd.model.criteria.FilterCriteria;
import ru.hflabs.rcd.service.IDocumentService;
import ru.hflabs.rcd.service.IHistoryService;
import ru.hflabs.rcd.service.IValidateService;
import ru.hflabs.util.core.EqualsUtil;
import ru.hflabs.util.core.Pair;
import ru.hflabs.util.lucene.LuceneModifierUtil;
import ru.hflabs.util.spring.Assert;

import java.util.*;

import static ru.hflabs.rcd.model.CriteriaUtils.createCriteriaByIDs;
import static ru.hflabs.rcd.model.ModelUtils.ID_FUNCTION;
import static ru.hflabs.rcd.service.ServiceUtils.checkFoundDocuments;
import static ru.hflabs.rcd.service.ServiceUtils.publishChangeEvent;

/**
 * Класс <class>DocumentServiceTemplate</class> реализует базовый сервис работы с документами
 *
 * @author Nazin Alexander
 */
public class DocumentServiceTemplate<E extends Essence & Historical> extends FilterDocumentServiceTemplate<E> implements IDocumentService<E> {

    /** Сервис работы с историей документов */
    protected IHistoryService historyService;

    /** Сервис валидации документов при создании */
    protected IValidateService<E> createValidator;
    /** Сервис валидации документов при обновлении */
    protected IValidateService<E> updateValidator;
    /** Сервис валидации документов при закрытии */
    protected IValidateService<E> closeValidator;

    public DocumentServiceTemplate(Class<E> targetClass) {
        super(targetClass);
    }

    public void setHistoryService(IHistoryService historyService) {
        this.historyService = historyService;
    }

    public void setCreateValidator(IValidateService<E> createValidator) {
        this.createValidator = createValidator;
    }

    public void setUpdateValidator(IValidateService<E> updateValidator) {
        this.updateValidator = updateValidator;
    }

    public void setCloseValidator(IValidateService<E> closeValidator) {
        this.closeValidator = closeValidator;
    }

    /**
     * Выполняет оповещение слушателей об изменениях
     *
     * @param descriptor дескриптор изменений
     * @param types целевые типы событий
     */
    protected void doPublishEvent(HistoryBuilder<E> descriptor, ChangeType... types) {
        publishChangeEvent(eventPublisher, this, descriptor, types);
    }

    @Override
    protected Collection<E> handleSelfRestoreEvent(Collection<E> changed) {
        doModify("restore", LuceneModifierUtil.createUpdateCallback(indexManager, binderTransformer, changed));
        return changed;
    }

    @Override
    protected Collection<E> handleSelfCloseEvent(Collection<E> changed) {
        doModify("close", LuceneModifierUtil.createUpdateCallback(indexManager, binderTransformer, changed));
        return changed;
    }

    /**
     * Выполняет заполнение истории для документа
     *
     * @param object целевой документ
     * @return Возвращает модифицированный документ с заполненной историей
     */
    protected E injectHistory(E object) {
        if (object != null) {
            object.setHistory(historyService.findByID(object.getHistoryId(), false, false));
        }
        return object;
    }

    /**
     * Выполняет заполнение истории документов
     *
     * @param objects коллекцию документов
     * @return Возвращает коллекцию документов с заполненной историей
     */
    protected Collection<E> injectHistory(Collection<E> objects) {
        if (!CollectionUtils.isEmpty(objects)) {
            // Формируем карту соответствий идентификатора истории к объекту
            Map<String, E> historyId2objects = Maps.uniqueIndex(objects, ModelUtils.HISTORY_ID_FUNCTION);
            // Заполняем историю для документов
            Collection<History> histories = historyService.findByIDs(historyId2objects.keySet(), false, false);
            for (History history : histories) {
                historyId2objects.get(history.getId()).setHistory(history);
            }
        }
        return objects;
    }

    @Override
    protected Collection<E> injectTransitiveDependencies(Collection<E> objects) {
        return super.injectTransitiveDependencies(injectHistory(objects));
    }

    /**
     * Выполняет создание документов
     *
     * @param objects коллекция документов для создания
     * @param validateService сервис валидации перед создаеним
     * @return Возвращает коллекцию созданных документов
     */
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    protected Collection<E> doCreate(Collection<E> objects, IValidateService<E> validateService) {
        final HistoryBuilder<E> changeDescriptor = new HistoryBuilder<E>(retrieveTargetClass());
        for (E object : objects) {
            object = (validateService != null) ? validateService.validate(object) : object;
            changeDescriptor.addChange(historyService.createChangeHistory(null, object));
        }
        doPublishEvent(changeDescriptor);
        return changeDescriptor.getEssences();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public Collection<E> create(Collection<E> objects, boolean needValidation) {
        if (!CollectionUtils.isEmpty(objects)) {
            return doCreate(objects, needValidation ? createValidator : null);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Выполняет обновление документов
     *
     * @param newObjects коллекцию документов для обновления
     * @param oldObjects коллекция существующих документов
     * @param validateService сервис валидации перед обновлением
     * @return Возвращает обновленную коллекцию документов
     */
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    protected Collection<E> doUpdate(Map<String, E> newObjects, Map<String, E> oldObjects, IValidateService<E> validateService) {
        final Collection<E> toSelfUpdate = Lists.newArrayList();
        final HistoryBuilder<E> changeDescriptor = new HistoryBuilder<E>(retrieveTargetClass());

        for (Map.Entry<String, E> entry : newObjects.entrySet()) {
            E oldValue = oldObjects.get(entry.getKey());
            Assert.notNull(oldValue, String.format("Can't find '%s' with ID '%s'", retrieveTargetClassName(), entry.getKey()));

            E newValue = (validateService != null) ? validateService.validate(entry.getValue()) : entry.getValue();
            Pair<ChangeType, E> change = historyService.createChangeHistory(oldValue, newValue);

            // Если персистентные поля не изменились, то проверяем транзитивные поля
            if (ChangeType.SKIP.equals(change.first) && !EqualsUtil.equals(oldValue, newValue)) {
                toSelfUpdate.add(change.second);
            }
            changeDescriptor.addChange(change);
        }

        doPublishEvent(changeDescriptor);
        if (!CollectionUtils.isEmpty(toSelfUpdate)) {
            handleSelfUpdateEvent(toSelfUpdate);
        }

        return changeDescriptor.getEssences();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public Collection<E> update(Collection<E> newObjects, Collection<E> oldObjects, boolean needValidation) {
        return !CollectionUtils.isEmpty(newObjects) ?
                doUpdate(Maps.uniqueIndex(newObjects, ID_FUNCTION), Maps.uniqueIndex(oldObjects, ID_FUNCTION), needValidation ? updateValidator : null) :
                Collections.<E>emptyList();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public Collection<E> update(Collection<E> objects, boolean needValidation) {
        return !CollectionUtils.isEmpty(objects) ?
                update(objects, findByIDs(Sets.newHashSet(Collections2.transform(objects, ID_FUNCTION)), true, false), needValidation) :
                Collections.<E>emptyList();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public Collection<E> closeByCriteria(FilterCriteria criteria) {
        return doClose(findAllByCriteria(criteria, false), null);
    }

    /**
     * Выполняет закрытие документов
     *
     * @param existed коллекция существующих документов
     * @param validator сервис валидации перед закрытием
     * @return Возвращает обновленную коллекцию документов
     */
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    protected Collection<E> doClose(Collection<E> existed, IValidateService<E> validator) {
        final HistoryBuilder<E> changeDescriptor = new HistoryBuilder<>(retrieveTargetClass());
        for (E object : existed) {
            object = (validator != null) ? validator.validate(object) : object;
            changeDescriptor.addChange(historyService.createChangeHistory(object, null));
        }
        doPublishEvent(changeDescriptor);
        return changeDescriptor.getEssences();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public Collection<E> close(Collection<E> objects, boolean needValidation) {
        return !CollectionUtils.isEmpty(objects) ?
                doClose(objects, needValidation ? closeValidator : null) :
                Collections.<E>emptyList();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public void closeByIDs(Set<String> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            doClose(findAllByCriteria(createCriteriaByIDs(E.PRIMARY_KEY, ids), false), closeValidator);
        }
    }

    /**
     * Выполняет переоткрытие документов
     *
     * @param restoreDate дата, на которую производится восстановление объектов
     * @param closedObjects коллекция документов для переоткрытия
     * @return Возвращает актуальные документы
     */
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    protected Collection<E> doReopen(Date restoreDate, Collection<E> closedObjects) {
        ChangeSet<E> changeSet = new ChangeSet<>(retrieveTargetClass(), ChangeType.RESTORE, ChangeMode.DEFAULT);
        for (E object : closedObjects) {
            E validated = createValidator.validate(object);
            changeSet.appendChange(historyService.createRestoreHistory(validated));
        }
        eventPublisher.publishEvent(new ChangeEvent(this, changeSet));
        return changeSet.getChanged();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public Collection<E> reopen(Set<String> ids) {
        Assert.isTrue(!CollectionUtils.isEmpty(ids), "Restore IDs must not be NULL or EMPTY");
        // Получаем коллекцию восстаналиваемых документов
        Collection<E> documents = findAllByCriteria(
                createCriteriaByIDs(E.PRIMARY_KEY, ids).injectActivity(FilterActivity.ALL),
                true
        );
        checkFoundDocuments(retrieveTargetClass(), ids, documents, false);
        // Отбираем те документы, для которых последним событием было закрытие
        Collection<E> closedDocuments = Collections2.filter(documents, new Predicate<E>() {
            @Override
            public boolean apply(E input) {
                return ChangeType.CLOSE.equals(input.getChangeType());
            }
        });

        // Группируем документы по целевой дате восстановления
        Map<Date, Collection<E>> date2documents = Multimaps.index(closedDocuments, new Function<E, Date>() {
            @Override
            public Date apply(E input) {
                return input.getChangeDate();
            }
        }).asMap();
        ImmutableList.Builder<E> resultBuilder = ImmutableList.builder();

        // Выполняем восстановление документов по датам
        for (Map.Entry<Date, Collection<E>> entry : date2documents.entrySet()) {
            resultBuilder.addAll(doReopen(entry.getKey(), entry.getValue()));
        }
        // Возвращаем результирующую коллекцию
        throw new UnsupportedOperationException("Not implement yet.");
    }
}
