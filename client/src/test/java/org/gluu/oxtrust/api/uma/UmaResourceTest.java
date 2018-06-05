package org.gluu.oxtrust.api.uma;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.gluu.oxtrust.api.users.PeopleRepository;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xdi.oxauth.model.uma.persistence.UmaResource;

public class UmaResourceTest {

	private UmaResourceRepository resourceRepository;
	private UmaResource resource;
	private String searchPattern = "scim";
	private String id;
	
	private static boolean canRunOtherTest = false;

	@BeforeClass
	public static void testConnection() {
		try {
			PeopleRepository peopleRepositoryImpl = new PeopleRepository();
			peopleRepositoryImpl.searchPersons("user");
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
		resourceRepository = new UmaResourceRepository();
	}

	@Test
	public void getAllUmaResourcesTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("List uma resources");
		System.out.println("==================");

		List<UmaResource> resources = resourceRepository.getAllUmaResources();

		Assert.assertNotNull(resources);
		Assert.assertTrue(!resources.isEmpty());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void searchUmaResourcesTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Search uma resources");
		System.out.println("==================");

		List<UmaResource> resources = resourceRepository.searchUmaResources(searchPattern, 100);

		Assert.assertNotNull(resources);
		Assert.assertTrue(!resources.isEmpty());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void createUmaResourceTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Create uma resource ");
		System.out.println("==================");

		resource = resourceRepository.createUmaResource(generateUmaResource());

		Assert.assertNotNull(resource);
		Assert.assertNotNull(resource.getId());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void updateUmaResourceTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Update uma resource ");
		System.out.println("==================");
		resource = resourceRepository.createUmaResource(generateUmaResource());
		id = resource.getId();
		resource = resourceRepository.updateUmaResource(resource);

		Assert.assertNotNull(resource);
		Assert.assertEquals(id, resource.getId());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void deleteUmaResourceTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Delete uma resource ");
		System.out.println("==================");
		resource = resourceRepository.createUmaResource(generateUmaResource());
		id = resource.getId();

		resourceRepository.deleteUmaResource(id);

		Assert.assertNull(resourceRepository.getUmaResourceById(id));
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void getUmaResourceTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Get uma resource ");
		System.out.println("==================");
		resource = resourceRepository.createUmaResource(generateUmaResource());
		id = resource.getId();

		resource = resourceRepository.getUmaResourceById(id);

		Assert.assertNotNull(resource);
		Assert.assertEquals(id, resource.getId());
		System.out.println("*******************");
		System.out.println("Done");
	}

	private UmaResource generateUmaResource() {
		int next1 = new Random().nextInt(100);
		int next2 = new Random().nextInt(50);
		String name = "New UmaResource" + next1 + next2;
		UmaResource umaResource = new UmaResource();
		umaResource.setName(name);
		umaResource.setDescription(name + " description");
		umaResource.setId(UUID.randomUUID().toString());
		return umaResource;
	}

}
