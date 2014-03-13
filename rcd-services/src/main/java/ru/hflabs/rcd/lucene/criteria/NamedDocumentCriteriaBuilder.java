package ru.hflabs.rcd.lucene.criteria;

import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.Named;

import javax.swing.*;

/**
 * Класс <class>NamedDocumentCriteriaBuilder</class> реализует сервис построения критерий для именованных документов
 *
 * @author Nazin Alexander
 */
public class NamedDocumentCriteriaBuilder<T extends Identifying & Named> extends LuceneCriteriaBuilder<T> {

    @Override
    protected LuceneCriteriaHolder appendDefaultOrder(LuceneCriteriaHolder current, Class<T> criteriaClass) {
        // Сортировка по имени документа
        current = appendOrder(current, criteriaClass, T.NAME, SortOrder.ASCENDING);
        // Сортировка по умолчанию
        current = super.appendDefaultOrder(current, criteriaClass);

        return current;
    }
}
