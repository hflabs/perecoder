package ru.hflabs.rcd.backend.console.exports;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.hflabs.rcd.backend.console.RunTemplateTest;
import ru.hflabs.rcd.backend.console.exports.handlers.ExportDescriptor;
import ru.hflabs.rcd.backend.console.exports.handlers.dictionary.ExportDictionariesCommand;
import ru.hflabs.rcd.model.path.DictionaryNamedPath;

import javax.annotation.Resource;
import java.io.File;
import java.util.Iterator;

import static org.testng.Assert.*;
import static ru.hflabs.rcd.model.MockFactory.generateMockGroup;

public class ExportDictionariesTest extends RunTemplateTest<ExportDictionariesCommand> {

    /** Приложение экспорта справочников */
    @Resource(name = "exportDictionaries")
    private ExportDictionaries exportDictionaries;
    /** Директория работы теста */
    private File rootDirectory = new File(SystemUtils.getJavaIoTmpDir(), ExportDictionariesTest.class.getSimpleName());
    /** Директория экспорта справочников */
    private File exportDirectory;

    @BeforeMethod
    private void prepareExportDirectory() {
        exportDirectory = new File(rootDirectory, String.valueOf(System.nanoTime()));
    }

    @AfterMethod
    private void purgeExportDirectory() {
        FileUtils.deleteQuietly(rootDirectory);
    }

    @BeforeMethod
    @Override
    protected void prepareManagerServiceStub() {
        Mockito.when(managerService.dumpGroups(Mockito.any(DictionaryNamedPath.class)))
                .thenReturn(Lists.newArrayList(generateMockGroup("alpha", 3, 2, 2), generateMockGroup("beta", 3, 2, 2)));
    }

    @AfterMethod
    @Override
    protected void purgeManagerServiceStub() {
        Mockito.reset(managerService);
    }

    @Override
    protected ExportDictionariesCommand createPreferenceInstance() throws Exception {
        ExportDictionariesCommand command = new ExportDictionariesCommand();
        command.setPathToFile(Lists.newArrayList(exportDirectory.getCanonicalPath()));
        return command;
    }

    @DataProvider
    private Iterator<Object[]> createExportCases() {
        return Lists.<Object[]>newArrayList(
                new Object[]{null, null, false},
                new Object[]{null, null, true},
                new Object[]{"alpha", null, false},
                new Object[]{null, "dictionary", true},
                new Object[]{"someGroup", "someDictionary", false}
        ).iterator();
    }

    @Test(
            dataProvider = "createExportCases",
            description = "Тест проверяет экспорт документов"
    )
    public void testExport(String groupName, String dictionaryName, boolean compress) throws Exception {
        // call preference
        ExportDictionariesCommand preference = createPreferenceInstance();
        {
            preference.setGroupName(groupName);
            preference.setDictionaryName(dictionaryName);
            preference.setCompress(compress);
        }
        ExportDescriptor descriptor = exportDictionaries.exportDocuments(preference);
        assertNotNull(descriptor);

        // check invocation arguments
        ArgumentCaptor<DictionaryNamedPath> pathCaptor = ArgumentCaptor.forClass(DictionaryNamedPath.class);
        Mockito.verify(managerService).dumpGroups(pathCaptor.capture());
        assertEquals(pathCaptor.getValue(), new DictionaryNamedPath(groupName, dictionaryName));

        // check descriptor
        assertEquals(descriptor.getPathToArchive() != null, compress);
        File actualDirectory = descriptor.getDirectory();
        assertTrue(actualDirectory.list().length != 0);
    }
}
