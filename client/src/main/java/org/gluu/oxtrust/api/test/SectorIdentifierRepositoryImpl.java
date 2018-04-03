package org.gluu.oxtrust.api.test;

import org.gluu.oxtrust.api.proxy.SectorIdentifierProxy;
import org.gluu.oxtrust.model.OxAuthSectorIdentifier;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class SectorIdentifierRepositoryImpl extends BaseRepository {

	private String PATH = "restv1/api/sectoridentifiers";
	private ResteasyClient client;
	private String id;
	private String searchPattern = null;

	public SectorIdentifierRepositoryImpl() {
		super();
		PATH = baseURI + PATH;
		client = new ResteasyClientBuilder().build();
	}

	public void getAllSectorIdentifiers() {
		System.out.println("==================");
		System.out.println("List sector identifiers");
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		SectorIdentifierProxy client = target.proxy(SectorIdentifierProxy.class);
		for (OxAuthSectorIdentifier identifier : client.getAllSectorIdentifiers()) {
			System.out.println(identifier.toString());
		}
		System.out.println("*******************");
		System.out.println("Done");
	}

	public void searchSectorIdentifiers() {
		System.out.println("==================");
		System.out.println("Search sector identifiers");
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		SectorIdentifierProxy client = target.proxy(SectorIdentifierProxy.class);
		for (OxAuthSectorIdentifier identifier : client.searchSectorIdentifiers(searchPattern)) {
			System.out.println(identifier.toString());
		}
		System.out.println("*******************");
		System.out.println("Done");
	}

	public void getSectorIdentifier() {
		System.out.println("==================");
		System.out.println("Get sector identifier" + id);
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		SectorIdentifierProxy client = target.proxy(SectorIdentifierProxy.class);
		System.out.println(client.getSectorIdentifier(id).toString());
		System.out.println("*******************");
		System.out.println("Done");
	}

	public void deleteSectorIdentifier() {
		System.out.println("==================");
		System.out.println("Delete sector identifier " + id);
		System.out.println("==================");
		ResteasyWebTarget target = client.target(PATH);
		SectorIdentifierProxy client = target.proxy(SectorIdentifierProxy.class);
		client.deleteSectorIdentifier(id);
		System.out.println("*******************");
		System.out.println("Done");
	}

	public void testAll() {
		this.getAllSectorIdentifiers();
		this.searchSectorIdentifiers();
		this.deleteSectorIdentifier();
	}

}
