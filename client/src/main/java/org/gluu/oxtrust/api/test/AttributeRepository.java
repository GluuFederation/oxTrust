package org.gluu.oxtrust.api.test;

import org.gluu.oxtrust.api.proxy.AttributeProxy;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.xdi.model.GluuAttribute;

public class AttributeRepository extends BaseRepository {

	private String PATH = "restv1/api/attributes";
	private ResteasyClient client;
	private String inum;
	private String searchPattern = "city";

	public AttributeRepository() {
		super();
		PATH = baseURI + PATH;
		client = new ResteasyClientBuilder().build();
	}

	public void getAllAttrinutes() {
		System.out.println("==================");
		System.out.println("List all attributes");
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		AttributeProxy client = target.proxy(AttributeProxy.class);
		for (GluuAttribute gluuAttribute : client.getAllAttributes()) {
			System.out.println(gluuAttribute.getDisplayName());
			inum = gluuAttribute.getInum();
		}
		System.out.println("Done");
	}

	public void getAllActiveAttributes() {
		System.out.println("==================");
		System.out.println("List all actives attributes");
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		AttributeProxy client = target.proxy(AttributeProxy.class);
		for (GluuAttribute gluuAttribute : client.getAllActiveAttributes()) {
			System.out.println(gluuAttribute.getDisplayName());
			inum = gluuAttribute.getInum();
		}
		System.out.println("Done");
	}

	public void getAllInActiveAttributes() {
		System.out.println("==================");
		System.out.println("List all inactives attributes");
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		AttributeProxy client = target.proxy(AttributeProxy.class);
		for (GluuAttribute gluuAttribute : client.getAllInActiveAttributes()) {
			System.out.println(gluuAttribute.getDisplayName());
			inum = gluuAttribute.getInum();
		}
		System.out.println("Done");
	}

	public void testAll() {
		getAllAttrinutes();
	}

}
