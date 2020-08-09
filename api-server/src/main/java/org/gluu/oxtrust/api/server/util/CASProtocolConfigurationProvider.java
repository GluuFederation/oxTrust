package org.gluu.oxtrust.api.server.util;

import org.gluu.oxtrust.service.CASService;
import org.gluu.oxtrust.util.CASProtocolAvailability;
import org.gluu.oxtrust.util.CASProtocolConfiguration;
import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.config.oxtrust.ShibbolethCASProtocolConfiguration;

import javax.inject.Inject;

public class CASProtocolConfigurationProvider {

	@Inject
	private CASService casService;

	@Inject
	private AppConfiguration appConfiguration;

	public CASProtocolConfiguration get() {
		CASProtocolAvailability casProtocolAvailability = CASProtocolAvailability.get();
		if (!casProtocolAvailability.isAvailable()) {
			throw new IllegalArgumentException();
		}
		return new CASProtocolConfiguration(baseUrl(), configuration());
	}

	private String baseUrl() {
		return appConfiguration.getIdpUrl() + "/idp/profile/cas";
	}

	private ShibbolethCASProtocolConfiguration configuration() {
		ShibbolethCASProtocolConfiguration configuration = casService.loadCASConfiguration();
		if (configuration != null) {
			return configuration;
		}
		return create();
	}

	private ShibbolethCASProtocolConfiguration create() {
		ShibbolethCASProtocolConfiguration newConfiguration = new ShibbolethCASProtocolConfiguration();
		newConfiguration.setEnabled(false);
		newConfiguration.setEnableToProxyPatterns(false);
		newConfiguration.setAuthorizedToProxyPattern("https://([A-Za-z0-9_-]+\\.)*example\\.org(:\\d+)?/.*");
		newConfiguration.setUnauthorizedToProxyPattern("https://([A-Za-z0-9_-]+\\.)*example\\.org(:\\d+)?/.*");
		newConfiguration.setSessionStorageType(SessionStorageType.DEFAULT_STORAGE_SERVICE.getName());
		return newConfiguration;
	}
}