package org.gluu.oxtrust.service.test;

import java.util.List;

import org.gluu.oxtrust.action.test.AbstractAuthorizationTest;
import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.model.GluuAttribute;
import org.gluu.oxtrust.model.GluuAttributeDataType;
import org.gluu.oxtrust.model.GluuAttributePrivacyLevel;
import org.gluu.oxtrust.model.GluuStatus;
import org.gluu.oxtrust.model.GluuUserRole;
import org.jboss.seam.mock.AbstractSeamTest;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Test class for AttributeService
 *
 * @author Yuriy Movchan Date: 10.14.2010
 */
public class AttributeServiceTest extends AbstractAuthorizationTest {

	@BeforeTest
	public void initTestConfiguration() throws Exception {
		initTest();
	}

	/**
	 * Test adding new attribute and getting attributes list
	 *
	 * @throws Exception
	 */
	@Test
	@Parameters(value = "userKey")
	public void testGetAttributesAndAddAttribute(String userKey) throws Exception {
		loginAndCheckLoggedInFacesRequest(userKey);

		new AbstractSeamTest.FacesRequest() {

			@Override
			protected void invokeApplication() throws Exception {
				// Get attributes
				AttributeService attributeService = (AttributeService) getInstance("attributeService");
				List<GluuAttribute> attributes = attributeService.getAllAttributes();

				Assert.assertNotNull(attributes, "Failed to load attributes definition");
				Assert.assertTrue(attributes.size() > 0, "Failed to load attributes definition");

				// Add new attribute
				GluuAttribute gluuAttribute = new GluuAttribute();
				gluuAttribute.setDn(getConf().getString("attributeServiceTest.add.gluuAttribute.dn"));

				gluuAttribute.setInum(getConf().getString("attributeServiceTest.add.gluuAttribute.inum"));
				gluuAttribute.setName(getConf().getString("attributeServiceTest.add.gluuAttribute.name"));
				gluuAttribute.setDisplayName(getConf().getString("attributeServiceTest.add.gluuAttribute.displayName"));
				gluuAttribute.setDescription(getConf().getString("attributeServiceTest.add.gluuAttribute.description"));
				gluuAttribute.setOrigin(getConf().getString("attributeServiceTest.add.gluuAttribute.origin"));
				gluuAttribute.setStatus(GluuStatus.getByValue(getConf().getString("attributeServiceTest.add.gluuAttribute.status")));
				gluuAttribute.setDataType(GluuAttributeDataType.getByValue(getConf().getString("attributeServiceTest.add.gluuAttribute.dataType")));
				gluuAttribute.setEditType(GluuUserRole.getByValues(getConf().getStringArray("attributeServiceTest.add.gluuAttribute.editType")));
				gluuAttribute.setViewType(GluuUserRole.getByValues(getConf().getStringArray("attributeServiceTest.add.gluuAttribute.viewType")));
				gluuAttribute.setPrivacyLevel(GluuAttributePrivacyLevel.getByValue(getConf().getString("attributeServiceTest.add.gluuAttribute.privacyLevel")));

				attributeService.addAttribute(gluuAttribute);

				// Get attributes after adding new attribute
				List<GluuAttribute> attributesAfterAdding = attributeService.getAllAttributes();

				Assert.assertTrue(System.identityHashCode(attributes) != System.identityHashCode(attributesAfterAdding),
                        "Attributes cache reloading failure after adding new attribute");

				Assert.assertTrue(attributes.size() + 1 == attributesAfterAdding.size(), "Invalid count of attributes after adding new attribute");
			}
		}.run();

		logoutUserFacesRequest();
	}

	/**
	 * Test adding new attribute and getting attributes list
	 *
	 * @throws Exception
	 */
	@Test
	@Parameters(value = "userKey")
	public void testUpdateAttribute(String userKey) throws Exception {
		loginAndCheckLoggedInFacesRequest(userKey);

		new AbstractSeamTest.FacesRequest() {

			@Override
			protected void invokeApplication() throws Exception {
				// Get attribute
				String inum = getConf().getString("attributeServiceTest.update.gluuAttribute.inum");
				String description = getConf().getString("attributeServiceTest.add.gluuAttribute.description");

				AttributeService attributeService = (AttributeService) getInstance("attributeService");

				// Get attribute by Inum
				GluuAttribute attribute = attributeService.getAttributeByInum(inum);
				Assert.assertNotNull(attribute, "Failed to get attribute by Inum");

				// Set new description
				attribute.setDescription(description);

				// Save attribute
				attributeService.updateAttribute(attribute);

				// Get attribute by Inum
				attribute = attributeService.getAttributeByInum(inum);
				Assert.assertNotNull(attribute, "Failed to get attribute by Inum");

				Assert.assertTrue(description.equals(attribute.getDescription()), "Invalid description of attribute");
			}
		}.run();

		logoutUserFacesRequest();
	}

	/**
	 * Test adding new attribute and getting attributes list
	 *
	 * @throws Exception
	 */
	@Test
	@Parameters(value = "userKey")
	public void testGetAttributeByInum(String userKey) throws Exception {
		loginAndCheckLoggedInFacesRequest(userKey);

		new AbstractSeamTest.FacesRequest() {

			@Override
			protected void invokeApplication() throws Exception {
				// Get attribute
				AttributeService attributeService = (AttributeService) getInstance("attributeService");
				GluuAttribute attribute = attributeService.getAttributeByInum(getConf().getString("attributeServiceTest.get.gluuAttribute.inum"));

				Assert.assertNotNull(attribute, "Failed to get attribute by Inum");
			}
		}.run();

		logoutUserFacesRequest();
	}

}
