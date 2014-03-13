package ru.hflabs.rcd.storage.sequence;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.model.rule.RecodeRuleSet;
import ru.hflabs.rcd.service.ISequenceService;

import static org.testng.Assert.*;
import static ru.hflabs.rcd.model.MockFactory.createMockGroup;
import static ru.hflabs.rcd.model.MockFactory.createMockRecodeRuleSet;

@Test
public class SequenceServiceTest {

    private ISequenceService sequenceService;

    @BeforeClass
    private void createSequenceGenerator() {
        sequenceService = new SequenceService(new UUIDSequenceGenerator());
    }

    public void testFillIdentifier1() throws Exception {
        assertNotNull(sequenceService.fillIdentifier(new Group(), false).getId());
        assertNotNull(sequenceService.fillIdentifier(new Group(), true).getId());
    }

    public void testFillIdentifier2() throws Exception {
        Identifying object = createMockGroup();
        object.setId(object.getClass().getSimpleName());
        assertEquals(sequenceService.fillIdentifier(object, false).getId(), object.getClass().getSimpleName());
    }

    public void testFillIdentifier3() throws Exception {
        Identifying object = createMockGroup();
        object.setId(object.getClass().getSimpleName());
        Identifying result = sequenceService.fillIdentifier(object, true);
        assertNotNull(result.getId());
        assertFalse(object.getClass().getSimpleName().equals(result.getId()));
    }

    public void testFillIdentifier4() throws Exception {
        assertNull(sequenceService.fillIdentifier(null, true));
        assertNull(sequenceService.fillIdentifier(null, false));
    }

    public void testFillIdentifierToRRS1() throws Exception {
        RecodeRuleSet ruleSet = createMockRecodeRuleSet();
        ruleSet.setId(null);
        ruleSet.setName(null);
        RecodeRuleSet result = sequenceService.fillIdentifier(ruleSet, true);
        assertNotNull(result.getId());
        assertEquals(result.getId(), result.getName());
    }

    public void testFillIdentifierToRRS2() throws Exception {
        RecodeRuleSet ruleSet = createMockRecodeRuleSet();
        ruleSet.setId(null);
        RecodeRuleSet result = sequenceService.fillIdentifier(ruleSet, true);
        assertNotNull(result.getId());
        assertNotEquals(result.getId(), result.getName());
    }
}
