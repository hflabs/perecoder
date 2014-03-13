package ru.hflabs.rcd.backend.console.imports;

import com.beust.jcommander.ParameterException;
import com.google.common.collect.Lists;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.hflabs.rcd.backend.console.RunTemplateTest;
import ru.hflabs.rcd.backend.console.imports.handlers.ImportDescriptor;
import ru.hflabs.rcd.backend.console.imports.handlers.dictionary.ImportDictionariesCommand;
import ru.hflabs.rcd.model.Named;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.model.document.Record;

import javax.annotation.Resource;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.testng.Assert.*;

public class ImportDictionariesTest extends RunTemplateTest<ImportDictionariesCommand> {

    private static final String DIRECTORY = "dictionaries";
    private static final String ALPHA = "alpha";
    private static final String ALPHA_PATH = DIRECTORY + File.separator + ALPHA + ".csv";
    private static final String BETA = "beta";
    private static final String BETA_PATH = DIRECTORY + File.separator + BETA + ".csv";

    /** Сервис сравнения сущностей по их имени */
    private static final Comparator<Named> NAMED_COMPARATOR = new Comparator<Named>() {

        @Override
        public int compare(Named o1, Named o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    /** Приложение загрузки справочников */
    @Resource(name = "importDictionaries")
    private ImportDictionaries importDictionaries;

    @BeforeClass
    @Override
    protected void prepareManagerServiceStub() {
        Mockito.when(managerService.storeGroups(Mockito.<Collection<Group>>any())).thenAnswer(new Answer<Object>() {
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
    protected ImportDictionariesCommand createPreferenceInstance() throws Exception {
        return new ImportDictionariesCommand();
    }

    private static void assertSingleGroup(Collection<Group> groups, String expectedGroupName, String expectedDictionaryName, int expectedRecordsCount) {
        assertNotNull(groups);
        assertEquals(groups.size(), 1);
        // Check group
        Group group = groups.iterator().next();
        assertEquals(group.getName(), expectedGroupName);
        // Check dictionary
        Collection<Dictionary> dictionaries = group.getDescendants();
        assertNotNull(dictionaries);
        assertEquals(dictionaries.size(), 1);
        Dictionary dictionary = dictionaries.iterator().next();
        assertEquals(dictionary.getName(), expectedDictionaryName);
        // Check records
        Collection<Record> records = dictionary.getRecords();
        assertNotNull(records);
        assertEquals(records.size(), expectedRecordsCount);
    }

    private static void assertDirectoryDirectGroup(Collection<Group> groups, String expectedGroupName, String... expectedDictionaryNames) {
        assertNotNull(groups);
        assertEquals(groups.size(), 1);
        // Check group
        Group group = groups.iterator().next();
        assertEquals(group.getName(), expectedGroupName);
        // Check dictionaries
        List<Dictionary> dictionaries = Lists.newArrayList(group.getDescendants());
        assertEquals(dictionaries.size(), expectedDictionaryNames.length);
        Collections.sort(dictionaries, NAMED_COMPARATOR);
        for (int i = 0, dictionariesSize = dictionaries.size(); i < dictionariesSize; i++) {
            assertEquals(dictionaries.get(i).getName(), expectedDictionaryNames[i]);
        }
    }

    private static void assertDirectoryInverseGroup(Collection<Group> groups, String expectedDictionaryName, String... expectedGroupsNames) {
        assertNotNull(groups);
        assertEquals(groups.size(), expectedGroupsNames.length);
        // Check group
        List<Group> sortedGroups = Lists.newArrayList(groups);
        Collections.sort(sortedGroups, NAMED_COMPARATOR);
        for (int i = 0, sortedGroupsSize = sortedGroups.size(); i < sortedGroupsSize; i++) {
            Group group = sortedGroups.get(i);
            assertEquals(group.getName(), expectedGroupsNames[i]);
            // Check dictionary
            Collection<Dictionary> dictionaries = group.getDescendants();
            assertNotNull(dictionaries);
            assertEquals(dictionaries.size(), 1);
            Dictionary dictionary = dictionaries.iterator().next();
            assertEquals(dictionary.getName(), expectedDictionaryName);
        }
    }

    @Test(
            description = "Тест проверяет наличие обязательно параметра: путь к файлу",
            expectedExceptions = ParameterException.class
    )
    public void test_missingFile() throws Exception {
        importDictionaries.importDocuments(createBasePreference());
    }

    @Test(
            description = "Тест проверяет наличие обязательно параметра: название группы"
    )
    public void test_missingGroupName() throws Exception {
        ImportDescriptor<Group> descriptor = importDictionaries.importDocuments(createBasePreference(ALPHA_PATH));
        assertEquals(descriptor.getErrors().size(), 1);
        assertEquals(descriptor.getErrors().iterator().next().getClass(), ParameterException.class);
    }

    @Test(
            description = "Тест проверяет создание справочника из файла, где имя справочника равно названию файла"
    )
    public void testFile1() throws Exception {
        ImportDictionariesCommand preference = createBasePreference(ALPHA_PATH);
        preference.setGroupName("testGroup");
        ImportDescriptor<Group> descriptor = importDictionaries.importDocuments(preference);
        assertFalse(descriptor.hasErrors());
        assertSingleGroup(descriptor.getDocuments(), "testGroup", ALPHA, 2);
    }

    @Test(
            description = "Тест проверяет создание справочника из файла, где имя справочника берется из переданных свойств"
    )
    public void testFile2() throws Exception {
        ImportDictionariesCommand preference = createBasePreference(BETA_PATH);
        preference.setGroupName("testGroup");
        preference.setDictionaryName("testDictionary");
        ImportDescriptor<Group> descriptor = importDictionaries.importDocuments(preference);
        assertFalse(descriptor.hasErrors());
        assertSingleGroup(descriptor.getDocuments(), "testGroup", "testDictionary", 3);
    }

    @Test(
            description = "Тест проверяет создание справочника из директории, где имя группы равно названию директории"
    )
    public void testDirectDirectory1() throws Exception {
        ImportDictionariesCommand preference = createBasePreference(DIRECTORY);
        ImportDescriptor<Group> descriptor = importDictionaries.importDocuments(preference);
        assertFalse(descriptor.hasErrors());
        assertDirectoryDirectGroup(descriptor.getDocuments(), DIRECTORY, ALPHA, BETA);
    }

    @Test(
            description = "Тест проверяет создание справочника из директории, где имя группы берется из переданных свойств"
    )
    public void testDirectDirectory2() throws Exception {
        ImportDictionariesCommand preference = createBasePreference(DIRECTORY);
        preference.setGroupName("testGroup");
        ImportDescriptor<Group> descriptor = importDictionaries.importDocuments(preference);
        assertFalse(descriptor.hasErrors());
        assertDirectoryDirectGroup(descriptor.getDocuments(), "testGroup", ALPHA, BETA);
    }

    @Test(
            description = "Тест проверяет создание справочника из директории с указанным расширением файлов"
    )
    public void testDirectDirectory3() throws Exception {
        ImportDictionariesCommand preference = createBasePreference(DIRECTORY);
        preference.setFileType("xml");
        ImportDescriptor<Group> descriptor = importDictionaries.importDocuments(preference);
        assertFalse(descriptor.hasErrors());
        assertTrue(descriptor.getDocuments().isEmpty());
    }

    @Test(
            description = "Тест проверяет создание справочника из директории, где название справочника равно названию директории"
    )
    public void testInverseDirectory1() throws Exception {
        ImportDictionariesCommand preference = createBasePreference(DIRECTORY);
        preference.setInverse(true);
        ImportDescriptor<Group> descriptor = importDictionaries.importDocuments(preference);
        assertFalse(descriptor.hasErrors());
        assertDirectoryInverseGroup(descriptor.getDocuments(), DIRECTORY, ALPHA, BETA);
    }

    @Test(
            description = "Тест проверяет создание справочника из директории, где название справочника берется из переданных свойств"
    )
    public void testInverseDirectory2() throws Exception {
        ImportDictionariesCommand preference = createBasePreference(DIRECTORY);
        preference.setInverse(true);
        preference.setDictionaryName("testDictionary");
        ImportDescriptor<Group> descriptor = importDictionaries.importDocuments(preference);
        assertFalse(descriptor.hasErrors());
        assertDirectoryInverseGroup(descriptor.getDocuments(), "testDictionary", ALPHA, BETA);
    }
}
