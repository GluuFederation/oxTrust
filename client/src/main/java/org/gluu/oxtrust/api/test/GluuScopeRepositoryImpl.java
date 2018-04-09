package org.gluu.oxtrust.api.test;

import org.gluu.oxtrust.api.proxy.ScopeProxy;
import org.gluu.oxtrust.model.OxAuthScope;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.xdi.oxauth.model.common.ScopeType;

public class GluuScopeRepositoryImpl extends BaseRepository {

	private String PATH = "restv1/api/scopes";
	private ResteasyClient client;
	private String inum;
	private String searchPattern = "pro";

	public GluuScopeRepositoryImpl() {
		super();
		PATH = baseURI + PATH;
		client = new ResteasyClientBuilder().build();
	}

	public void getAllScopes() {
		System.out.println("==================");
		System.out.println("List all scope");
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		ScopeProxy client = target.proxy(ScopeProxy.class);
		for (OxAuthScope scope : client.getScopes()) {
			System.out.println(scope.getDisplayName());
			inum = scope.getInum();
		}
		System.out.println("*******************");
		System.out.println("Done");
	}

	public void getSearchScopes() {
		System.out.println("==================");
		System.out.println("Search scopes");
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		ScopeProxy client = target.proxy(ScopeProxy.class);
		for (OxAuthScope scope : client.searchScopes(searchPattern, 100)) {
			System.out.println(scope.getDisplayName());
			inum = scope.getInum();
		}
		System.out.println("*******************");
		System.out.println("Done");
	}

	public void deleteScope() {
		System.out.println("==================");
		System.out.println("Delete scope " + inum);
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		ScopeProxy client = target.proxy(ScopeProxy.class);
		client.deleteScope(inum);
		System.out.println("*******************");
		System.out.println("Done");
	}

	public void getScope(String inum) {
		System.out.println("==================");
		System.out.println("Get scope" + inum);
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		ScopeProxy client = target.proxy(ScopeProxy.class);
		System.out.println(client.getScope(inum).toString());
		System.out.println("*******************");
		System.out.println("Done");
	}

	public OxAuthScope createScope(OxAuthScope person) {
		System.out.println("==================");
		System.out.println("Add Scope");
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		ScopeProxy client = target.proxy(ScopeProxy.class);
		OxAuthScope createdScope = client.createScope(person);
		System.out.println(createdScope.getDisplayName());
		System.out.println("*******************");
		System.out.println("Done");
		return createdScope;
	}

	public void updateScope(OxAuthScope person) {
		System.out.println("==================");
		System.out.println("Update scope" + person.getInum());
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		ScopeProxy client = target.proxy(ScopeProxy.class);
		OxAuthScope updatedScope = client.updateScope(person);
		System.out.println(updatedScope.getDisplayName());
		System.out.println("*******************");
		System.out.println("Done");
	}

	public void testAll() {
		this.getAllScopes();
		this.getSearchScopes();
		this.deleteScope();
		OxAuthScope scope = this.createScope(generateAScope());
		scope.setDisplayName("UdatedScope");
		this.updateScope(scope);
	}

	private OxAuthScope generateAScope() {
		OxAuthScope scope = new OxAuthScope();
		scope.setDisplayName("AddedScope");
		scope.setScopeType(ScopeType.OPENID);
		scope.setDescription("scope added using rest api");
		return scope;
	}

}
