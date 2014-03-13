package ru.hflabs.rcd.history;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.accessor.Accessors;
import ru.hflabs.rcd.event.modify.ChangeEvent;
import ru.hflabs.rcd.model.Essence;
import ru.hflabs.rcd.model.Historical;
import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.change.*;
import ru.hflabs.rcd.service.*;
import ru.hflabs.rcd.service.document.FilterDocumentServiceTemplate;
import ru.hflabs.util.core.EqualsUtil;
import ru.hflabs.util.core.Pair;
import ru.hflabs.util.io.IOUtils;
import ru.hflabs.util.security.SecurityUtil;
import ru.hflabs.util.spring.transaction.support.TransactionUtil;

import java.util.Date;
import java.util.Map;

/**
 * Класс <class>HistoryService</class> реализует сервис работы с историей документов
 *
 * @author Nazin Alexander
 */
public class HistoryService extends FilterDocumentServiceTemplate<History> implements IHistoryService {

    /** Сервис создания уникальных идентификаторов */
    private ISequenceService sequenceService;
    /** Сервис работы с изменениями */
    private IDifferenceService<Identifying> differenceService;

    public HistoryService() {
        super(History.class);
    }

    public void setSequenceService(ISequenceService sequenceService) {
        this.sequenceService = sequenceService;
    }

    public void setDifferenceService(IDifferenceService<Identifying> differenceService) {
        this.differenceService = differenceService;
    }

    /**
     * Определяет тип события в зависимости от состояния переданных сущностей
     *
     * @param oldEssence старый экземпляр сущности
     * @param newEssence новый экземпляр сущности
     * @return Возвращает определенный тип события
     */
    private <E extends Identifying & Historical> ChangeType determineChangeType(E oldEssence, E newEssence) {
        if (oldEssence == null && newEssence != null) {
            return ChangeType.CREATE;
        } else if (oldEssence != null && newEssence != null) {
            if (ChangeType.CLOSE.equals(oldEssence.getChangeType())) {
                return ChangeType.RESTORE;
            } else {
                return EqualsUtil.equals(differenceService.createHashCode(oldEssence), differenceService.createHashCode(newEssence)) ?
                        ChangeType.SKIP :
                        ChangeType.UPDATE;
            }
        } else if (oldEssence != null) {
            if (ChangeType.CLOSE.equals(oldEssence.getChangeType())) {
                return ChangeType.IGNORE;
            } else {
                return ChangeType.CLOSE;
            }
        } else {
            throw new IllegalArgumentException("Can't determine change type: old or new essence must not be NULL");
        }
    }

