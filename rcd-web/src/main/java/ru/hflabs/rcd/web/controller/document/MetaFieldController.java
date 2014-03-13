package ru.hflabs.rcd.web.controller.document;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.document.MetaField;
import ru.hflabs.rcd.service.document.IFieldService;
import ru.hflabs.rcd.service.document.IMetaFieldService;
import ru.hflabs.rcd.web.controller.ControllerTemplate;
import ru.hflabs.rcd.web.model.document.MetaFieldBean;

import javax.annotation.Resource;
import java.util.Collection;

import static ru.hflabs.rcd.accessor.Accessors.*;

/**
 * Класс <class>MetaFieldController</class> реализует контроллер управления структурой справочника
 *
 * @see MetaField
 */
@Controller(MetaFieldController.MAPPING_URI + MetaFieldController.NAME_POSTFIX)
@RequestMapping(MetaFieldController.MAPPING_URI + MetaFieldController.DATA_URI)
public class MetaFieldController extends ControllerTemplate {

    public static final String MAPPING_URI = "metafields";

    /** Сервис работы с МЕТА-полями справочника */
    @Resource(name = "metaFieldService")
    private IMetaFieldService metaFieldService;
    /** Сервис работы со значениями полей справочника */
    @Resource(name = "fieldService")
    private IFieldService fieldService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public Collection<MetaFieldBean> getMetaFields(@RequestParam(value = MetaField.DICTIONARY_ID) String dictionaryId) {
        return Collections2.transform(
                metaFieldService.findAllByRelativeId(dictionaryId, null, true),
                MetaFieldBean.CONVERT
        );
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ResponseBody
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public MetaFieldBean createMetaField(@RequestBody MetaFieldBean bean) {
        MetaField target = bean.getDelegate();
        // Выполняем создание МЕТА-поля
        MetaField result = createSingleDocument(metaFieldService, injectId(target, null), true);
        // Выполняем создание пустых значений полей
        MetaField primaryMetaField = metaFieldService.findPrimaryMetaField(result.getDictionaryId(), false, true);
        if (primaryMetaField != null) {
            ImmutableList.Builder<Field> fields = ImmutableList.builder();
            // Получаем значение первичных ключей
            Collection<Field> primaryFields = fieldService.findAllByRelativeId(primaryMetaField.getId(), null, false);
            // Подготавливаем фиктивные значение полей
            for (Field primaryField : primaryFields) {
                Field field = new Field();
                {
                    field = linkRelative(result, field);
                    field = injectName(field, primaryField.getName());
                }
                fields.add(field);
            }
            // Создаем фиктивные значений полей
            fieldService.create(fields.build(), false);
        }
        // Возвращаем созданное МЕТА-поле
        return MetaFieldBean.CONVERT.apply(result);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public MetaFieldBean updateMetaField(@PathVariable String id, @RequestBody MetaFieldBean metaFieldBean) {
        return MetaFieldBean.CONVERT.apply(
                updateSingleDocument(metaFieldService, injectId(metaFieldBean.getDelegate(), id), true)
        );
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void closeMetaField(@PathVariable String id) {
        metaFieldService.closeByIDs(Sets.newHashSet(id));
    }
}
