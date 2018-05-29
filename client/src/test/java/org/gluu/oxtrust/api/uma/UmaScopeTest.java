package org.gluu.oxtrust.api.uma;

import java.util.List;
import java.util.Random;

import org.gluu.oxtrust.api.users.PeopleRepository;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xdi.oxauth.model.uma.persistence.UmaScopeDescription;

public class UmaScopeTest {

	private UmaScopeRepository scopeRepository;
	private String inum;
	private String searchPattern = "scim";
	private UmaScopeDescription scope;
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
		scopeRepository = new UmaScopeRepository();
	}

	@Test
	public void getAllUmaScopesTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("List uma scopes");
		System.out.println("==================");

		List<UmaScopeDescription> scopes = scopeRepository.getAllUmaScopes();

		Assert.assertNotNull(scopes);
		Assert.assertTrue(!scopes.isEmpty());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void searchScopesTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Search uma scopes");
		System.out.println("==================");

		List<UmaScopeDescription> scopesFound = scopeRepository.searchUmaScopes(searchPattern, 10);

		Assert.assertNotNull(scopesFound);
		Assert.assertTrue(!scopesFound.isEmpty());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void addUmaScopeTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Add new uma scope");
		System.out.println("==================");

		scope = scopeRepository.createUmaScope(generateNewUmaScope());

		Assert.assertNotNull(scope);
		Assert.assertNotNull(scope.getInum());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void updateUmaScopeTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Update new uma scope");
		System.out.println("==================");
		scope = scopeRepository.createUmaScope(generateNewUmaScope());
		inum = scope.getInum();
		String displayName = "UpdatedDisplayName";
		scope.setDisplayName(displayName);

		scope = scopeRepository.updateScope(scope);

		Assert.assertNotNull(scope);
		Assert.assertEquals(inum, scope.getInum());
		Assert.assertEquals(displayName, scope.getDisplayName());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void getUmaScopeByInumTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Get uma scope by inum");
		System.out.println("==================");
		scope = scopeRepository.createUmaScope(generateNewUmaScope());
		inum = scope.getInum();

		scope = scopeRepository.getUmaScopeByInum(inum);

		Assert.assertNotNull(scope);
		Assert.assertEquals(inum, scope.getInum());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void deleteUmaScopeTest() {
		Assume.assumeTrue(canRunOtherTest);
		scope = scopeRepository.createUmaScope(generateNewUmaScope());
		inum = scope.getInum();

		scopeRepository.deleteUmaScope(inum);

		Assert.assertNull(scopeRepository.getUmaScopeByInum(inum));
		System.out.println("*******************");
		System.out.println("Done");
	}

	private UmaScopeDescription generateNewUmaScope() {
		int next1 = new Random().nextInt(100);
		int next2 = new Random().nextInt(50);
		String name = "UmaScope" + next1 + next2;
		UmaScopeDescription scope = new UmaScopeDescription();
		scope.setDisplayName(name);
		scope.setDescription(name + "  " + name);
		scope.setId("https://gluu.gasmyr.com/identity/scim");
		return scope;
	}

}
