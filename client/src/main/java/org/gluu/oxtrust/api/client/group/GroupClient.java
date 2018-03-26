package org.gluu.oxtrust.api.client.group;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gluu.oxtrust.api.client.AbstractClient;
import org.gluu.oxtrust.model.GluuGroup;

public class GroupClient extends AbstractClient<GluuGroup> {

	private static final Logger logger = LogManager.getLogger(GroupClient.class);

	private static final String PATH = "/restv1/api/groups";

	public GroupClient(Client client, String baseURI) {
		super(GluuGroup.class, client, baseURI, PATH);
	}

	public List<GluuGroup> list() {
		logger.debug("Final Url:" + webTarget.getUri().toString());
		GenericType<List<GluuGroup>> responseType = new GenericType<List<GluuGroup>>() {
		};
		return webTarget.request().get(responseType);
	}

	public List<GluuGroup> searchGroups(String pattern, int size) {
		WebTarget resource = webTarget.path("search").queryParam("pattern", pattern).queryParam("size", size);
		logger.debug("Final Url:" + resource.getUri().toString());
		GenericType<List<GluuGroup>> responseType = new GenericType<List<GluuGroup>>() {
		};
		return resource.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).get(responseType);
	}

	public GluuGroup getGroup(String inum) {
		logger.debug("Final Url:" + webTarget.getUri().toString());
		WebTarget resource = webTarget.path("/{inum}").resolveTemplate("inum", inum);
		return resource.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).get(GluuGroup.class);
	}

}
