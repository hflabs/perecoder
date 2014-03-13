package ru.hflabs.rcd.web.model;

import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;
import java.util.Collection;

/**
 * Класс <class>PageResponseBean</class> содержит страницу объектов результатов фильтрации
 *
 * @param <T> класс объектов фильтрации
 * @see ru.hflabs.rcd.model.criteria.FilterResult
 */
@Getter
@XmlAccessorType(XmlAccessType.PROPERTY)
public class PageResponseBean<T> implements Serializable {

    private static final long serialVersionUID = -4444425417896083638L;

    /** Количество объектов на странице */
    private final int pageSize;
    /** Текущая страница */
    private final int currentPage;
    /** Общее количество страниц */
    private final int totalPages;
    /** Общее количество объектов */
    private final int totalRecords;
    /** Результат фильтрации */
    private final Collection<T> content;

    public PageResponseBean(int pageSize, int currentPage, int totalPages, int totalRecords, Collection<T> content) {
        this.pageSize = pageSize;
        this.content = content;
        this.currentPage = currentPage;
        this.totalRecords = totalRecords;
        this.totalPages = totalPages;
    }

    public int getFirstPage() {
        return PageRequestBean.DEFAULT_PAGE;
    }

    public int getLastPage() {
        return totalPages != 0 ? totalPages : getFirstPage();
    }
}
