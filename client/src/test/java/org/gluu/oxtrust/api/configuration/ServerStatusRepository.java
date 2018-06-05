package org.gluu.oxtrust.api.configuration;

import org.gluu.oxtrust.api.BaseRepository;
import org.gluu.oxtrust.api.GluuServerStatus;
import org.gluu.oxtrust.api.proxy.ServerStatusProxy;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class ServerStatusRepository extends BaseRepository {

	private String PATH = "restv1/api/configuration/status";
	private ResteasyClient client;

	public ServerStatusRepository() {
		super();
		PATH = baseURI + PATH;
		client = new ResteasyClientBuilder().build();
	}

	public GluuServerStatus getServerStatus() {
		ResteasyWebTarget target = client.target(PATH);
		ServerStatusProxy simpleClient = target.proxy(ServerStatusProxy.class);
		return simpleClient.getServerStatus();
	}

}
