package ru.hflabs.rcd.service.document.metaField;

import org.springframework.validation.Errors;
import ru.hflabs.rcd.exception.constraint.IllegalPrimaryKeyException;
import ru.hflabs.rcd.exception.constraint.document.NotUniqueFieldsException;
import ru.hflabs.rcd.exception.search.document.UnknownMetaFieldException;
import ru.hflabs.rcd.model.ModelUtils;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;
import ru.hflabs.rcd.service.document.IDictionaryService;
import ru.hflabs.rcd.service.document.IFieldService;
import ru.hflabs.rcd.service.document.IMetaFieldService;
import ru.hflabs.rcd.service.document.NamedDocumentChangeValidator;

import static ru.hflabs.rcd.accessor.Accessors.DICTIONARY_TO_META_FIELD_INJECTOR;
import static ru.hflabs.rcd.accessor.Accessors.GROUP_TO_META_FIELD_INJECTOR;
import static ru.hflabs.rcd.model.CriteriaUtils.createCriteriaByIDs;

/**
 * Класс <class>MetaFieldValidator</class> реализует сервис валидации изменения МЕТА-полей справочника
 *
 * @author Nazin Alexander
 */
public class MetaFieldChangeValidator extends NamedDocumentChangeValidator<MetaField, IMetaFieldService> {

    /** Сервис работы со справочниками */
    private IDictionaryService dictionaryService;
    /** Сервис работы со значениями полей справочников */
    private IFieldService fieldService;

    public MetaFieldChangeValidator(boolean mustExist) {
        super(MetaField.class, mustExist, UnknownMetaFieldException.class);
    }

