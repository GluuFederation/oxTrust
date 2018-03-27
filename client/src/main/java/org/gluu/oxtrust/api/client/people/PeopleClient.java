package org.gluu.oxtrust.api.client.people;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.gluu.oxtrust.api.client.util.AbstractClient;
import org.gluu.oxtrust.model.GluuCustomPerson;

public class PeopleClient extends AbstractClient<GluuCustomPerson> {

	private static final String PATH = "/restv1/api/people";

	public PeopleClient(Client client, String baseURI) {
		super(GluuCustomPerson.class, client, baseURI, PATH);
	}

	public List<GluuCustomPerson> list() {
		System.out.println("Final Url:" + webTarget.getUri().toString());
		GenericType<List<GluuCustomPerson>> responseType = new GenericType<List<GluuCustomPerson>>() {
		};
		return webTarget.request().get(responseType);
	}

	public List<GluuCustomPerson> searchGroups(String pattern, int size) {
		WebTarget resource = webTarget.path("search").queryParam("pattern", pattern).queryParam("size", size);
		System.out.println("Final Url:" + resource.getUri().toString());
		GenericType<List<GluuCustomPerson>> responseType = new GenericType<List<GluuCustomPerson>>() {
		};
		return resource.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).get(responseType);
	}

	public GluuCustomPerson getGroup(String inum) {
		System.out.println("Final Url:" + webTarget.getUri().toString());
		WebTarget resource = webTarget.path("/{inum}").resolveTemplate("inum", inum);
		return resource.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.get(GluuCustomPerson.class);
	}

}
