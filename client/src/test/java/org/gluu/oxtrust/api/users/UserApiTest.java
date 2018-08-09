package org.gluu.oxtrust.api.users;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.gluu.oxtrust.api.GluuPersonApi;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xdi.model.GluuStatus;

public class UserApiTest {
	private GluuPersonApi user;
	private PeopleRepository peopleRepository;
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
		peopleRepository = new PeopleRepository();
	}

	@Test
	public void getPeopleTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("List people");
		System.out.println("==================");
		List<GluuPersonApi> peoples = peopleRepository.getAllPersons();
		Assert.assertNotNull(peoples);
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void addUserTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Add user");
		System.out.println("==================");
		int sizeBefore = peopleRepository.getAllPersons().size();
		user = peopleRepository.createPerson(generateNewUser());
		int sizeAfter = peopleRepository.getAllPersons().size();
		Assert.assertNotNull(user);
		Assert.assertNotNull(user.getInum());
		Assert.assertTrue((sizeBefore + 1) == sizeAfter);
		System.out.println(user.toString());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void updateUserTest() {
		Assume.assumeTrue(canRunOtherTest);
		user = peopleRepository.createPerson(generateNewUser());
		String displayName = "updated Display name";
		String inum = user.getInum();
		user.setDisplayName(displayName);
		System.out.println("=================" + inum);
		System.out.println("==================");
		user = peopleRepository.updatePerson(user);
		Assert.assertEquals(displayName, user.getDisplayName());
		Assert.assertEquals(inum, user.getInum());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void searchUserTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Search people");
		System.out.println("==================");
		String pattern = "user";
		List<GluuPersonApi> foundPeoples = peopleRepository.searchPersons(pattern);
		Assert.assertNotNull(foundPeoples);
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void getUserByInumTest() {
		Assume.assumeTrue(canRunOtherTest);
		user = peopleRepository.createPerson(generateNewUser());
		String inum = user.getInum();
		System.out.println("==================");
		System.out.println("Get person" + inum);
		System.out.println("==================");
		user = peopleRepository.getPerson(inum);
		Assert.assertNotNull(user);
		Assert.assertTrue(inum.equalsIgnoreCase(user.getInum()));
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void deleteUserTest() {
		Assume.assumeTrue(canRunOtherTest);
		user = peopleRepository.createPerson(generateNewUser());
		String inum = user.getInum();
		System.out.println("==================");
		System.out.println("Delete person " + inum);
		System.out.println("==================");
		peopleRepository.deletePerson(inum);
		Assert.assertEquals(null, peopleRepository.getPerson(inum));
		Assert.assertTrue(inum.equalsIgnoreCase(user.getInum()));
		System.out.println("*******************");
		System.out.println("Done");
	}

	private GluuPersonApi generateNewUser() {
		int next1 = new Random().nextInt(100);
		int next2 = new Random().nextInt(50);
		String name = "User" + next1 + next2;
		GluuPersonApi person = new GluuPersonApi();
		person.setEmail(name + "@yahoo.fr");
		person.setUserName(name);
		person.setStatus(GluuStatus.ACTIVE);
		person.setPassword(name);
		person.setDisplayName(name.toUpperCase());
		person.setSurName(name);
		person.setGivenName(name);
		person.setCreationDate(new Date());
		return person;
	}

}
