package ru.hflabs.rcd.web.controller;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import ru.hflabs.rcd.Version;
import ru.hflabs.rcd.web.controller.document.DictionaryController;
import ru.hflabs.rcd.web.controller.document.GroupController;
import ru.hflabs.rcd.web.controller.document.MetaFieldController;
import ru.hflabs.rcd.web.controller.document.RecordController;
import ru.hflabs.rcd.web.controller.notification.NotificationController;
import ru.hflabs.rcd.web.controller.rule.RecodeRuleController;
import ru.hflabs.rcd.web.controller.rule.RecodeRuleSetController;
import ru.hflabs.rcd.web.controller.task.TaskController;

import java.util.Map;

/**
 * Класс <class>DashboardController</class> реализует контроллера работы с главной страницей системы
 *
 * @see ControllerTemplate
 */
@Controller(DashboardController.VIEW_NAME + DashboardController.NAME_POSTFIX)
@RequestMapping({
        DashboardController.MAPPING_URI,
        NotificationController.MAPPING_URI,
        GroupController.MAPPING_URI,
        DictionaryController.MAPPING_URI,
        MetaFieldController.MAPPING_URI,
        RecordController.MAPPING_URI,
        RecodeRuleSetController.MAPPING_URI,
        RecodeRuleController.MAPPING_URI,
        TaskController.MAPPING_URI
})
public class DashboardController extends ControllerTemplate {

    public static final String MAPPING_URI = "";
    public static final String VIEW_NAME = "dashboard";

    @RequestMapping({
            MAPPING_URI,
            DictionaryController.MAPPING_URI + "/{anyParameter}",
            RecordController.MAPPING_URI + "/{anyParameter}",
            RecodeRuleController.MAPPING_URI + "/{anyParameter}"
    })
    public ModelAndView createModelAndView() {
        return new ModelAndView(VIEW_NAME);
    }

    @RequestMapping(value = "/version", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> retrieveVersion() {
        return ImmutableMap.of(
                Version.VERSION, Version.getVersion(),
                Version.REVISION, Version.getRevision()
        );
    }
}
