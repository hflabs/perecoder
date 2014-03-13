package ru.hflabs.rcd.web.model.transfer;

import org.springframework.util.StringUtils;
import ru.hflabs.rcd.backend.console.preference.FilePreference;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * Класс <class>TransferDictionaryDescriptor</class> реализует базовый декоратор транспорта справочника
 *
 * @author Nazin Alexander
 * @see FilePreference
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
public abstract class TransferDictionaryDescriptor<D extends FilePreference> implements Serializable {

    private static final long serialVersionUID = -224529462735837210L;

    /** Настройки транспорта импорта */
    protected final D delegate;

    public TransferDictionaryDescriptor(D delegate) {
        this.delegate = delegate;
    }

    @XmlTransient
    public D getDelegate() {
        return delegate;
    }

    @NotNull
    public String getEncoding() {
        return delegate.getEncoding();
    }

    public void setEncoding(String encoding) {
        delegate.setEncoding(encoding);
    }

    @NotNull
    @Size(min = 1, max = 1)
    public String getDelimiter() {
        return String.valueOf(delegate.getDelimiter());
    }

    public void setDelimiter(String delimiter) {
        delegate.setDelimiter(StringUtils.hasText(delimiter) ? delimiter.charAt(0) : D.DEFAULT_DELIMITER);
    }

    @NotNull
    @Size(min = 1, max = 1)
    public String getQuote() {
        return String.valueOf(delegate.getQuote());
    }

    public void setQuote(String quote) {
        delegate.setQuote(StringUtils.hasText(quote) ? quote.charAt(0) : D.DEFAULT_QUOTE);
    }
}
