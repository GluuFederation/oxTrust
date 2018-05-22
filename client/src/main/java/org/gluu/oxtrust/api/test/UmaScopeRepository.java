package org.gluu.oxtrust.api.test;

import org.gluu.oxtrust.api.proxy.UmaScopeProxy;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.xdi.oxauth.model.uma.persistence.UmaScopeDescription;

public class UmaScopeRepository extends BaseRepository {

	private String PATH = "restv1/api/uma/scopes";
	private ResteasyClient client;
	private String inum;
	private String searchPattern = "scim";

	public UmaScopeRepository() {
		super();
		PATH = baseURI + PATH;
		client = new ResteasyClientBuilder().build();
	}

	public void getAllUmaScopes() {
		System.out.println("==================");
		System.out.println("List uma scopes");
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		UmaScopeProxy client = target.proxy(UmaScopeProxy.class);
		for (UmaScopeDescription scope : client.getAllUmaScopes()) {
			System.out.println(scope.toString());
			inum = scope.getInum();
		}
		System.out.println("*******************");
		System.out.println("Done");
	}

	public void searchUmaScopes() {
		System.out.println("==================");
		System.out.println("Search uma scopes");
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		UmaScopeProxy client = target.proxy(UmaScopeProxy.class);
		for (UmaScopeDescription scope : client.searchUmaScopes(searchPattern, 100)) {
			System.out.println(scope.toString());
			inum = scope.getInum();
		}
		System.out.println("*******************");
		System.out.println("Done");
	}

	public void deleteUmaScope(String inum) {
		System.out.println("==================");
		System.out.println("Delete uma scope " + inum);
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		UmaScopeProxy simpleClient = target.proxy(UmaScopeProxy.class);
		simpleClient.deleteUmaScope(inum);
		System.out.println("*******************");
		System.out.println("Done");
	}

	public void getUmaScopeByInum(String inum) {
		System.out.println("==================");
		System.out.println("Get uma scope by " + inum);
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		UmaScopeProxy simpleClient = target.proxy(UmaScopeProxy.class);
		System.out.println(simpleClient.getUmaScope(inum).toString());
		System.out.println("*******************");
		System.out.println("Done");
	}

	public UmaScopeDescription createUmaScope(UmaScopeDescription scope) {
		System.out.println("==================");
		System.out.println("Add new uma scope");
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		UmaScopeProxy simpleClient = target.proxy(UmaScopeProxy.class);
		UmaScopeDescription createdScope = simpleClient.createUmaScope(scope);
		System.out.println(createdScope.toString());
		System.out.println("*******************");
		System.out.println("Done");
		return createdScope;
	}

	public void updatePerson(UmaScopeDescription scope) {
		System.out.println("==================");
		System.out.println("Update uma scope" + scope.getInum());
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		scope.setDescription("Description updated");
		UmaScopeProxy simpleClient = target.proxy(UmaScopeProxy.class);
		UmaScopeDescription updatedScope = simpleClient.updateUmaScopeDescription(scope);
		System.out.println(updatedScope.toString());
		System.out.println("*******************");
		System.out.println("Done");
	}

	public void testAll() {
		getAllUmaScopes();
		searchUmaScopes();
		UmaScopeDescription umaScopeDescription = createUmaScope(generateAUmaScope());
		updatePerson(umaScopeDescription);
		getUmaScopeByInum(inum);
//		deleteUmaScope(inum);
	}

	private UmaScopeDescription generateAUmaScope() {
		String name = "UmaScope";
		UmaScopeDescription scope = new UmaScopeDescription();
		scope.setDisplayName(name);
		scope.setDescription(name + "  " + name);
		scope.setId("https://gluu.gasmyr.com/identity/scim");
		return scope;
	}

}
