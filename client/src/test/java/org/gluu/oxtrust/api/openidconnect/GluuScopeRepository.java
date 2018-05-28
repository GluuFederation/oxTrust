package org.gluu.oxtrust.api.openidconnect;

import java.util.List;

import org.gluu.oxtrust.api.BaseRepository;
import org.gluu.oxtrust.api.proxy.ScopeProxy;
import org.gluu.oxtrust.model.OxAuthScope;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class GluuScopeRepository extends BaseRepository {

	private String PATH = "restv1/api/scopes";
	private ResteasyClient client;

	public GluuScopeRepository() {
		super();
		PATH = baseURI + PATH;
		client = new ResteasyClientBuilder().build();
	}

	public List<OxAuthScope> getAllScopes() {
		ResteasyWebTarget target = client.target(PATH);
		ScopeProxy client = target.proxy(ScopeProxy.class);
		return client.getScopes();

	}

	public List<OxAuthScope> getSearchScopes(String searchPattern) {
		ResteasyWebTarget target = client.target(PATH);
		ScopeProxy client = target.proxy(ScopeProxy.class);
		return client.searchScopes(searchPattern, 100);
	}

	public void deleteScope(String inum) {
		ResteasyWebTarget target = client.target(PATH);
		ScopeProxy client = target.proxy(ScopeProxy.class);
		client.deleteScope(inum);
	}

	public OxAuthScope getScope(String inum) {
		ResteasyWebTarget target = client.target(PATH);
		ScopeProxy client = target.proxy(ScopeProxy.class);
		try {
			return client.getScope(inum);
		} catch (Exception e) {
			return null;
		}
	}

	public OxAuthScope createScope(OxAuthScope person) {
		ResteasyWebTarget target = client.target(PATH);
		ScopeProxy client = target.proxy(ScopeProxy.class);
		return client.createScope(person);
	}

	public OxAuthScope updateScope(OxAuthScope person) {
		ResteasyWebTarget target = client.target(PATH);
		ScopeProxy client = target.proxy(ScopeProxy.class);
		return client.updateScope(person);
	}

}
