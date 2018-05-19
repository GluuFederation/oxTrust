package org.gluu.oxtrust.api.test;

import org.gluu.oxtrust.api.GluuAttributeApi;
import org.gluu.oxtrust.api.proxy.AttributeProxy;
import org.gluu.persist.model.base.GluuStatus;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.xdi.model.GluuAttribute;
import org.xdi.model.GluuAttributeDataType;

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

	public void getAllAttributes() {
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

	public GluuAttributeApi addAttribute() {
		System.out.println("==================");
		System.out.println("Add new attribute");
		System.out.println("==================");
		GluuAttributeApi gluuAttributeApi = generatedAttribute();
		ResteasyWebTarget target = client.target(PATH);
		AttributeProxy client = target.proxy(AttributeProxy.class);
		GluuAttributeApi result = client.createAttribute(gluuAttributeApi);
		System.out.println("Attribute added");
		return result;
	}

	public void updateAttribute(GluuAttributeApi gluuAttributeApi) {
		gluuAttributeApi.setDescription("New updated description");
		System.out.println("==================");
		System.out.println("Update attribute " + gluuAttributeApi.getInum());
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		AttributeProxy client = target.proxy(AttributeProxy.class);
		GluuAttributeApi updatedAttribute = client.updateAttribute(gluuAttributeApi);
		System.out.println(updatedAttribute.getDisplayName());
		System.out.println("*******************");
		System.out.println("Done");
	}

	public void searchAttributes() {
		System.out.println("==================");
		System.out.println("Search Attributes");
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		AttributeProxy client = target.proxy(AttributeProxy.class);
		for (GluuAttributeApi attributeApi : client.searchAttributes(searchPattern, 1)) {
			System.out.println(attributeApi.toString());
			inum = attributeApi.getInum();
		}
		System.out.println("Done");
	}

	public void deleteAttribute() {
		inum = "@!619C.061B.1A7E.5AF4!0001!4377.CD0A!0008!7D21.E637.B969.1C51";
		System.out.println("==================");
		System.out.println("Delete Attribute " + inum);
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		AttributeProxy client = target.proxy(AttributeProxy.class);
		client.deleteAttribute(inum);
		System.out.println("Done");
	}

	private GluuAttributeApi generatedAttribute() {
		GluuAttributeApi gluuAttributeApi = new GluuAttributeApi();
		gluuAttributeApi.setDescription("My new Attribute");
		gluuAttributeApi.setDisplayName("New Attribute");
		gluuAttributeApi.setDataType(GluuAttributeDataType.STRING);
		gluuAttributeApi.setStatus(GluuStatus.ACTIVE);
		gluuAttributeApi.setName("kudiaId");
		gluuAttributeApi.setOrigin("gluuPerson");
		return gluuAttributeApi;
	}

	public void testAll() {
		GluuAttributeApi gluuAttributeApi = addAttribute();
		updateAttribute(gluuAttributeApi);
		getAllAttributes();
		getAllActiveAttributes();
		getAllInActiveAttributes();
		searchAttributes();
		// deleteAttribute();
	}

}
