package ru.hflabs.rcd.backend.console.imports.handlers;

import ru.hflabs.rcd.backend.console.RunDescriptor;

import java.util.Collection;
import java.util.Collections;

/**
 * Класс <class>ImportDescriptor</class> реализует дескриптор загрузки документов
 *
 * @author Nazin Alexander
 */
public class ImportDescriptor<T> extends RunDescriptor {

    /** Коллекция подготовленных документов */
    private Collection<T> documents;

    public ImportDescriptor() {
        this(Collections.<T>emptyList(), Collections.<Throwable>emptyList());
    }

    public ImportDescriptor(Collection<T> documents, Collection<Throwable> errors) {
        super(errors);
        this.documents = documents;
    }

    public Collection<T> getDocuments() {
        return documents;
    }

    public void setDocuments(Collection<T> documents) {
        this.documents = documents;
    }

    @Override
    public String describe() {
        return String.format("Imported: %d; errors %d", getDocuments().size(), getErrors().size());
    }
}
