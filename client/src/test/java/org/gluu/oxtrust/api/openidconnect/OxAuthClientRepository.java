package org.gluu.oxtrust.api.openidconnect;

import java.util.List;

import org.gluu.oxtrust.api.BaseRepository;
import org.gluu.oxtrust.api.GluuOxAuthClient;
import org.gluu.oxtrust.api.proxy.OxAuthClientProxy;
import org.gluu.oxtrust.model.OxAuthScope;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class OxAuthClientRepository extends BaseRepository {

	private String PATH = "restv1/api/clients";
	private ResteasyClient client;

	public OxAuthClientRepository() {
		super();
		PATH = baseURI + PATH;
		client = new ResteasyClientBuilder().build();
	}

	public List<GluuOxAuthClient> getAllClients() {
		ResteasyWebTarget target = client.target(PATH);
		OxAuthClientProxy client = target.proxy(OxAuthClientProxy.class);
		return client.getClients();
	}

	public List<GluuOxAuthClient> searchClients(String searchPattern, int size) {
		ResteasyWebTarget target = client.target(PATH);
		OxAuthClientProxy client = target.proxy(OxAuthClientProxy.class);
		return client.searchClients(searchPattern, size);
	}

	public List<OxAuthScope> getClientScopes(String inum) {
		ResteasyWebTarget target = client.target(PATH);
		OxAuthClientProxy client = target.proxy(OxAuthClientProxy.class);
		return client.getClientScopes(inum);
	}

	public void deleteClient(String inum) {
		ResteasyWebTarget target = client.target(PATH);
		OxAuthClientProxy client = target.proxy(OxAuthClientProxy.class);
		client.deleteClient(inum);
	}

	public GluuOxAuthClient addClient(GluuOxAuthClient gluuOxAuthClient) {
		ResteasyWebTarget target = client.target(PATH);
		OxAuthClientProxy client = target.proxy(OxAuthClientProxy.class);
		return client.createClient(gluuOxAuthClient);
	}

	public GluuOxAuthClient updateClient(GluuOxAuthClient gluuOxAuthClient) {
		ResteasyWebTarget target = client.target(PATH);
		OxAuthClientProxy client = target.proxy(OxAuthClientProxy.class);
		return client.updateClient(gluuOxAuthClient);
	}

	public GluuOxAuthClient getClientByInum(String inum) {
		ResteasyWebTarget target = client.target(PATH);
		OxAuthClientProxy client = target.proxy(OxAuthClientProxy.class);
		try {
			return client.getClient(inum);
		} catch (Exception e) {
			return null;
		}

	}
}
