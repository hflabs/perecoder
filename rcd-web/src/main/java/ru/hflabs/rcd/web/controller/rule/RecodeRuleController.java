package ru.hflabs.rcd.web.controller.rule;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import ru.hflabs.rcd.model.document.Field;
import ru.hflabs.rcd.model.rule.RecodeRule;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;
import ru.hflabs.rcd.service.document.IFieldService;
import ru.hflabs.rcd.service.rule.IRecodeRuleService;
import ru.hflabs.rcd.service.rule.IRecodeRuleSetService;
import ru.hflabs.rcd.web.controller.ControllerTemplate;
import ru.hflabs.rcd.web.model.rule.RecodeRuleRequestBean;
import ru.hflabs.rcd.web.model.rule.RecodeRuleResponseBean;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static ru.hflabs.rcd.accessor.Accessors.FROM_RULE_INJECTOR;
import static ru.hflabs.rcd.accessor.Accessors.TO_RULE_INJECTOR;
import static ru.hflabs.rcd.model.ModelUtils.*;

/**
 * Класс <class>RecodeRuleController</class> реализует контроллер управления правилами перекодирования
 *
 * @see RecodeRule
 */
@Controller(RecodeRuleController.MAPPING_URI + RecodeRuleController.NAME_POSTFIX)
@RequestMapping(RecodeRuleController.MAPPING_URI + RecodeRuleController.DATA_URI)
public class RecodeRuleController extends ControllerTemplate {

    public static final String MAPPING_URI = "recodes";

    /** Сервис работы со значениями полей */
    @Resource(name = "fieldService")
    private IFieldService fieldService;
    /** Сервис работы с наборами правил перекодирования */
    @Resource(name = "recodeRuleSetService")
    private IRecodeRuleSetService recodeRuleSetService;
    /** Сервис работы с правилами перекодирования */
    @Resource(name = "recodeRuleService")
    private IRecodeRuleService recodeRuleService;

    @RequestMapping(value = "/{rrsId}", method = RequestMethod.GET)
    @ResponseBody
    public Collection<RecodeRuleResponseBean> getRecodeRules(@PathVariable String rrsId, @RequestParam(value = "recordIDs") Set<String> fromRecordIDs) {
        RecodeRuleSet ruleSet = recodeRuleSetService.findByID(rrsId, false, false);
        // Определяем исходных значения полей по идентификаторам записей
        Collection<Field> fromFields = fieldService.findByNames(ruleSet.getFromFieldId(), fromRecordIDs, false);
        // Определяем правила
        Collection<RecodeRule> rules = !CollectionUtils.isEmpty(fromFields) ?
                recodeRuleService.findAllByFieldIDs(ruleSet.getId(), Collections2.transform(fromFields, ID_FUNCTION), true) :
                Collections.<RecodeRule>emptyList();
        // Формируем декораторы
        return Collections2.transform(rules, RecodeRuleResponseBean.CONVERT);
    }

    /**
     * Выполняет модификацию правил для указанных полей
     *
     * @param ruleSet набор правил перекодирования
     * @param fromFields коллекция исходных полей
     * @param toField целевое поле
     * @return Возвращает коллекция модифицированных правил
     */
    private Collection<RecodeRule> doModifyRecodeRules(RecodeRuleSet ruleSet, Collection<Field> fromFields, Field toField) {
        ImmutableSet.Builder<RecodeRule> toCreate = ImmutableSet.builder();
        ImmutableSet.Builder<RecodeRule> toUpdate = ImmutableSet.builder();
        ImmutableSet.Builder<RecodeRule> toClose = ImmutableSet.builder();
        // Получаем все существующие правила
        Map<String, RecodeRule> existedRules = Maps.newHashMap(Maps.uniqueIndex(
                recodeRuleService.findAllByFieldIDs(ruleSet.getId(), Collections2.transform(fromFields, ID_FUNCTION), true),
                FROM_RULE_FIELD_ID
        ));
        // Для каждого исходного значения определяем действие по модификации
        for (Field fromField : fromFields) {
            RecodeRule existed = existedRules.remove(ID_FUNCTION.apply(fromField));
            if (existed == null && toField != null) {
                RecodeRule rule = new RecodeRule().injectRecodeRuleSet(ruleSet);
                rule = FROM_RULE_INJECTOR.inject(rule, fromField);
                rule = TO_RULE_INJECTOR.inject(rule, toField);
                toCreate.add(rule);
            } else if (existed != null && toField != null) {
                toUpdate.add(TO_RULE_INJECTOR.inject(existed, toField));
            } else if (existed != null) {
                toClose.add(existed);
            }
        }
        // Выполняем модификацию
        return recodeRuleService.modify(toCreate.build(), toUpdate.build(), toClose.build(), false);
    }

    @RequestMapping(value = "/{rrsId}", method = RequestMethod.POST)
    @ResponseBody
    public Collection<RecodeRuleResponseBean> modifyRecodeRules(@PathVariable String rrsId, @Valid @RequestBody RecodeRuleRequestBean request) {
        final RecodeRuleSet ruleSet = recodeRuleSetService.findByID(rrsId, false, false);
        // Определяем целевое поле
        final Field toField = StringUtils.hasText(request.getToRecordId()) ?
                fieldService.findUniqueByRelativeId(ruleSet.getToFieldId(), request.getToRecordId(), true, false) :
                null;
        // Определяем исходные поля по идентификаторам записей
        Collection<Field> fieldByIDs = fieldService.findByNames(ruleSet.getFromFieldId(), request.getFromRecordIDs(), false);
        // Если поля найдены, то определяем все существующие поля с такими же значениями
        if (!CollectionUtils.isEmpty(fieldByIDs)) {
            Collection<Field> fieldByValues = fieldService.findByValues(ruleSet.getFromFieldId(), Sets.newHashSet(Collections2.transform(fieldByIDs, FIELD_VALUE)), true);
            // Выполняем модификацию
            Collection<RecodeRule> changed = doModifyRecodeRules(ruleSet, fieldByValues, toField);
            // Формируем декораторы
            return Collections2.transform(changed, new Function<RecodeRule, RecodeRuleResponseBean>() {
                @Override
                public RecodeRuleResponseBean apply(RecodeRule input) {
                    return RecodeRuleResponseBean.CONVERT.apply(input.injectTo(toField));
                }
            });
        } else {
            return Collections.emptyList();
        }
    }
}
