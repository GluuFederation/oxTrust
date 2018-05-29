package org.gluu.oxtrust.api.uma;

import java.util.List;

import org.gluu.oxtrust.api.BaseRepository;
import org.gluu.oxtrust.api.proxy.UmaResourceProxy;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.xdi.oxauth.model.uma.persistence.UmaResource;

public class UmaResourceRepository extends BaseRepository {

	private String PATH = "restv1/api/uma/resources";
	private ResteasyClient client;

	public UmaResourceRepository() {
		super();
		PATH = baseURI + PATH;
		client = new ResteasyClientBuilder().build();
	}

	public List<UmaResource> getAllUmaResources() {
		ResteasyWebTarget target = client.target(PATH);
		UmaResourceProxy client = target.proxy(UmaResourceProxy.class);
		return client.getAllUmaResources();
	}

	public List<UmaResource> searchUmaResources(String searchPattern, int size) {
		ResteasyWebTarget target = client.target(PATH);
		UmaResourceProxy client = target.proxy(UmaResourceProxy.class);
		return client.searchUmaResources(searchPattern, size);

	}

	public UmaResource createUmaResource(UmaResource resource) {
		ResteasyWebTarget target = client.target(PATH);
		UmaResourceProxy simpleClient = target.proxy(UmaResourceProxy.class);
		return simpleClient.createUmaResource(resource);
	}

	public UmaResource updateUmaResource(UmaResource resource) {
		ResteasyWebTarget target = client.target(PATH);
		resource.setDescription("Description updated");
		UmaResourceProxy simpleClient = target.proxy(UmaResourceProxy.class);
		return simpleClient.updateUmaResource(resource);

	}

	public void deleteUmaResource(String id) {
		ResteasyWebTarget target = client.target(PATH);
		UmaResourceProxy simpleClient = target.proxy(UmaResourceProxy.class);
		simpleClient.deleteUmaResource(id);
	}

	public UmaResource getUmaResourceById(String id) {
		ResteasyWebTarget target = client.target(PATH);
		UmaResourceProxy simpleClient = target.proxy(UmaResourceProxy.class);
		try {
			return simpleClient.getUmaResource(id);
		} catch (Exception e) {
			return null;
		}

	}
}
