package org.gluu.oxtrust.api.openidconnect;

import java.util.List;

import org.gluu.oxtrust.api.BaseRepository;
import org.gluu.oxtrust.api.proxy.SectorIdentifierProxy;
import org.gluu.oxtrust.model.OxAuthSectorIdentifier;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class SectorIdentifierRepository extends BaseRepository {

	private String PATH = "restv1/api/sectoridentifiers";
	private ResteasyClient client;

	public SectorIdentifierRepository() {
		super();
		PATH = baseURI + PATH;
		client = new ResteasyClientBuilder().build();
	}

	public List<OxAuthSectorIdentifier> getAllSectorIdentifiers() {
		ResteasyWebTarget target = client.target(PATH);
		SectorIdentifierProxy client = target.proxy(SectorIdentifierProxy.class);
		return client.getAllSectorIdentifiers();
	}

	public List<OxAuthSectorIdentifier> searchSectorIdentifiers(String searchPattern) {
		ResteasyWebTarget target = client.target(PATH);
		SectorIdentifierProxy client = target.proxy(SectorIdentifierProxy.class);
		return client.searchSectorIdentifiers(searchPattern);
	}

	public OxAuthSectorIdentifier getSectorIdentifier(String id) {
		ResteasyWebTarget target = client.target(PATH);
		SectorIdentifierProxy client = target.proxy(SectorIdentifierProxy.class);
		try {
			return client.getSectorIdentifier(id);
		} catch (Exception e) {
			return null;
		}

	}

	public void deleteSectorIdentifier(String id) {
		ResteasyWebTarget target = client.target(PATH);
		SectorIdentifierProxy client = target.proxy(SectorIdentifierProxy.class);
		client.deleteSectorIdentifier(id);
	}

	public OxAuthSectorIdentifier createSector(OxAuthSectorIdentifier sector) {
		ResteasyWebTarget target = client.target(PATH);
		SectorIdentifierProxy client = target.proxy(SectorIdentifierProxy.class);
		return client.createSectorIdentifier(sector);
	}

	public OxAuthSectorIdentifier updateSector(OxAuthSectorIdentifier sector) {
		ResteasyWebTarget target = client.target(PATH);
		SectorIdentifierProxy client = target.proxy(SectorIdentifierProxy.class);
		return client.updateSectorIdentifier(sector);
	}
}
