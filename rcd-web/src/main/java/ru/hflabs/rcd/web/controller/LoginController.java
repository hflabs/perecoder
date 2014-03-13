package ru.hflabs.rcd.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Locale;

/**
 * Класс <class>LoginController</class> реализует контроллер аутентификации
 *
 * @see org.springframework.security.core.Authentication
 */
@Controller(LoginController.MAPPING_URI + ControllerTemplate.NAME_POSTFIX)
@RequestMapping(LoginController.MAPPING_URI)
public class LoginController extends ControllerTemplate {

    public static final String MAPPING_URI = "login";
    public static final String ERROR_MESSAGE = "errorMessage";

    @RequestMapping
    public ModelAndView createModelAndView(@RequestParam(required = false) String errorCode, Locale locale) {
        final ModelAndView result = new ModelAndView(MAPPING_URI);
        if (StringUtils.hasText(errorCode)) {
            result.addObject(ERROR_MESSAGE, messageSource.getMessage(errorCode, null, "Authentication failed", locale));
        }
        return result;
    }
}
