package ru.hflabs.rcd.backend.console;

import com.google.common.collect.ImmutableList;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import ru.hflabs.rcd.backend.console.preference.FilePreference;
import ru.hflabs.rcd.service.IManagerService;

import javax.annotation.Resource;
import java.io.File;
import java.net.URL;
import java.util.List;

@ContextConfiguration(
        locations = {
                ResourcePatternResolver.CLASSPATH_URL_PREFIX + "backend.test.xml",
                ResourcePatternResolver.CLASSPATH_URL_PREFIX + "backend.mock-factories.xml"
        }
)
public abstract class RunTemplateTest<P extends FilePreference> extends AbstractTestNGSpringContextTests {

    /** Сервис управления документами */
    @Resource(name = "remoteManagerService")
    protected IManagerService managerService;

    /**
     * Подготавливает сервис управления документами
     *
     * @see ru.hflabs.rcd.service.IManagerService
     */
    protected void prepareManagerServiceStub() {
        // do nothing
    }

    protected void purgeManagerServiceStub() {
        // do nothing
    }

    /**
     * Создает и возвращает настройки импорта
     *
     * @return Возвращает базовые настройки импорта
     */
    protected abstract P createPreferenceInstance() throws Exception;

    /**
     * Создает и возвращает настройки импорта
     *
     * @param paths относительные пути к файлам
     * @return Возвращает базовые настройки импорта
     */
    protected P createBasePreference(String... paths) throws Exception {
        final P preference = createPreferenceInstance();
        preference.setPathToFile(createTargetFiles(paths));
        return preference;
    }

    /**
     * Создает и возвращает коллекцию полных путей к файлам
     *
     * @param paths коллекция относительных путей
     * @return Возвращает сформированную коллекцию полных путей
     */
    protected static List<String> createTargetFiles(String... paths) throws Exception {
        final URL rootDirectoryUrl = RunTemplateTest.class.getClassLoader().getResource(".");
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        for (String path : paths) {
            builder.add(new File(rootDirectoryUrl.getFile(), path).getCanonicalPath());
        }
        return builder.build();
    }
}
