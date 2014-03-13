package ru.hflabs.rcd.backend.console.imports.handlers.rule;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.io.FilenameUtils;
import ru.hflabs.rcd.backend.console.imports.handlers.ImportDescriptor;
import ru.hflabs.rcd.model.path.MetaFieldNamedPath;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;
import ru.hflabs.rcd.service.IManagerService;
import ru.hflabs.util.core.EqualsUtil;
import ru.hflabs.util.spring.Assert;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс <class>ImportRulesOverFile</class> реализует процессор импорта правил перекодирования из директории
 *
 * @author Nazin Alexander
 */
public class ImportRulesOverDirectory extends ImportRulesOverFile {

    /** Регулярное выражение названия группы */
    private static final String GROUP_NAME = "[a-zа-яё\\d]+";
    /** Регулярное выражение получения названий групп */
    private static final Pattern GROUPS_PATTERN = Pattern.compile(
            String.format("(%s)_(%s)", GROUP_NAME, GROUP_NAME), Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE
    );

    @Override
    public ImportDescriptor<RecodeRuleSet> processImport(ImportRulesCommand preference, IManagerService managerService) {
        ImmutableList.Builder<Throwable> errorsBuilder = ImmutableList.builder();
        ImmutableList.Builder<RecodeRuleSet> builder = ImmutableList.builder();

        final File directory = preference.retrieveTargetFile();
        for (File file : directory.listFiles(createFileFilter(preference.getFileType()))) {
            try {
                String fileName = FilenameUtils.getBaseName(file.getPath());
                Matcher matcher = GROUPS_PATTERN.matcher(fileName);
                Assert.isTrue(matcher.find(), String.format("File name '%s' doesn't match pattern '%s'", fileName, GROUPS_PATTERN.pattern()));

                // Формируем именованные пути справочников
                final MetaFieldNamedPath fromPath = createDocumentNamedPath(matcher.group(1), preference.getFromDictionaryName(), directory.getPath());
                final MetaFieldNamedPath toPath = createDocumentNamedPath(matcher.group(2), preference.getFromDictionaryName(), directory.getPath());
                Assert.isTrue(
                        !EqualsUtil.equals(fromPath, toPath),
                        String.format("Mapping from dictionary '%s' to itself is not allowed", fromPath),
                        IllegalArgumentException.class
                );

                // Формируем набор правил перекодирования
                RecodeRuleSet ruleSet = createRecodeRuleSet(
                        fromPath, toPath, null,
                        retrieveDocumentConnector(file.getPath(), preference.getFileType()),
                        ImportRulesCommand.createConnectorPreference(file.getPath(), preference)
                );
                builder.addAll(managerService.storeRecodeRuleSets(Lists.newArrayList(ruleSet)));
            } catch (Throwable th) {
                errorsBuilder.add(createThrowable(file.getPath(), th));
            }
        }

        return new ImportDescriptor<RecodeRuleSet>(builder.build(), errorsBuilder.build());
    }
}
