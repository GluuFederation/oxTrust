package org.gluu.oxtrust.api.openidconnect;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.gluu.oxtrust.api.GluuOxAuthClient;
import org.gluu.oxtrust.model.OxAuthApplicationType;
import org.gluu.oxtrust.model.OxAuthScope;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xdi.oxauth.model.common.GrantType;
import org.xdi.oxauth.model.common.ResponseType;

public class OpenIdConnectClientTest {

	private String inum;
	private static String searchPattern = "scim";
	private OxAuthClientRepository clientRepository;
	private GluuOxAuthClient client;
	private static boolean canRunOtherTest = false;

	@BeforeClass
	public static void testConnection() {
		try {
			OxAuthClientRepository clientRepository = new OxAuthClientRepository();
			clientRepository.searchClients(searchPattern, 1);
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
		clientRepository = new OxAuthClientRepository();
	}

	@Test
	public void getAllClientsTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("List clients");
		System.out.println("==================");

		List<GluuOxAuthClient> clients = clientRepository.getAllClients();

		Assert.assertNotNull(clients);
		Assert.assertTrue(!clients.isEmpty());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void searchClients() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Search clients");
		System.out.println("==================");

		List<GluuOxAuthClient> clientsFound = clientRepository.searchClients(searchPattern, 2);

		Assert.assertNotNull(clientsFound);
		Assert.assertTrue(!clientsFound.isEmpty());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void getClientScopesTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Get clients scopes");
		System.out.println("==================");
		client = clientRepository.searchClients(searchPattern, 1).get(0);
		inum = client.getInum();

		List<OxAuthScope> scopes = clientRepository.getClientScopes(inum);

		Assert.assertNotNull(scopes);
		Assert.assertTrue(!scopes.isEmpty());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void getClientByInum() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Get client by inum");
		System.out.println("==================");
		client = clientRepository.addClient(generatedNewClient());
		inum = client.getInum();

		client = clientRepository.getClientByInum(inum);

		Assert.assertNotNull(client);
		Assert.assertEquals(inum, client.getInum());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void addClientTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Add new client");
		System.out.println("==================");

		client = clientRepository.addClient(generatedNewClient());

		Assert.assertNotNull(client);
		Assert.assertNotNull(client.getInum());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void updateClientTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Update client");
		System.out.println("==================");
		client = clientRepository.addClient(generatedNewClient());
		inum = client.getInum();
		String displayName = "UpdatedClient";
		client.setDisplayName(displayName);

		client = clientRepository.updateClient(client);

		Assert.assertNotNull(client);
		Assert.assertNotNull(client.getInum());
		System.out.println("*******************");
		System.out.println("Done");
	}

	@Test
	public void deleteClientTest() {
		Assume.assumeTrue(canRunOtherTest);
		System.out.println("==================");
		System.out.println("Delete client");
		System.out.println("==================");
		client = clientRepository.addClient(generatedNewClient());
		inum = client.getInum();

		clientRepository.deleteClient(inum);

		Assert.assertNull(clientRepository.getClientByInum(inum));
		System.out.println("*******************");
		System.out.println("Done");
	}

	private GluuOxAuthClient generatedNewClient() {
		int next1 = new Random().nextInt(100);
		int next2 = new Random().nextInt(50);
		String clientName = "NewAddedClient" + next1 + next2;
		GluuOxAuthClient gluuOxAuthClient = new GluuOxAuthClient();
		gluuOxAuthClient.setDescription("");
		gluuOxAuthClient.setDisplayName(clientName);
		gluuOxAuthClient.setOxAuthAppType(OxAuthApplicationType.WEB);
		GrantType[] grantTypes = { GrantType.AUTHORIZATION_CODE };
		gluuOxAuthClient.setGrantTypes(grantTypes);
		ResponseType[] responseTypes = { ResponseType.CODE, ResponseType.ID_TOKEN };
		gluuOxAuthClient.setResponseTypes(responseTypes);
		gluuOxAuthClient.setOxAuthRedirectURIs(Arrays.asList("https://gasmyr.livevision.com" + next1));
		return gluuOxAuthClient;
	}

}
