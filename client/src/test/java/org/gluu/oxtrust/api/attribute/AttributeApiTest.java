package org.gluu.oxtrust.api.attribute;

import java.util.List;
import java.util.Random;

import org.gluu.oxtrust.api.GluuAttributeApi;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xdi.model.GluuStatus;
import org.xdi.model.attribute.AttributeDataType;

public class AttributeApiTest {
	private AttributeRepository attributeRepository;
	private static String searchPattern = "city";
	private GluuAttributeApi attribute;
	private String inum;
	private static boolean canRunOtherTest = false;

	@BeforeClass
	public static void testConnection() {
		try {
			AttributeRepository attributeRepository = new AttributeRepository();
			attributeRepository.searchAttributes(searchPattern, 1);
			canRunOtherTest = true;
		} catch (Exception e) {
			System.out.println("***********************");
			System.out.println("ERROR OCCURS: POSSIBLE CAUSES");
			System.out.println("1. MAKE SURE THE HOSTNAME DEFINE IN CONFIGURATION FILE IS RESOLVABLE");
			System.out.println("2. MAKE SURE THE CERTS FILE ARE IMPORTED IN JAVA KEY STORE");
			System.out.println("***********************");
		}
	}

	@Before
	public void setup() {
		Assume.assumeTrue(canRunOtherTest);
		attributeRepository = new AttributeRepository();
	}

	@Test
	public void getAllAttributesTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("List all attributes");
		System.out.println("==================");

		List<GluuAttributeApi> allAttributes = attributeRepository.getAllAttributes();

		Assert.assertNotNull(allAttributes);
		Assert.assertTrue(!allAttributes.isEmpty());
		Assert.assertTrue(allAttributes.size() > 1);
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void getAllActiveAttributesTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("List all actives attributes");
		System.out.println("==================");

		List<GluuAttributeApi> allActivesAttributes = attributeRepository.getAllActiveAttributes();

		Assert.assertNotNull(allActivesAttributes);
		Assert.assertTrue(!allActivesAttributes.isEmpty());
		Assert.assertTrue(allActivesAttributes.size() > 1);
		Assert.assertTrue(allActivesAttributes.get(0).getStatus() == GluuStatus.ACTIVE);
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void getAllInactiveAttributesTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("List all inactives attributes");
		System.out.println("==================");

		List<GluuAttributeApi> allInActivesAttributes = attributeRepository.getAllInActiveAttributes();

		Assert.assertNotNull(allInActivesAttributes);
		Assert.assertTrue(!allInActivesAttributes.isEmpty());
		Assert.assertTrue(allInActivesAttributes.size() > 1);
		Assert.assertTrue(allInActivesAttributes.get(0).getStatus() == GluuStatus.INACTIVE);
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void searchAttributesTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Search Attributes");
		System.out.println("==================");

		List<GluuAttributeApi> attributesFound = attributeRepository.searchAttributes(searchPattern, 1);

		Assert.assertNotNull(attributesFound);
		Assert.assertTrue(!attributesFound.isEmpty());
		Assert.assertTrue(attributesFound.size() == 1);

	}

	@Test
	public void addAttributeTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Add new attribute");
		System.out.println("==================");

		attribute = attributeRepository.addAttribute(generatedNewAttribute());

		Assert.assertNotNull(attribute);
		Assert.assertNotNull(attribute.getInum());

		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void updateAttributeTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Update attribute");
		System.out.println("==================");
		String description = "UpdatedDescription";
		attribute = attributeRepository.addAttribute(generatedNewAttribute());
		attribute.setDescription(description);

		attribute = attributeRepository.updateAttribute(attribute);

		Assert.assertNotNull(attribute);
		Assert.assertNotNull(attribute.getInum());
		Assert.assertEquals(description, attribute.getDescription());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void getAttributeByInumTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Get attribute by inum");
		System.out.println("==================");
		attribute = attributeRepository.addAttribute(generatedNewAttribute());
		inum = attribute.getInum();

		attribute = attributeRepository.getAttributeByInum(inum);

		Assert.assertNotNull(attribute);
		Assert.assertEquals(inum, attribute.getInum());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void deleteAttributeTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Update attribute");
		System.out.println("==================");
		attribute = attributeRepository.addAttribute(generatedNewAttribute());
		inum = attribute.getInum();

		attributeRepository.deleteAttribute(inum);

		Assert.assertNull(attributeRepository.getAttributeByInum(inum));
		System.out.println("*******************");
		System.out.println("Done");
	}

	private GluuAttributeApi generatedNewAttribute() {
		int next1 = new Random().nextInt(100);
		int next2 = new Random().nextInt(50);
		GluuAttributeApi gluuAttributeApi = new GluuAttributeApi();
		String displayName = "AtrributeAddByTest" + next1 + next2;
		gluuAttributeApi.setDescription("My new Attribute");
		gluuAttributeApi.setDisplayName(displayName);
		gluuAttributeApi.setDataType(AttributeDataType.STRING);
		gluuAttributeApi.setStatus(GluuStatus.ACTIVE);
		gluuAttributeApi.setName("kudiaId");
		gluuAttributeApi.setOrigin("gluuPerson");
		return gluuAttributeApi;
	}

}