    /**
     * Выполняет формирование события истории для сущности
     *
     * @param changeType тип изменения
     * @param date дата события
     * @param author автор события
     * @param oldEssence существующая сущность (может быть <code>NULL</code>)
     * @param newEssence обновленная сущность
     * @return Возвращает обновленную сущность
     */
    private <E extends Identifying & Historical> E doCreateHistory(ChangeType changeType, Date date, String author, E oldEssence, E newEssence) {
        Assert.notNull(newEssence, "Target history object must not be NULL");

        final History history = sequenceService.fillIdentifier(new History(), true);

        history.setTargetId(newEssence.getId());
        history.setTargetType(newEssence.getHistoryName());

        history.setEventType(changeType);
        history.setEventDate(date);
        history.setEventAuthor(author);

        history.setDiffs(differenceService.createDiff(oldEssence, newEssence));

        newEssence.setHistory(history);
        return newEssence;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public <E extends Essence & Historical> E createRestoreHistory(E essence) {
        return doCreateHistory(
                ChangeType.RESTORE,
                TransactionUtil.getTransactionStartDate(),
                SecurityUtil.getCurrentUserName(),
                essence,
                Accessors.shallowClone(essence)
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public <E extends Essence & Historical> Pair<ChangeType, E> createChangeHistory(E oldEssence, E newEssence) {
        return createChangeHistory(TransactionUtil.getTransactionStartDate(), SecurityUtil.getCurrentUserName(), oldEssence, newEssence);
    }

    @Override
    public <E extends Essence & Historical> Pair<ChangeType, E> createChangeHistory(Date date, String author, E oldEssence, E newEssence) {
        Assert.notNull(date, "Change date must not be NULL");
        Assert.isTrue(StringUtils.hasText(author), "Change author must not be NULL or EMPTY");
        // В зависимости от типа события формируем историю модификации
        ChangeType changeType = determineChangeType(oldEssence, newEssence);
        switch (changeType) {
            case IGNORE: {
                return Pair.valueOf(changeType, oldEssence);
            }
            case SKIP: {
                newEssence.injectId(oldEssence.getId());
                newEssence.setHistory(oldEssence.getHistory());
                return Pair.valueOf(changeType, newEssence);
            }
            case CREATE: {
                newEssence = sequenceService.fillIdentifier(newEssence, false);
                // invoke UPDATE methods
            }
            case UPDATE: {
                // invoke RESTORE methods
            }
            case RESTORE: {
                return Pair.valueOf(changeType, doCreateHistory(changeType, date, author, oldEssence, newEssence));
            }
            case CLOSE: {
                return Pair.valueOf(changeType, doCreateHistory(changeType, date, author, IOUtils.deepClone(oldEssence), oldEssence));
            }
            default: {
                throw new UnsupportedOperationException(String.format("Change type '%s' not supported by '%s'", changeType.name(), getClass().getName()));
            }
        }
    }

    @Override
    public <E extends Essence & Historical> HistoryBuilder<E> createChangeSet(
            Class<E> targetClass,
            Map<String, E> existedEssences,
            Map<String, E> newEssences,
            IMergeService.Single<E> mergeService) {
        return createChangeSet(targetClass, TransactionUtil.getTransactionStartDate(), SecurityUtil.getCurrentUserName(), existedEssences, newEssences, mergeService);
    }

    @Override
    public <E extends Essence & Historical> HistoryBuilder<E> createChangeSet(Class<E> targetClass, Date date, String author, Map<String, E> existedEssences, Map<String, E> newEssences, IMergeService.Single<E> mergeService) {
        Assert.notNull(targetClass, "Target class must not be NULL");
        Assert.notNull(mergeService, "Merge service must not be NULL");
        final HistoryBuilder<E> changeDescriptor = new HistoryBuilder<E>(targetClass);

        // Выполняем сортировку сущностей по типам операций, опираясь на уникальность их имени
        for (Map.Entry<String, E> entry : newEssences.entrySet()) {
            final E oldValue = existedEssences.remove(entry.getKey());
            final E newValue = mergeService.merge(entry.getValue(), oldValue);
            changeDescriptor.addChange(createChangeHistory(date, author, oldValue, newValue));
        }

        // Для оставшихся сущностей совпадений не найдено - выполняем их закрытие
        for (E existed : existedEssences.values()) {
            changeDescriptor.addChange(createChangeHistory(date, author, existed, null));
        }

        return changeDescriptor;
    }

    @Override
    protected void handleOtherChangeEvent(ChangeEvent event) {
        if (Historical.class.isAssignableFrom(event.getChangedClass()) &&
                !ChangeType.SKIP.equals(event.getChangeType()) &&
                !ChangeType.IGNORE.equals(event.getChangeType())) {
            ChangeSet<?> otherChangeSet = event.getChangeSet();
            // Формируем набор изменений истории для создания
            ChangeSet<History> historyChangeSet = new ChangeSet<>(
                    retrieveTargetClass(),
                    ChangeType.CREATE,
                    ChangeMode.ISOLATED,
                    otherChangeSet.getChanges()
            );
            // Публикуем событие создания истории документов
            ServiceUtils.publishChangeEvent(eventPublisher, event.getSource(), historyChangeSet);
        }
    }
}
