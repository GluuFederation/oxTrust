package org.gluu.oxtrust.api.test;

import java.util.Arrays;

import org.gluu.oxtrust.api.GluuOxAuthClient;
import org.gluu.oxtrust.api.proxy.OxAuthClientProxy;
import org.gluu.oxtrust.model.OxAuthApplicationType;
import org.gluu.oxtrust.model.OxAuthScope;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.xdi.oxauth.model.common.GrantType;
import org.xdi.oxauth.model.common.ResponseType;

public class OxAuthClientRepositoryImpl extends BaseRepository {

	private String PATH = "restv1/api/clients";
	private ResteasyClient client;
	private String inum;
	private String searchPattern = "inbound";

	public OxAuthClientRepositoryImpl() {
		super();
		PATH = baseURI + PATH;
		client = new ResteasyClientBuilder().build();
	}

	public void getAllClients() {
		System.out.println("==================");
		System.out.println("List clients");
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		OxAuthClientProxy client = target.proxy(OxAuthClientProxy.class);
		for (GluuOxAuthClient authClient : client.getClients()) {
			System.out.println(authClient.getDisplayName());
			inum = authClient.getInum();
		}
		System.out.println("Done");
	}

	public void searchClients() {
		System.out.println("==================");
		System.out.println("Search clients");
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		OxAuthClientProxy client = target.proxy(OxAuthClientProxy.class);
		for (GluuOxAuthClient authClient : client.searchClients(searchPattern, 2)) {
			System.out.println(authClient.getDisplayName());
			inum = authClient.getInum();
		}
		System.out.println("Done");
	}

	public void getClientScopes() {
		inum = "@!619C.061B.1A7E.5AF4!0001!4377.CD0A!0008!19C7.70AA.4388.942E";
		System.out.println("==================");
		System.out.println("Get client " + inum + " scopes");
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		OxAuthClientProxy client = target.proxy(OxAuthClientProxy.class);
		for (OxAuthScope scope : client.getClientScopes(inum)) {
			System.out.println(scope.getDisplayName());
		}
		System.out.println("Done");
	}

	public void deleteClient() {
		inum = "@!619C.061B.1A7E.5AF4!0001!4377.CD0A!0008!7D21.E637.B969.1C51";
		System.out.println("==================");
		System.out.println("Delete client " + inum);
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		OxAuthClientProxy client = target.proxy(OxAuthClientProxy.class);
		client.deleteClient(inum);
		System.out.println("Done");
	}

	public String add() {
		System.out.println("==================");
		System.out.println("Add new client");
		System.out.println("==================");
		GluuOxAuthClient gluuOxAuthClient = generatedOxAuthClient();
		ResteasyWebTarget target = client.target(PATH);
		OxAuthClientProxy client = target.proxy(OxAuthClientProxy.class);
		GluuOxAuthClient result = client.createClient(gluuOxAuthClient);
		System.out.println("Client added");
		return result.getInum();
	}

	private GluuOxAuthClient generatedOxAuthClient() {
		GluuOxAuthClient gluuOxAuthClient = new GluuOxAuthClient();
		gluuOxAuthClient.setDescription("");
		gluuOxAuthClient.setDisplayName("NewAddedClient");
		gluuOxAuthClient.setOxAuthAppType(OxAuthApplicationType.WEB);
		GrantType[] grantTypes = { GrantType.AUTHORIZATION_CODE };
		gluuOxAuthClient.setGrantTypes(grantTypes);
		ResponseType[] responseTypes = { ResponseType.CODE, ResponseType.ID_TOKEN };
		gluuOxAuthClient.setResponseTypes(responseTypes);
		gluuOxAuthClient.setOxAuthRedirectURIs(Arrays.asList("https://gasmyr.estelle.com/love"));
		return gluuOxAuthClient;
	}

	public void testAll() {
		this.getAllClients();
		this.searchClients();
		this.getClientScopes();
		// this.deleteClient();
		this.add();
	}

}