    public void setDictionaryService(IDictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setFieldService(IFieldService fieldService) {
        this.fieldService = fieldService;
    }

    @Override
    protected MetaField findUniqueByName(MetaField target) {
        return getDocumentService().findUniqueByRelativeId(target.getDictionaryId(), target.getName(), false, true);
    }

    /**
     * Выполняет валидацию установки {@link MetaField#FLAG_PRIMARY первичного флага}
     *
     * @param errors контейнер ошибок
     * @param newObject обновляемое МЕТА-поле
     * @param oldObject существущее МЕТА-поле или <code>NULL</code>
     */
    private void doValidatePrimaryFlag(Errors errors, MetaField newObject, MetaField oldObject) {
        boolean illegalChange = newObject.isFlagEstablished(MetaField.FLAG_PRIMARY) && (oldObject == null || !oldObject.isFlagEstablished(MetaField.FLAG_PRIMARY));
        illegalChange = illegalChange || !newObject.isFlagEstablished(MetaField.FLAG_PRIMARY) && (oldObject != null && oldObject.isFlagEstablished(MetaField.FLAG_PRIMARY));
        if (illegalChange) {
            reject(errors, new NotUniqueFieldsException(ModelUtils.createMetaFieldNamedPath(newObject)), newObject.getName());
        }
    }

    /**
     * Выполняет валидацию установки {@link MetaField#FLAG_UNIQUE уникального флага}
     *
     * @param errors контейнер ошибок
     * @param newObject обновляемое МЕТА-поле
     * @param oldObject существущее МЕТА-поле или <code>NULL</code>
     */
    private void doValidateUniqueFlag(Errors errors, MetaField newObject, MetaField oldObject) {
        if (newObject.isFlagEstablished(MetaField.FLAG_UNIQUE) && (oldObject == null || !oldObject.isFlagEstablished(MetaField.FLAG_UNIQUE))) {
            if (!fieldService.isFieldsUnique(newObject.getId())) {
                reject(errors, new NotUniqueFieldsException(ModelUtils.createMetaFieldNamedPath(newObject)), newObject.getName());
            }
        }
    }

    /**
     * Выполняет валидацию количества {@link ru.hflabs.rcd.model.document.Field значений полей}
     *
     * @param errors контейнер ошибок
     * @param metaField уникальное МЕТА-поле
     */
    private void doValidateFieldCount(Errors errors, MetaField metaField) {
        // Если МЕТА-поле помечено как уникальное и не является первичным ключем,
        // то количество полей для первичного ключа должно совпадать с количеством полей текущего МЕТА-поля
        // для корректного формирования записей справочника
        if (!metaField.isFlagEstablished(MetaField.FLAG_PRIMARY) && metaField.isFlagEstablished(MetaField.FLAG_UNIQUE)) {
            MetaFieldNamedPath metaFieldNamedPath = ModelUtils.createMetaFieldNamedPath(metaField);
            // Получаем количество значений полей по текущему полю
            int currentFieldsCount = fieldService.countByCriteria(createCriteriaByIDs(Field.META_FIELD_ID, metaField.getId()));
            // Получаем количество значений полей по первичному полю
            MetaField primaryMetaField = getDocumentService().findPrimaryMetaFieldByNamedPath(metaFieldNamedPath, false);
            int primaryFieldsCount = fieldService.countByCriteria(createCriteriaByIDs(Field.META_FIELD_ID, primaryMetaField.getId()));
            // Проверяем количество
            if (currentFieldsCount != primaryFieldsCount) {
                reject(errors, new NotUniqueFieldsException(metaFieldNamedPath), metaField.getName());
            }
        }
    }

    /**
     * Выполняет валидацию установленных флагов МЕТА-поля
     *
     * @param errors контейнер ошибок
     * @param newObject обновляемое МЕТА-поле
     * @param oldObject существущее МЕТА-поле или <code>NULL</code>
     */
    protected void doValidateFlags(Errors errors, MetaField newObject, MetaField oldObject) {
        // Проверяем первичный ключ
        if (!errors.hasErrors()) {
            doValidatePrimaryFlag(errors, newObject, oldObject);
        }
        // Проверяем количество значений полей
        if (!errors.hasErrors()) {
            doValidateFieldCount(errors, newObject);
        }
        // Проверяем уникальность
        if (!errors.hasErrors()) {
            doValidateUniqueFlag(errors, newObject, oldObject);
        }
    }

    @Override
    protected void doValidateCommon(Errors errors, MetaField target) {
        String relativeId = DICTIONARY_TO_META_FIELD_INJECTOR.applyRelativeId(target);
        try {
            DICTIONARY_TO_META_FIELD_INJECTOR.inject(target, dictionaryService.findByID(relativeId, true, false));
            doValidatePermissions(errors, GROUP_TO_META_FIELD_INJECTOR.apply(target));
        } catch (IllegalPrimaryKeyException ex) {
            reject(errors, ex, relativeId);
        }
    }

    /**
     * Класс <class>Create</class> реализует валидатор создания МЕТА-поля
     *
     * @author Nazin Alexander
     */
    public static class Create extends MetaFieldChangeValidator {

        public Create() {
            super(false);
        }

        @Override
        protected void doValidateCommon(Errors errors, MetaField target) {
            super.doValidateCommon(errors, target);
            if (!errors.hasErrors()) {
                // Устанавливаем позицию поля относительно других
                int metaFieldsCount = getDocumentService().countByCriteria(
                        createCriteriaByIDs(MetaField.DICTIONARY_ID, target.getDictionaryId())
                );
                target.setOrdinal(metaFieldsCount);
                // Проверяем флаги
                doValidateFlags(errors, target, null);
            }
        }
    }

    /**
     * Класс <class>Update</class> реализует валидатор обновления МЕТА-поля
     *
     * @author Nazin Alexander
     */
    public static class Update extends MetaFieldChangeValidator {

        public Update() {
            super(true);
        }

        @Override
        protected void validateNewToOld(Errors errors, MetaField newObject, MetaField oldObject) {
            super.validateNewToOld(errors, newObject, oldObject);
            // Восстанавливаем позицию МЕТА-поля
            newObject.setOrdinal(oldObject.getOrdinal());
            // Восстанавливаем старое значение первичного ключа
            if (oldObject.isFlagEstablished(MetaField.FLAG_PRIMARY)) {
                newObject.establishFlags(MetaField.FLAG_PRIMARY);
            }
            // Проверяем флаги
            doValidateFlags(errors, newObject, oldObject);
        }
    }
}
