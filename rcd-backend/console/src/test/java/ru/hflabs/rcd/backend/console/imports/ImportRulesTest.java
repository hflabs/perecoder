package ru.hflabs.rcd.backend.console.imports;

import com.beust.jcommander.ParameterException;
import com.google.common.collect.Lists;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.hflabs.rcd.backend.console.RunTemplateTest;
import ru.hflabs.rcd.backend.console.imports.handlers.ImportDescriptor;
import ru.hflabs.rcd.backend.console.imports.handlers.rule.ImportRulesCommand;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;

import static org.testng.Assert.*;

public class ImportRulesTest extends RunTemplateTest<ImportRulesCommand> {

    private static final String DIRECTORY = "rules";
    private static final String ALPHA_BETA = "alpha_beta";
    private static final String ALPHA_BETA_PATH = DIRECTORY + File.separator + ALPHA_BETA + ".csv";
    private static final String BETA_ALPHA = "beta_alpha";
    private static final String BETA_ALPHA_PATH = DIRECTORY + File.separator + BETA_ALPHA + ".csv";

    /** Сервис сравнения наборов правил перекодирования */
    private static final Comparator<RecodeRuleSet> FROM_DICTIONARY_COMPARATOR = new Comparator<RecodeRuleSet>() {

        @Override
        public int compare(RecodeRuleSet o1, RecodeRuleSet o2) {
            return o1.getFromNamedPath().getGroupName().compareTo(o2.getFromNamedPath().getGroupName());
        }
    };

    /** Приложение загрузки правил перекодирования */
    @Resource(name = "importRules")
    private ImportRules importRules;

