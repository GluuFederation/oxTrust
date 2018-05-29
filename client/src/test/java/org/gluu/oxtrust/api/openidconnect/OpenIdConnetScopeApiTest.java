package org.gluu.oxtrust.api.openidconnect;

import java.util.List;
import java.util.Random;

import org.gluu.oxtrust.model.OxAuthScope;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xdi.oxauth.model.common.ScopeType;

public class OpenIdConnetScopeApiTest {
	private GluuScopeRepository scopeRepository;
	private OxAuthScope scope;
	private static String searchPattern = "pro";
	private static boolean canRunOtherTest = false;

	@BeforeClass
	public static void testConnection() {
		try {
			GluuScopeRepository scopeRepository = new GluuScopeRepository();
			scopeRepository.searchScopes(searchPattern);
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
		scopeRepository = new GluuScopeRepository();
	}

	@Test
	public void getAllScopesTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("List openidconnect scopes");
		System.out.println("==================");

		List<OxAuthScope> scopes = scopeRepository.getAllScopes();

		Assert.assertNotNull(scopes);
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void addScopeTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Add Scope");
		System.out.println("==================");

		scope = scopeRepository.createScope(generateAScope());

		Assert.assertNotNull(scope);
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void updateScopeTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Update Scope");
		System.out.println("==================");
		scope = scopeRepository.createScope(generateAScope());
		String displayName = "UpdatedScope";
		scope.setDisplayName(displayName);

		scope = scopeRepository.updateScope(scope);

		Assert.assertEquals(displayName, scope.getDisplayName());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void getScopeByInumTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Get scope");
		System.out.println("==================");
		scope = scopeRepository.createScope(generateAScope());
		String inum = scope.getInum();

		scope = scopeRepository.getScope(inum);

		Assert.assertNotNull(scope);
		Assert.assertEquals(inum, scope.getInum());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void getScopeByWrongInumTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Get scope");
		System.out.println("==================");
		String inum = "@!1525525";

		scope = scopeRepository.getScope(inum);

		Assert.assertNull(scope);
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void searchScopesTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Search scopes");
		System.out.println("==================");

		List<OxAuthScope> scopes = scopeRepository.searchScopes(searchPattern);

		Assert.assertNotNull(scopes);
		Assert.assertTrue(scopes.size() > 1);
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void deleteScopeTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Delete scope");
		System.out.println("==================");
		scope = scopeRepository.createScope(generateAScope());
		String inum = scope.getInum();

		scopeRepository.deleteScope(inum);

		scope = scopeRepository.getScope(inum);
		Assert.assertNull(scope);
		System.out.println("*******************");
		System.out.println("Done");
	}

	private OxAuthScope generateAScope() {
		int next1 = new Random().nextInt(100);
		int next2 = new Random().nextInt(50);
		String displayName = "ScopeAddByTest" + next1 + next2;
		OxAuthScope scope = new OxAuthScope();
		scope.setDisplayName(displayName);
		scope.setScopeType(ScopeType.OPENID);
		scope.setDescription("scope added using rest api");
		return scope;
	}

}
