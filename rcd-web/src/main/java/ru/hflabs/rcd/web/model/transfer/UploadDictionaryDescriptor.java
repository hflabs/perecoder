package ru.hflabs.rcd.web.model.transfer;

import lombok.Getter;
import lombok.Setter;
import ru.hflabs.rcd.backend.console.imports.handlers.dictionary.ImportDictionariesCommand;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static ru.hflabs.rcd.model.Descriptioned.DESCRIPTION_SIZE;
import static ru.hflabs.rcd.model.Identifying.PRIMARY_KEY_MAX_SIZE;
import static ru.hflabs.rcd.model.Named.NAME_MAX_SIZE;
import static ru.hflabs.rcd.model.Named.NAME_MIN_SIZE;

/**
 * Класс <class>UploadDictionaryDescriptor</class> реализует декоратор, содержащий информацию о настройках загрузки справочника
 *
 * @see ImportDictionariesCommand
 */
@Getter
@Setter
public class UploadDictionaryDescriptor extends TransferDictionaryDescriptor<ImportDictionariesCommand> {

    private static final long serialVersionUID = 8299083546729242250L;

    /** Идентификатор группы справочника */
    @NotNull
    @Size(max = PRIMARY_KEY_MAX_SIZE)
    private String groupId;

    public UploadDictionaryDescriptor() {
        super(new ImportDictionariesCommand());
    }

    @NotNull
    @Size(min = NAME_MIN_SIZE, max = NAME_MAX_SIZE)
    public String getName() {
        return delegate.getDictionaryName();
    }

    public void setName(String dictionaryName) {
        delegate.setDictionaryName(dictionaryName);
    }

    @Size(max = DESCRIPTION_SIZE)
    public String getDescription() {
        return delegate.getDictionaryDescription();
    }

    public void setDescription(String dictionaryDescription) {
        delegate.setDictionaryDescription(dictionaryDescription);
    }
}
