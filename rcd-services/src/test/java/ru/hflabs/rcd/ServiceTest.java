package ru.hflabs.rcd;

import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

/**
 * Класс <class>ServiceTest</class> реализует маркерный класс для сервисных тестов
 *
 * @author Nazin Alexander
 */
@ContextConfiguration(
        locations = {
                ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "api.xml",
                ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "services.xml",
                ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "storage.xml",
                ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "storage.test.xml"
        }
)
public abstract class ServiceTest extends AbstractTestNGSpringContextTests {
}