    @BeforeClass
    @Override
    protected void prepareManagerServiceStub() {
        Mockito.when(managerService.storeRecodeRuleSets(Mockito.<Collection<RecodeRuleSet>>any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArguments()[0];
            }
        });
    }

    @AfterClass
    @Override
    protected void purgeManagerServiceStub() {
        super.purgeManagerServiceStub();
    }

    @Override
    protected ImportRulesCommand createPreferenceInstance() throws Exception {
        return new ImportRulesCommand();
    }

    @Test(
            description = "Тест проверяет наличие обязательно параметра: путь к файлу",
            expectedExceptions = ParameterException.class
    )
    public void test_missingFile() throws Exception {
        importRules.importDocuments(createBasePreference());
    }

    @Test(
            description = "Тест проверяет наличие обязательно параметра: название группы источника"
    )
    public void test_missingFromGroupName() throws Exception {
        ImportRulesCommand preference = createBasePreference(BETA_ALPHA_PATH);
        {
            preference.setToGroupName("beta");
        }
        ImportDescriptor<RecodeRuleSet> descriptor = importRules.importDocuments(createBasePreference(ALPHA_BETA_PATH));
        assertEquals(descriptor.getErrors().size(), 1);
        assertEquals(descriptor.getErrors().iterator().next().getClass(), ParameterException.class);
    }

    @Test(
            description = "Тест проверяет наличие обязательно параметра: название группы приемника"
    )
    public void test_missingToGroupName() throws Exception {
        ImportRulesCommand preference = createBasePreference(BETA_ALPHA_PATH);
        {
            preference.setFromGroupName("alpha");
        }
        ImportDescriptor<RecodeRuleSet> descriptor = importRules.importDocuments(preference);
        assertEquals(descriptor.getErrors().size(), 1);
        assertEquals(descriptor.getErrors().iterator().next().getClass(), ParameterException.class);
    }

    private static RecodeRuleSet assertRuleSet(
            RecodeRuleSet ruleSet,
            String expectedFromGroupName, String expectedToGroupName,
            String expectedFromDictionaryName,
            String expectedToDictionaryName,
            int expectedRulesCount) {
        // Именованный путь источника
        assertEquals(ruleSet.getFromNamedPath().getGroupName(), expectedFromGroupName);
        assertEquals(ruleSet.getFromNamedPath().getDictionaryName(), expectedFromDictionaryName);
        // Именованный путь назначения
        assertEquals(ruleSet.getToNamedPath().getGroupName(), expectedToGroupName);
        assertEquals(ruleSet.getToNamedPath().getDictionaryName(), expectedToDictionaryName);
        // Коллекция правил
        assertEquals(ruleSet.getRecodeRules().size(), expectedRulesCount);
        // Возвращаем проверенный набор правил
        return ruleSet;
    }

    private static RecodeRuleSet assertRuleSets(
            Collection<RecodeRuleSet> ruleSets,
            String expectedFromGroupName, String expectedToGroupName,
            String expectedFromDictionaryName, String expectedToDictionaryName,
            int expectedRulesCount) {
        assertNotNull(ruleSets);
        assertEquals(ruleSets.size(), 1);
        return assertRuleSet(ruleSets.iterator().next(), expectedFromGroupName, expectedToGroupName, expectedFromDictionaryName, expectedToDictionaryName, expectedRulesCount);
    }

    @Test(
            description = "Тест проверяет создание набора правил из файла с параметрами по умолчанию"
    )
    public void testFile1() throws Exception {
        ImportRulesCommand preference = createBasePreference(ALPHA_BETA_PATH);
        {
            preference.setFromGroupName("alpha");
            preference.setToGroupName("beta");
        }
        ImportDescriptor<RecodeRuleSet> descriptor = importRules.importDocuments(preference);
        assertFalse(descriptor.hasErrors());
        RecodeRuleSet ruleSet = assertRuleSets(descriptor.getDocuments(), "alpha", "beta", ALPHA_BETA, ALPHA_BETA, 3);
        assertNull(ruleSet.getDefaultPath());
    }

    @Test(
            description = "Тест проверяет создание набора правил из файла с перекрытыми параметрами"
    )
    public void testFile2() throws Exception {
        ImportRulesCommand preference = createBasePreference(BETA_ALPHA_PATH);
        {
            preference.setFromGroupName("beta");
            preference.setToGroupName("alpha");
            preference.setFromDictionaryName("someDictionary");
            preference.setDefaultValue("");
        }
        ImportDescriptor<RecodeRuleSet> descriptor = importRules.importDocuments(preference);
        assertFalse(descriptor.hasErrors());
        RecodeRuleSet ruleSet = assertRuleSets(descriptor.getDocuments(), "beta", "alpha", "someDictionary", "someDictionary", 2);
        assertNotNull(ruleSet.getDefaultPath());
        assertEquals(ruleSet.getDefaultPath().getGroupName(), "alpha");
        assertEquals(ruleSet.getDefaultPath().getDictionaryName(), "someDictionary");
        assertNull(ruleSet.getDefaultPath().getFieldValue());
    }

    @Test(
            description = "Тест проверяет создание набора правил из файла с указанием целевого справочника"
    )
    public void testFile3() throws Exception {
        ImportRulesCommand preference = createBasePreference(BETA_ALPHA_PATH);
        {
            preference.setFromGroupName("beta");
            preference.setToGroupName("alpha");
            preference.setFromDictionaryName("someDictionary");
            preference.setToDictionaryName("toDictionary");
            preference.setDefaultValue("");
        }
        ImportDescriptor<RecodeRuleSet> descriptor = importRules.importDocuments(preference);
        assertFalse(descriptor.hasErrors());
        RecodeRuleSet ruleSet = assertRuleSets(descriptor.getDocuments(), "beta", "alpha", "someDictionary", "toDictionary", 2);
        assertNotNull(ruleSet.getDefaultPath());
        assertEquals(ruleSet.getDefaultPath().getGroupName(), "alpha");
        assertEquals(ruleSet.getDefaultPath().getDictionaryName(), "toDictionary");
        assertNull(ruleSet.getDefaultPath().getFieldValue());
    }

    @DataProvider
    private Iterator<Object[]> createDirectoryCases() {
        return Lists.<Object[]>newArrayList(
                new Object[]{null, DIRECTORY},
                new Object[]{"someDictionary", "someDictionary"}
        ).iterator();
    }

    @Test(
            dataProvider = "createDirectoryCases",
            description = "Тест проверяет создание набора правил из директории"
    )
    public void testDirectory(String dictionaryName, String expectedDictionaryName) throws Exception {
        ImportRulesCommand preference = createBasePreference(DIRECTORY);
        {
            preference.setFromDictionaryName(dictionaryName);
        }
        ImportDescriptor<RecodeRuleSet> descriptor = importRules.importDocuments(preference);
        assertFalse(descriptor.hasErrors());

        List<RecodeRuleSet> ruleSets = Lists.newArrayList(descriptor.getDocuments());
        assertNotNull(ruleSets);
        assertEquals(ruleSets.size(), 2);
        Collections.sort(ruleSets, FROM_DICTIONARY_COMPARATOR);
        {
            RecodeRuleSet ruleSet = assertRuleSet(ruleSets.get(0), "alpha", "beta", expectedDictionaryName, expectedDictionaryName, 3);
            assertNull(ruleSet.getDefaultPath());
        }
        {
            RecodeRuleSet ruleSet = assertRuleSet(ruleSets.get(1), "beta", "alpha", expectedDictionaryName, expectedDictionaryName, 2);
            assertNull(ruleSet.getDefaultPath());
        }
    }
}
