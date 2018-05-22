package org.gluu.oxtrust.api.test;

import java.util.UUID;

import org.gluu.oxtrust.api.proxy.UmaResourceProxy;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.xdi.oxauth.model.uma.persistence.UmaResource;

public class UmaResourceRepository extends BaseRepository {

	private String PATH = "restv1/api/uma/resources";
	private ResteasyClient client;
	private String id;
	private String searchPattern = "scim";

	public UmaResourceRepository() {
		super();
		PATH = baseURI + PATH;
		client = new ResteasyClientBuilder().build();
	}

	public void getAllUmaResources() {
		System.out.println("==================");
		System.out.println("List uma resources");
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		UmaResourceProxy client = target.proxy(UmaResourceProxy.class);
		for (UmaResource resource : client.getAllUmaResources()) {
			System.out.println(resource.toString());
			id = resource.getId();
		}
		System.out.println("*******************");
		System.out.println("Done");
	}

	public void searchUmaResources() {
		System.out.println("==================");
		System.out.println("Search uma resources");
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		UmaResourceProxy client = target.proxy(UmaResourceProxy.class);
		for (UmaResource resource : client.searchUmaResources(searchPattern, 100)) {
			System.out.println(resource.toString());
			id = resource.getId();
		}
		System.out.println("*******************");
		System.out.println("Done");
	}

	public UmaResource createUmaResource(UmaResource resource) {
		System.out.println("==================");
		System.out.println("Add new uma resource");
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		UmaResourceProxy simpleClient = target.proxy(UmaResourceProxy.class);
		UmaResource createdResource = simpleClient.createUmaResource(resource);
		System.out.println(createdResource.toString());
		System.out.println("*******************");
		System.out.println("Done");
		return createdResource;
	}

	public void updateUmaResource(UmaResource resource) {
		System.out.println("==================");
		System.out.println("Update uma resource " + resource.getInum());
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		resource.setDescription("Description updated");
		UmaResourceProxy simpleClient = target.proxy(UmaResourceProxy.class);
		UmaResource updatedResource = simpleClient.updateUmaResource(resource);
		System.out.println(updatedResource.toString());
		System.out.println("*******************");
		System.out.println("Done");
	}

	public void deleteUmaResource() {
		System.out.println("==================");
		System.out.println("Delete uma resource " + id);
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		UmaResourceProxy simpleClient = target.proxy(UmaResourceProxy.class);
		simpleClient.deleteUmaResource(id);
		System.out.println("*******************");
		System.out.println("Done");
	}

	public void getUmaResourceById() {
		System.out.println("==================");
		System.out.println("Get uma resource by " + id);
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		UmaResourceProxy simpleClient = target.proxy(UmaResourceProxy.class);
		System.out.println(simpleClient.getUmaResource(id).toString());
		System.out.println("*******************");
		System.out.println("Done");
	}

	public void testAll() {
		getAllUmaResources();
		searchUmaResources();
		getUmaResourceById();
		UmaResource umaResource = createUmaResource(generateUmaResource());
		updateUmaResource(umaResource);
		// deleteUmaResource();
	}

	private UmaResource generateUmaResource() {
		String name = "New UmaResource";
		UmaResource umaResource = new UmaResource();
		umaResource.setName(name);
		umaResource.setDescription(name + "  " + name);
		umaResource.setId(UUID.randomUUID().toString());
		return umaResource;
	}

}
