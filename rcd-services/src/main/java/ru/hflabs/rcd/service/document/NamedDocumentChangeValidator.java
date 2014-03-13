package ru.hflabs.rcd.service.document;

import org.springframework.validation.Errors;
import ru.hflabs.rcd.exception.constraint.DuplicateNameException;
import ru.hflabs.rcd.exception.search.UnknownNamedDocumentException;
import ru.hflabs.rcd.model.Descriptioned;
import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.Named;
import ru.hflabs.rcd.service.IFindService;
import ru.hflabs.util.core.EqualsUtil;

import static ru.hflabs.rcd.accessor.Accessors.injectTrimmedDescription;
import static ru.hflabs.rcd.accessor.Accessors.injectTrimmedName;

/**
 * Класс <class>NamedDocumentValidator</class> реализует базовый сервис валидации именованного документа
 *
 * @author Nazin Alexander
 */
public abstract class NamedDocumentChangeValidator<T extends Identifying & Named & Descriptioned, S extends IFindService<T>> extends ExistedDocumentChangeValidator<T> {

    /** Сервис работы с именованными документами */
    private S documentService;
    /** Класс исключения при несуществующем документе */
    private final Class<? extends UnknownNamedDocumentException> unknownNamedDocumentClass;

    public NamedDocumentChangeValidator(Class<T> targetClass, boolean mustExist, Class<? extends UnknownNamedDocumentException> unknownNamedDocumentClass) {
        super(targetClass, mustExist);
        this.unknownNamedDocumentClass = unknownNamedDocumentClass;
    }

    public void setDocumentService(S documentService) {
        this.documentService = documentService;
    }

    public S getDocumentService() {
        return documentService;
    }

    @Override
    protected T formatValue(T target) {
        target = super.formatValue(target);
        target = injectTrimmedName(target, target.getName());
        target = injectTrimmedDescription(target, target.getDescription());
        return target;
    }

    @Override
    protected T findExisted(T target, boolean exist) {
        return exist ?
                documentService.findByID(target.getId(), false, true) :
                findUniqueByName(target);
    }

    /**
     * Выполняет поиск документа по уникальному имени
     *
     * @param target проверяемый документ
     * @return Возвращает существующий документ, или <code>NULL</code>, если документа не существует
     */
    protected abstract T findUniqueByName(T target);

    @Override
    protected void validateNewToOld(Errors errors, T newObject, T oldObject) {
        newObject.setId(oldObject.getId());
        // Если название документа изменилось, то проверяем его уникальность
        if (!EqualsUtil.lowerCaseEquals(newObject.getName(), oldObject.getName())) {
            T existed = findUniqueByName(newObject);
            if (existed != null) {
                rejectExisted(errors, newObject, existed, false);
            }
        }
    }

    @Override
    protected void rejectExisted(Errors errors, T target, T existed, boolean mustExist) {
        errors.rejectValue(
                T.NAME,
                mustExist ? unknownNamedDocumentClass.getSimpleName() : DuplicateNameException.class.getSimpleName(),
                new Object[]{target.getName()},
                String.format("%s with name '%s' %s exist", retrieveTargetClass().getSimpleName(), target.getName(), mustExist ? "not" : "already")
        );
    }
}
