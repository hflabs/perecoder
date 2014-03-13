package ru.hflabs.rcd.web.model;

import lombok.Getter;
import lombok.Setter;
import ru.hflabs.rcd.model.criteria.FilterCriteria;
import ru.hflabs.util.core.FormatUtil;

import javax.swing.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

/**
 * Класс <class>PageRequestBean</class> реализует класс, содержащий информацию о запрашиваемой страницы объектов
 *
 * @see FilterCriteria
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.PROPERTY)
public class PageRequestBean implements Serializable {

    private static final long serialVersionUID = 6823481588460871997L;

    /** Целевая страница по умолчанию */
    public static final int DEFAULT_PAGE = 1;

    /** Предпочтительный размер страницы */
    @Max(FilterCriteria.COUNT_DEFAULT)
    private Integer pageSize = null;
    /** Целевая страница */
    @Min(DEFAULT_PAGE)
    @Max(Integer.MAX_VALUE)
    private Integer page = DEFAULT_PAGE;
    /** Строка поиска */
    private String search;
    /** Ключ сортировки */
    private String sortOrderKey;
    /** Значение сортировки */
    private SortOrder sortOrderValue = SortOrder.UNSORTED;

    public void setPage(Integer page) {
        this.page = (page != null) ? page : DEFAULT_PAGE;
    }

    public void setSearch(String search) {
        this.search = FormatUtil.parseString(search);
    }

    public void setSortOrderKey(String sortOrderKey) {
        this.sortOrderKey = FormatUtil.parseString(sortOrderKey);
    }

    public FilterCriteria createFilterCriteria() {
        return new FilterCriteria()
                .injectSearch(search)
                .injectSort(sortOrderKey, sortOrderValue);
    }
}
