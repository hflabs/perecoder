package ru.hflabs.rcd.service;

import ru.hflabs.rcd.model.Essence;
import ru.hflabs.rcd.model.Historical;
import ru.hflabs.rcd.model.change.ChangeType;
import ru.hflabs.rcd.model.change.History;
import ru.hflabs.rcd.model.change.HistoryBuilder;
import ru.hflabs.util.core.Pair;

import java.util.Date;
import java.util.Map;

/**
 * Интерфейс <class>IHistoryService</class> декларирует методы работы с историей документов
 *
 * @see Historical
 * @see History
 */
public interface IHistoryService extends IFilterService<History> {

    /**
     * Формирует историю отмены закрытия сущности
     *
     * @param essence переоткрываемая сущность
     * @return Возвращает сущность с заполненной историей
     */
    <E extends Essence & Historical> E createRestoreHistory(E essence);

    /**
     * Формирует историю модификации для сущности, где в качестве даты выставляется время открытия транзакции, а в качестве автора - текущий пользователь
     *
     * @param oldEssence существующая сущность
     * @param newEssence обновленная сущность
     * @return Возвращает сущность с заполненной историей модификации
     */
    <E extends Essence & Historical> Pair<ChangeType, E> createChangeHistory(E oldEssence, E newEssence);

    /**
     * Формирует историю модификации для сущности
     *
     * @param date дата изменения
     * @param author автор изменения
     * @param oldEssence существующая сущность
     * @param newEssence обновленная сущность
     * @return Возвращает сущность с заполненной историей модификации
     */
    <E extends Essence & Historical> Pair<ChangeType, E> createChangeHistory(Date date, String author, E oldEssence, E newEssence);

    /**
     * Выполняет построение дескриптора изменений
     *
     * @param targetClass целевой класс сущности
     * @param existedEssences коллекция существующих сущностей
     * @param newEssences коллекция новых сущностей
     * @param mergeService сервис слияния новой и старой сущности
     * @return Возвращает построенный дескриптор
     */
    <E extends Essence & Historical> HistoryBuilder<E> createChangeSet(
            Class<E> targetClass,
            Map<String, E> existedEssences,
            Map<String, E> newEssences,
            IMergeService.Single<E> mergeService
    );

    /**
     * Выполняет построение дескриптора изменений
     *
     * @param targetClass целевой класс сущности
     * @param date дата изменения
     * @param author автор изменения
     * @param existedEssences коллекция существующих сущностей
     * @param newEssences коллекция новых сущностей
     * @param mergeService сервис слияния новой и старой сущности
     * @return Возвращает построенный дескриптор
     */
    <E extends Essence & Historical> HistoryBuilder<E> createChangeSet(
            Class<E> targetClass,
            Date date,
            String author,
            Map<String, E> existedEssences,
            Map<String, E> newEssences,
            IMergeService.Single<E> mergeService
    );
}
