package org.gluu.oxtrust.api.uma;

import java.util.List;

import org.gluu.oxtrust.api.BaseRepository;
import org.gluu.oxtrust.api.proxy.UmaScopeProxy;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.xdi.oxauth.model.uma.persistence.UmaScopeDescription;

public class UmaScopeRepository extends BaseRepository {

	private String PATH = "restv1/api/uma/scopes";
	private ResteasyClient client;

	public UmaScopeRepository() {
		super();
		PATH = baseURI + PATH;
		client = new ResteasyClientBuilder().build();
	}

	public List<UmaScopeDescription> getAllUmaScopes() {
		ResteasyWebTarget target = client.target(PATH);
		UmaScopeProxy client = target.proxy(UmaScopeProxy.class);
		return client.getAllUmaScopes();
	}

	public List<UmaScopeDescription> searchUmaScopes(String searchPattern, int size) {
		ResteasyWebTarget target = client.target(PATH);
		UmaScopeProxy client = target.proxy(UmaScopeProxy.class);
		return client.searchUmaScopes(searchPattern, size);
	}

	public void deleteUmaScope(String inum) {
		ResteasyWebTarget target = client.target(PATH);
		UmaScopeProxy simpleClient = target.proxy(UmaScopeProxy.class);
		simpleClient.deleteUmaScope(inum);
	}

	public UmaScopeDescription getUmaScopeByInum(String inum) {
		ResteasyWebTarget target = client.target(PATH);
		UmaScopeProxy simpleClient = target.proxy(UmaScopeProxy.class);
		try {
			return simpleClient.getUmaScope(inum);
		} catch (Exception e) {
			return null;
		}
	}

	public UmaScopeDescription createUmaScope(UmaScopeDescription scope) {
		ResteasyWebTarget target = client.target(PATH);
		UmaScopeProxy simpleClient = target.proxy(UmaScopeProxy.class);
		return simpleClient.createUmaScope(scope);
	}

	public UmaScopeDescription updateScope(UmaScopeDescription scope) {
		ResteasyWebTarget target = client.target(PATH);
		UmaScopeProxy simpleClient = target.proxy(UmaScopeProxy.class);
		return simpleClient.updateUmaScopeDescription(scope);
	}
}
