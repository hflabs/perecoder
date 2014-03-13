package ru.hflabs.rcd.backend.console.imports.handlers.rule;

import com.beust.jcommander.ParameterException;
import com.google.common.collect.Lists;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.StringUtils;
import ru.hflabs.rcd.backend.console.imports.handlers.ImportProcessorOverFileTemplate;
import ru.hflabs.rcd.connector.files.FilesConnectorConfiguration;
import ru.hflabs.rcd.model.path.FieldNamedPath;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;
import ru.hflabs.rcd.model.rule.RecodeRule;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;
import ru.hflabs.rcd.service.IDocumentConnector;
import ru.hflabs.rcd.service.IManagerService;
import ru.hflabs.util.core.EqualsUtil;
import ru.hflabs.util.core.FormatUtil;
import ru.hflabs.util.spring.Assert;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Класс <class>ImportRulesOverFile</class> реализует процессор импорта правил перекодирования из файла
 *
 * @author Nazin Alexander
 */
public class ImportRulesOverFile extends ImportProcessorOverFileTemplate<ImportRulesCommand, RecodeRuleSet> {

    /**
     * Создает и возвращает именнованный путь справочника
     *
     * @param groupName название группы справочников
     * @param dictionaryName название справочника
     * @param pathToFile путь к файлу
     * @return Возвращает именнованный путь справочника
     */
    protected MetaFieldNamedPath createDocumentNamedPath(String groupName, String dictionaryName, String pathToFile) {
        return new MetaFieldNamedPath(
                groupName,
                StringUtils.hasText(dictionaryName) ? dictionaryName : FilenameUtils.getBaseName(pathToFile),
                null
        );
    }

    /**
     * Формирует набор правил перекодирования
     *
     * @param fromPath именованный путь источника
     * @param toPath именнованный путь назначения
     * @param defaultValue значение по умолчанию
     * @param documentConnector сервис создания документов
     * @param connectorPreference настройки чтения файла
     * @return Возвращает набор правил перекодирования
     */
    protected RecodeRuleSet createRecodeRuleSet(
            MetaFieldNamedPath fromPath, MetaFieldNamedPath toPath, String defaultValue,
            IDocumentConnector<FilesConnectorConfiguration, List<File>> documentConnector, FilesConnectorConfiguration connectorPreference) {
        final Collection<RecodeRule> recodeRules = documentConnector.readRecodeRules(connectorPreference).getContent();

        String fromFieldName = null;
        String toFieldName = null;

        // Определяем название полей источника, приемника и переопределяем именованный путь справочника для сформированный правил
        for (RecodeRule rule : recodeRules) {
            rule.injectFromNamedPath(
                    new FieldNamedPath(
                            fromPath,
                            rule.getFromNamedPath().getFieldName(),
                            rule.getFromNamedPath().getFieldValue()
                    )
            );
            rule.injectToNamedPath(
                    new FieldNamedPath(
                            toPath,
                            rule.getToNamedPath().getFieldName(),
                            rule.getToNamedPath().getFieldValue()
                    )
            );

            if (fromFieldName == null) {
                fromFieldName = rule.getFromNamedPath().getFieldName();
            } else {
                Assert.isTrue(EqualsUtil.equals(fromFieldName, rule.getFromNamedPath().getFieldName()));
            }

            if (toFieldName == null) {
                toFieldName = rule.getToNamedPath().getFieldName();
            } else {
                Assert.isTrue(EqualsUtil.equals(toFieldName, rule.getToNamedPath().getFieldName()));
            }
        }

        // Формируем результирующий набор правил
        final RecodeRuleSet ruleSet = new RecodeRuleSet()
                .injectFromNamedPath(new MetaFieldNamedPath(fromPath.getGroupName(), fromPath.getDictionaryName(), fromFieldName))
                .injectToNamedPath(new MetaFieldNamedPath(toPath.getGroupName(), toPath.getDictionaryName(), toFieldName));
        {
            ruleSet.setRecodeRules(recodeRules);
            if (toFieldName != null && defaultValue != null) {
                ruleSet.setDefaultPath(
                        new FieldNamedPath(ruleSet.getToNamedPath(), toFieldName, FormatUtil.parseString(defaultValue))
                );
            }
        }

        return ruleSet;
    }

    @Override
    protected Collection<RecodeRuleSet> doConvert(ImportRulesCommand preference, IManagerService managerService) throws Exception {
        Assert.isTrue(StringUtils.hasText(preference.getFromGroupName()), "Missing source group name parameter", ParameterException.class);
        Assert.isTrue(StringUtils.hasText(preference.getToGroupName()), "Missing destination group name parameter", ParameterException.class);

        MetaFieldNamedPath fromPath = createDocumentNamedPath(
                preference.getFromGroupName(),
                preference.getFromDictionaryName(),
                preference.getTargetPath()
        );
        MetaFieldNamedPath toPath = createDocumentNamedPath(
                preference.getToGroupName(),
                StringUtils.hasText(preference.getToDictionaryName()) ? preference.getToDictionaryName() : preference.getFromDictionaryName(),
                preference.getTargetPath()
        );

        RecodeRuleSet ruleSet = createRecodeRuleSet(
                fromPath, toPath, preference.getDefaultValue(),
                retrieveDocumentConnector(preference.getTargetPath(), preference.getFileType()),
                createConnectorPreference(preference)
        );

        return managerService.storeRecodeRuleSets(Lists.newArrayList(ruleSet));
    }
}
