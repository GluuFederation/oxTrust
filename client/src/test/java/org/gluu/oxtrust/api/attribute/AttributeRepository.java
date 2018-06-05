package org.gluu.oxtrust.api.attribute;

import java.util.List;

import org.gluu.oxtrust.api.BaseRepository;
import org.gluu.oxtrust.api.GluuAttributeApi;
import org.gluu.oxtrust.api.proxy.AttributeProxy;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class AttributeRepository extends BaseRepository {

	private String PATH = "restv1/api/attributes";
	private ResteasyClient client;

	public AttributeRepository() {
		super();
		PATH = baseURI + PATH;
		client = new ResteasyClientBuilder().build();
	}

	public List<GluuAttributeApi> getAllAttributes() {
		ResteasyWebTarget target = client.target(PATH);
		AttributeProxy client = target.proxy(AttributeProxy.class);
		return client.getAllAttributes();
	}

	public List<GluuAttributeApi> getAllActiveAttributes() {
		ResteasyWebTarget target = client.target(PATH);
		AttributeProxy client = target.proxy(AttributeProxy.class);
		return client.getAllActiveAttributes();
	}

	public List<GluuAttributeApi> getAllInActiveAttributes() {
		ResteasyWebTarget target = client.target(PATH);
		AttributeProxy client = target.proxy(AttributeProxy.class);
		return client.getAllInActiveAttributes();
	}

	public GluuAttributeApi addAttribute(GluuAttributeApi gluuAttributeApi) {
		ResteasyWebTarget target = client.target(PATH);
		AttributeProxy client = target.proxy(AttributeProxy.class);
		return client.createAttribute(gluuAttributeApi);
	}

	public GluuAttributeApi updateAttribute(GluuAttributeApi gluuAttributeApi) {
		ResteasyWebTarget target = client.target(PATH);
		AttributeProxy client = target.proxy(AttributeProxy.class);
		return client.updateAttribute(gluuAttributeApi);
	}

	public List<GluuAttributeApi> searchAttributes(String searchPattern, int size) {
		ResteasyWebTarget target = client.target(PATH);
		AttributeProxy client = target.proxy(AttributeProxy.class);
		return client.searchAttributes(searchPattern, size);
	}

	public void deleteAttribute(String inum) {
		ResteasyWebTarget target = client.target(PATH);
		AttributeProxy client = target.proxy(AttributeProxy.class);
		client.deleteAttribute(inum);
	}

	public GluuAttributeApi getAttributeByInum(String inum) {
		ResteasyWebTarget target = client.target(PATH);
		AttributeProxy client = target.proxy(AttributeProxy.class);
		try {
			return client.getAttribute(inum);
		} catch (Exception e) {
			return null;
		}

	}
}
