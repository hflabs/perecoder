package ru.hflabs.rcd.exception.search.rule;

import ru.hflabs.rcd.exception.search.document.UnknownDocumentException;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;

/**
 * Класс <class>UnknownRuleException</class> реализует исключительную ситуацию, возникающую, если невозможно найти правило
 *
 * @see UnknownDocumentException
 */
public abstract class UnknownRuleException extends UnknownDocumentException {

    private static final long serialVersionUID = 9216236516000083265L;

    /** Именованный путь источника */
    private MetaFieldNamedPath fromPath;
    /** Именованный путь назначения */
    private MetaFieldNamedPath toPath;

    public UnknownRuleException(String message) {
        super(message);
    }

    public UnknownRuleException(MetaFieldNamedPath fromPath, MetaFieldNamedPath toPath) {
        this(String.format("Rules from '%s' to '%s' not found", fromPath, toPath));
        this.fromPath = fromPath;
        this.toPath = toPath;
    }

    @Override
    public Object[] getExceptionParameters() {
        return new Object[]{fromPath, toPath};
    }
}
