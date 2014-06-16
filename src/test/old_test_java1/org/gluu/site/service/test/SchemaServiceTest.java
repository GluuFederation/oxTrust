package org.gluu.site.service.test;

import org.gluu.site.action.test.ConfigurableTest;
import org.gluu.site.ldap.service.SchemaService;
import org.gluu.site.model.schema.SchemaEntry;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Test class for SchemaService
 * 
 * @author Yuriy Movchan Date: 10.16.2010
 */
public class SchemaServiceTest extends ConfigurableTest {

	@BeforeTest
	public void initTestConfiguration() throws Exception {
		initTest();
	}

	/**
	 * Test adding/removing attribute types/object classes to DS schema
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAddAttributesAndObjectClassCreation() throws Exception {
		new FacesRequest() {

			@Override
			protected void invokeApplication() throws Exception {
				SchemaService schemaService = (SchemaService) getInstance("schemaService");

				String attributeType1 = getConf().getString("schemaServiceTest.testAddAttributesAndObjectClassCreation.custom-attribute-1");
				String attributeType2 = getConf().getString("schemaServiceTest.testAddAttributesAndObjectClassCreation.custom-attribute-2");
				String attributeType3 = getConf().getString("schemaServiceTest.testAddAttributesAndObjectClassCreation.custom-attribute-3");

				String objectClass = getConf().getString("schemaServiceTest.testAddAttributesAndObjectClassCreation.custom-object-class");

				// Add objectClass without attributes
				schemaService.addObjectClass(objectClass, null);
				SchemaEntry schema = schemaService.getSchema();
				Assert.assertNotNull(schemaService.getObjectClassDefinition(schema, objectClass), String.format("Failed to find objectClass %s in DS schema", objectClass));

				// Add attributeType1 to schema
				schemaService.addStringAttribute(attributeType1, attributeType1);
				schema = schemaService.getSchema();
				Assert.assertNotNull(schemaService.getAttributeTypeDefinition(schema, attributeType1), String.format("Failed to find attribute %s in DS schema", attributeType1));

				// Add attributeType2 to schema
				schemaService.addStringAttribute(attributeType2, attributeType2);
				schema = schemaService.getSchema();
				Assert.assertNotNull(schemaService.getAttributeTypeDefinition(schema, attributeType2), String.format("Failed to find attribute %s in DS schema", attributeType2));

				// Add attributeType3 to schema
				schemaService.addStringAttribute(attributeType3, attributeType3);
				schema = schemaService.getSchema();
				Assert.assertNotNull(schemaService.getAttributeTypeDefinition(schema, attributeType3), String.format("Failed to find attribute %s in DS schema", attributeType3));

				// Add attributeType1 to objectClass
				schemaService.addAttributeTypeToObjectClass(objectClass, attributeType1);
				// Add attributeType2 to objectClass
				schemaService.addAttributeTypeToObjectClass(objectClass, attributeType2);
				// Add attributeType3 to objectClass
				schemaService.addAttributeTypeToObjectClass(objectClass, attributeType3);

				// Check if objectClass contains attributeType1, attributeType2,
				// attributeType3 types
				schema = schemaService.getSchema();
				String objectClassDefinition = schemaService.getObjectClassDefinition(schema, objectClass);
				Assert.assertNotNull(objectClassDefinition, String.format("Failed to find objectClass %s in DS schema", objectClass));
				Assert.assertTrue(objectClassDefinition.contains(attributeType1), String.format("Failed to find attributeType %s in objectClass %s in DS schema", attributeType1,
						objectClass));
				Assert.assertTrue(objectClassDefinition.contains(attributeType2), String.format("Failed to find attributeType %s in objectClass %s in DS schema", attributeType2,
						objectClass));
				Assert.assertTrue(objectClassDefinition.contains(attributeType3), String.format("Failed to find attributeType %s in objectClass %s in DS schema", attributeType3,
						objectClass));

				// Remove attributeType1, attributeType3, attributeType3 from
				// objectClass
				schemaService.removeAttributeTypeFromObjectClass(objectClass, attributeType1);
				schemaService.removeAttributeTypeFromObjectClass(objectClass, attributeType3);
				schemaService.removeAttributeTypeFromObjectClass(objectClass, attributeType2);

				// Check if objectClass not contains attributeType1,
				// attributeType2, attributeType3 types
				schema = schemaService.getSchema();
				objectClassDefinition = schemaService.getObjectClassDefinition(schema, objectClass);
				Assert.assertNotNull(objectClassDefinition, String.format("Failed to find objectClass %s in DS schema", objectClass));
				Assert.assertFalse(objectClassDefinition.contains(attributeType1), String.format("objectClass %s should not contains attributeType %s in DS schema", objectClass,
						attributeType1));
				Assert.assertFalse(objectClassDefinition.contains(attributeType2), String.format("objectClass %s should not contains attributeType %s in DS schema", objectClass,
						attributeType2));
				Assert.assertFalse(objectClassDefinition.contains(attributeType3), String.format("objectClass %s should not contains attributeType %s in DS schema", objectClass,
						attributeType3));

				// Remove objectClass
				schemaService.removeObjectClass(objectClass);
				schema = schemaService.getSchema();
				Assert.assertNull(schemaService.getObjectClassDefinition(schema, objectClass), String.format("DS Schema should not contains objectClass %s", objectClass));

				// Remove attributeType1, attributeType2, attributeType3
				schemaService.removeStringAttribute(attributeType1);
				schemaService.removeStringAttribute(attributeType2);
				schemaService.removeStringAttribute(attributeType3);
				schema = schemaService.getSchema();
				Assert.assertNull(schemaService.getAttributeTypeDefinition(schema, attributeType1), String.format("DS Schema should not contains attribute %s", attributeType1));
				Assert.assertNull(schemaService.getAttributeTypeDefinition(schema, attributeType2), String.format("DS Schema should not contains attribute %s", attributeType2));
				Assert.assertNull(schemaService.getAttributeTypeDefinition(schema, attributeType3), String.format("DS Schema should not contains attribute %s", attributeType3));
			}
		}.run();
	}

}
