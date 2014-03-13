package ru.hflabs.rcd.service;

import org.testng.annotations.Test;
import ru.hflabs.rcd.ServiceTest;
import ru.hflabs.rcd.model.definition.ModelDefinition;
import ru.hflabs.rcd.model.definition.ModelFieldDefinition;
import ru.hflabs.rcd.model.document.Dictionary;
import ru.hflabs.rcd.model.document.Group;
import ru.hflabs.rcd.model.document.MetaField;

import javax.annotation.Resource;
import java.util.Map;

import static org.testng.Assert.*;

@Test
public class ModelDefinitionFactoryTest extends ServiceTest {

    /** Фабрика создения моделей */
    @Resource(name = "modelDefinitionFactory")
    private IServiceFactory<ModelDefinition, Class<?>> factory;

    public void testGroup() {
        ModelDefinition modelDefinition = factory.retrieveService(Group.class);
        assertNotNull(modelDefinition);
        assertEquals(modelDefinition.getId(), "Group");
        Map<String, ModelFieldDefinition> fields = modelDefinition.getFields();
        assertNotNull(fields);
        assertEquals(fields.size(), 6);
        // check name
        {
            ModelFieldDefinition modelFieldDefinition = fields.get(Group.NAME);
            assertNotNull(modelFieldDefinition);
            assertEquals(modelFieldDefinition.getType(), ModelFieldDefinition.FieldType.STRING);
            assertEquals(modelFieldDefinition.getMinLength(), new Long(Group.NAME_MIN_SIZE));
            assertEquals(modelFieldDefinition.getMaxLength(), new Long(Group.NAME_MAX_SIZE));
            assertTrue(modelFieldDefinition.isRequired());
            assertNull(modelFieldDefinition.getPattern());
            assertTrue(modelFieldDefinition.isSortable());
        }
        // check description
        {
            ModelFieldDefinition modelFieldDefinition = fields.get(Group.DESCRIPTION);
            assertNotNull(modelFieldDefinition);
            assertEquals(modelFieldDefinition.getType(), ModelFieldDefinition.FieldType.STRING);
            assertEquals(modelFieldDefinition.getMinLength(), new Long(0L));
            assertEquals(modelFieldDefinition.getMaxLength(), new Long(Group.DESCRIPTION_SIZE));
            assertFalse(modelFieldDefinition.isRequired());
            assertNull(modelFieldDefinition.getPattern());
            assertTrue(modelFieldDefinition.isSortable());
        }
        // check permissions
        {
            ModelFieldDefinition modelFieldDefinition = fields.get(Group.PERMISSIONS);
            assertNotNull(modelFieldDefinition);
            assertEquals(modelFieldDefinition.getType(), ModelFieldDefinition.FieldType.NUMBER);
            assertFalse(modelFieldDefinition.isRequired());
        }
    }

    public void testDictionary() {
        ModelDefinition modelDefinition = factory.retrieveService(Dictionary.class);
        assertNotNull(modelDefinition);
        assertEquals(modelDefinition.getId(), "Dictionary");
        Map<String, ModelFieldDefinition> fields = modelDefinition.getFields();
        assertNotNull(fields);
        assertEquals(fields.size(), 7);
    }

    public void testMetaField() {
        ModelDefinition modelDefinition = factory.retrieveService(MetaField.class);
        assertNotNull(modelDefinition);
        assertEquals(modelDefinition.getId(), "MetaField");
        Map<String, ModelFieldDefinition> fields = modelDefinition.getFields();
        assertNotNull(fields);
        assertEquals(fields.size(), 6);
    }
}
