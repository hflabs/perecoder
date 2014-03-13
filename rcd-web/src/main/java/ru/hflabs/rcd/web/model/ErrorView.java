package ru.hflabs.rcd.web.model;

import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.ModelAndView;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Класс <class>ErrorModel</class> реализует модель, которая содержит информацию о результате совершенного действия
 *
 * @see org.springframework.ui.Model
 * @see org.springframework.web.servlet.View
 */
@XmlRootElement(name = ErrorView.ERROR_VIEW_NAME)
public class ErrorView extends ExtendedModelMap {

    private static final long serialVersionUID = 9107384145985381450L;

    /** Название view ошибки */
    public static final String ERROR_VIEW_NAME = "error";

    public ErrorView() {
        this(new ErrorBean());
    }

    public ErrorView(ErrorBean errors) {
        addAttribute(ErrorBean.GLOBAL_ERRORS, errors.getGlobalErrors());
        addAttribute(ErrorBean.FIELD_ERRORS, errors.getFieldErrors());
    }

    /**
     * Создает и возвращает {@link ModelAndView} ошибки
     *
     * @return Возвращает созданный {@link ModelAndView}
     */
    public ModelAndView asView() {
        return new ModelAndView(ERROR_VIEW_NAME, this);
    }
}
