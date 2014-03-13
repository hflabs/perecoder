package ru.hflabs.rcd.lucene.binder;

import org.testng.annotations.BeforeClass;
import ru.hflabs.rcd.ServiceTest;
import ru.hflabs.rcd.model.Named;
import ru.hflabs.rcd.model.document.DocumentTemplate;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.model.rule.RecodeRule;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;
import ru.hflabs.rcd.model.rule.Rule;
import ru.hflabs.rcd.service.IServiceFactory;
import ru.hflabs.util.lucene.LuceneBinderTransformer;

import javax.annotation.Resource;

import static org.testng.Assert.*;

/**
 * Класс <class>BinderTransformerTest</class> реализует базовый класс для тестов трансформации сущности API в документ
 *
 * @see IServiceFactory
 */
public class BinderTransformerTest<E> extends ServiceTest {

    /** Целевой класс API */
    private final Class<E> targetClass;
    /** Фабрика сервисов */
    @Resource(name = "luceneBinderTransformerFactory")
    private IServiceFactory<LuceneBinderTransformer<E, String>, Class<?>> binderTransformerFactory;

    /** Сервис трансформации */
    protected LuceneBinderTransformer<E, String> binderTransformer;

    public BinderTransformerTest(Class<E> targetClass) {
        this.targetClass = targetClass;
    }

    @BeforeClass
    public void testInstance() {
        assertNotNull(binderTransformerFactory);
        binderTransformer = binderTransformerFactory.retrieveService(targetClass);
        assertNotNull(binderTransformer);
    }

    public static <T extends Named> void assertNamed(T actual, T expected) {
        assertEquals(actual.getName(), expected.getName());
    }

    public static <T extends DocumentTemplate> void assertDocumentTemplate(T actual, T expected) {
        assertEquals(actual.getId(), expected.getId());
        assertEquals(actual.getHistoryId(), expected.getHistoryId());
    }

    public static void assertEqualsGroup(Group actual, Group expected) {
        assertDocumentTemplate(actual, expected);
        assertNamed(actual, expected);
        assertEquals(actual.getDescription(), expected.getDescription());
        assertEquals(actual.getOwner(), expected.getOwner());
        assertNull(actual.getChangeType());
        assertNull(actual.getChangeDate());
    }

    private static <R extends Rule<?, ?, ?>> void assertEqualsRule(R actual, R expected) {
        assertDocumentTemplate(actual, expected);
        assertEquals(actual.getFromFieldId(), expected.getFromFieldId());
        assertEquals(actual.getToFieldId(), expected.getToFieldId());
    }

    public static void assertEqualsRecodeRuleSet(RecodeRuleSet actual, RecodeRuleSet expected) {
        assertEqualsRule(actual, expected);
        assertEquals(actual.getDefaultFieldId(), expected.getDefaultFieldId());
    }

    public static void assertEqualsRecodeRule(RecodeRule actual, RecodeRule expected) {
        assertEqualsRule(actual, expected);
        assertEquals(actual.getRecodeRuleSetId(), expected.getRecodeRuleSetId());
    }
}
