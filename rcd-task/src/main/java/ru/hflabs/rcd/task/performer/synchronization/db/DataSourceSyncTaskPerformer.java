package ru.hflabs.rcd.task.performer.synchronization.db;

import com.google.common.collect.ImmutableList;
import lombok.Setter;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;
import ru.hflabs.rcd.connector.db.DataSourceConnector;
import ru.hflabs.rcd.connector.db.model.DataSourceRevisionEntity;
import ru.hflabs.rcd.exception.transfer.CommunicationException;
import ru.hflabs.rcd.model.ModelUtils;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.path.DictionaryNamedPath;
import ru.hflabs.rcd.model.task.TaskProgress;
import ru.hflabs.rcd.model.task.TaskResultStatus;
import ru.hflabs.rcd.service.IServiceFactory;
import ru.hflabs.rcd.service.MergeServices;
import ru.hflabs.rcd.task.performer.TaskProgressHolder;
import ru.hflabs.rcd.task.performer.synchronization.SynchronizationResult;
import ru.hflabs.rcd.task.performer.synchronization.SynchronizationTaskPerformer;
import ru.hflabs.rcd.task.performer.synchronization.SynchronizeCallback;
import ru.hflabs.rcd.term.Condition;
import ru.hflabs.util.security.SecurityUtil;

import java.util.*;

import static ru.hflabs.rcd.accessor.Accessors.linkDescendants;
import static ru.hflabs.rcd.accessor.Accessors.linkRelative;

/**
 * Класс <class>DataSourceSyncTaskPerformer</class> реализует задачу синхронизации с внешней БД
 *
 * @author Nazin Alexander
 */
@Setter
public class DataSourceSyncTaskPerformer extends SynchronizationTaskPerformer<DataSourceSyncParameters> {

    /** Фабрика создания сервис работы с БД */
    private IServiceFactory<DataSourceConnector, DataSourceSyncParameters> dataSourceServiceFactory;

    @Override
    public Class<DataSourceSyncParameters> retrieveParameterClass() {
        return DataSourceSyncParameters.class;
    }

    /**
     * Выполняет синхронизация <b>закрытого</b> справочника
     *
     * @param context контекст синхронизации
     * @param documentsHistory дескриптор изменений документов
     * @param dictionary целевой справочник
     * @return Возвращает модифицированный дескриптор изменений документов
     */
    private DocumentsHistory doSynchronizeClosed(TaskProgressHolder context, DocumentsHistory documentsHistory, Dictionary dictionary) {
        // Формируем дескриптор изменения записей справочника
        documentsHistory = doSynchronizeRecords(
                context,
                documentsHistory,
                dictionary,
                new SynchronizeEmptyRecordCallback()
        );
        // Возвращаем модифицированный дескриптор изменений
        return documentsHistory;
    }

    /**
     * Выполняет синхронизация <b>актуального</b> справочника
     *
     * @param context контекст синхронизации
     * @param documentsHistory дескриптор изменений документов
     * @param dictionary целевой справочник
     * @return Возвращает модифицированный дескриптор изменений документов
     */
    private DocumentsHistory doSynchronizeChanged(TaskProgressHolder context, DocumentsHistory documentsHistory, Dictionary dictionary) {
        // Формируем дескриптор изменения записей справочника
        documentsHistory = doSynchronizeRecords(
                context,
                documentsHistory,
                dictionary,
                new SynchronizeRecordCallback() {
                    @Override
                    public SynchronizeCallback<MetaField> getMetaFields(final Dictionary dictionary) {
                        // Связываем измененные МЕТА-поля со справочником
                        final Collection<MetaField> targetMetaFields = linkDescendants(dictionary.getDescendants(), dictionary);
                        // Возвращаем провайдер синхронизации
                        return new SynchronizeCallback.Adapter<>(
                                MetaField.class,
                                targetMetaFields,
                                ModelUtils.<MetaField>lowerNameFunction(),
                                MergeServices.chain(
                                        MergeServices.<MetaField>copyId(),
                                        MergeServices.<MetaField>copyName(),
                                        MergeServices.<MetaField>copyDescription()
                                )
                        );
                    }

                    @Override
                    public SynchronizeCallback<Field> getFields(MetaField metaField) {
                        // Связываем измененные значения полей с МЕТА-полем
                        final Collection<Field> targetFields = linkDescendants(metaField.getDescendants(), metaField);
                        // Возвращаем провайдер синхронизации
                        return new SynchronizeCallback.Adapter<>(
                                Field.class,
                                targetFields,
                                ModelUtils.<Field>nameFunction(),
                                MergeServices.<Field>copyId()
                        );

                    }
                }
        );
        // Возвращаем модифицированный дескриптор изменений
        return documentsHistory;
    }

    /**
     * Выполняет синхронизацию справочника
     *
     * @param context контекст синхронизации
     * @param connector сервис работы с БД
     * @param entity описание справочника
     * @return Возвращает дескриптор изменений документов
     */
    private SynchronizationResult.Dictionary doSynchronizeDictionary(TaskProgressHolder context, DataSourceConnector connector, DataSourceRevisionEntity entity) {
        DocumentsHistory documentsHistory = new DocumentsHistory();
        // Получаем существующий справочник по его именованному пути
        Dictionary oldDictionary = dictionaryService.findUniqueByNamedPath(new DictionaryNamedPath(entity.getGroupName(), entity.getDictionaryName()), true);
        // Получаем существующую группу из найденного справочника или по ее имени
        Group oldGroup = (oldDictionary != null) ?
                oldDictionary.getRelative() :
                groupService.findUniqueByNamedPath(entity.getGroupName(), true);

        // Формируем новый справочник
        Dictionary newDictionary = connector.findUniqueByNamedPath(entity, false);
        // Формируем новую группу
        Group newGroup = (newDictionary != null) ?
                newDictionary.getRelative() :
                null;

        // Проверяем, что хотя бы один из справочников существует
        if (oldDictionary == null && newDictionary == null) {
            return new SynchronizationResult.Dictionary(TaskResultStatus.SKIPPED, entity.getGroupName(), entity.getDictionaryName());
        }

        // Формируем дескриптор изменений группы
        if (newGroup != null) {
            // Проверяем, что новая группа не имеет конфликтов с существующими
            checkGroupsCollision(Arrays.asList(newGroup), Condition.AND);
            // Выполняем актуализацию группы
            newGroup = MergeServices.chain(
                    MergeServices.<Group>copyId(),
                    MergeServices.<Group>copyDescription()
            ).merge(newGroup, oldGroup);
            // Формируем дескриптор изменения группы
            documentsHistory.groups.addChange(
                    historyService.createChangeHistory(
                            context.date, context.author, oldGroup, newGroup
                    )
            );
            // Формируем связь группы и справочника
            newDictionary = linkRelative(newGroup, newDictionary);
        } else if (oldGroup != null) {
            // Проверяем, что старая группа не имеет конфликтов с существующими
            checkGroupsCollision(Arrays.asList(oldGroup), Condition.AND);
        }

        // Выполняем актуализацию справочника
        newDictionary = MergeServices.<Dictionary>copyId().merge(newDictionary, oldDictionary);
        // Формируем дескриптор изменения справочника
        documentsHistory.dictionaries.addChange(
                historyService.createChangeHistory(
                        context.date, context.author, oldDictionary, newDictionary
                )
        );
        // Формируем историю изменения записей
        documentsHistory = (newDictionary != null) ?
                doSynchronizeChanged(context, documentsHistory, newDictionary) :
                doSynchronizeClosed(context, documentsHistory, oldDictionary);
        // Сохраняем модифицированные документы
        doSaveChanges(context, documentsHistory);
        // Формируем статус синхронизации
        return new SynchronizationResult.Dictionary(TaskResultStatus.FINISHED, entity.getGroupName(), entity.getDictionaryName());
    }

    /**
     * Выполняет синхронизацию справочников с БД
     *
     * @param connector сервис работы с БД
     * @return Возвращает результат синхронизации
     */
    private SynchronizationResult doSynchronize(DataSourceConnector connector) throws Exception {
        final TaskProgressHolder context = new TaskProgressHolder(new Date(), SecurityUtil.getCurrentUserName(), 3);

        // Получаем коллекцию описаний справочников
        changeProgress(context.nextStep(), "Retrieve dictionaries definitions", "dictionaries");
        List<DataSourceRevisionEntity> entities = connector.getAll();

        // Для каждого справочника выполняем построение изменений
        if (isCancelled()) {
            return null;
        }
        changeProgress(context.nextStep(), "Building difference with current dictionaries", "difference");
        final ImmutableList.Builder<SynchronizationResult.Dictionary> successResult = ImmutableList.builder();
        final ImmutableList.Builder<SynchronizationResult.Dictionary> errorResult = ImmutableList.builder();

        TaskProgressHolder innerProgress = new TaskProgressHolder(entities.size(), context);
        for (Iterator<DataSourceRevisionEntity> iterator = entities.iterator(); !isCancelled() && iterator.hasNext(); ) {
            DataSourceRevisionEntity entity = iterator.next();
            changeProgress(innerProgress.nextStep(), "Updating records for {0}.{1}", "records.update", entity.getGroupName(), entity.getDictionaryName());
            try {
                successResult.add(doSynchronizeDictionary(innerProgress, connector, entity));
            } catch (Throwable th) {
                LOG.error(String.format(
                        "Can't synchronize dictionary '%s.%s'. Cause by: %s", entity.getGroupName(), entity.getDictionaryName(), th.getMessage()),
                        th
                );
                errorResult.add(new SynchronizationResult.Dictionary(entity.getGroupName(), entity.getDictionaryName(), th));
            }
        }

        // Формирование результатов
        changeProgress(context.nextStep(), "Building results", "finished");
        return new SynchronizationResult(successResult.build(), errorResult.build());
    }

    @Override
    protected synchronized SynchronizationResult doPerformTask(DataSourceSyncParameters parameters) throws Throwable {
        Assert.notNull(parameters.getJdbcUrl(), "Target JDBC URL not properly configured");
        changeProgress(TaskProgress.INFINITE_PROGRESS, "Create connection to {0}", "connection", parameters.getJdbcUrl());
        DataSourceConnector connector = dataSourceServiceFactory.retrieveService(parameters);
        try {
            return doSynchronize(connector);
        } catch (DataAccessResourceFailureException ex) {
            throw new CommunicationException(ex.getCause().getMessage(), ex.getCause());
        } finally {
            dataSourceServiceFactory.destroyService(parameters, connector);
        }
    }
}
