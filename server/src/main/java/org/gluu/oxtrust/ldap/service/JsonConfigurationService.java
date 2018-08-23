/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.service.OpenIdService;
import org.gluu.oxtrust.service.config.oxauth.OxAuthConfigObjectMapper;
import org.gluu.oxtrust.service.config.oxauth.OxAuthConfigurationService;
import org.gluu.oxtrust.service.config.oxtrust.OxTrustConfigurationExtractor;
import org.gluu.oxtrust.service.config.oxtrust.OxTrustConfigurationService;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.exception.BasePersistenceException;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.config.oxtrust.CacheRefreshConfiguration;
import org.xdi.config.oxtrust.ImportPersonConfig;
import org.xdi.config.oxtrust.LdapOxTrustConfiguration;
import org.xdi.service.JsonService;
import org.xdi.service.cache.CacheConfiguration;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;

/**
 * Provides operations with JSON oxAuth/oxTrust configuration
 * 
 * @author Yuriy Movchan Date: 12.15.2010
 */
@Stateless
@Named("jsonConfigurationService")
public class JsonConfigurationService implements Serializable {

	private static final long serialVersionUID = -3840968275007784641L;

	@Inject
	private Logger log;

	@Inject
	private PersistenceEntryManager ldapEntryManager;
	@Inject
	private JsonService jsonService;

	@Inject
	private ConfigurationFactory configurationFactory;

	@Inject
	private ApplianceService applianceService;

	@Inject
	private OxAuthConfigurationService oxAuthConfigurationService;

	@Inject
	private OxAuthConfigObjectMapper oxAuthConfigObjectMapper;

	@Inject
	private OxTrustConfigurationService oxTrustConfigurationService;

	@Inject
	private OxTrustConfigurationExtractor oxTrustConfigurationExtractor;

	public AppConfiguration getOxTrustappConfiguration() {
		return oxTrustConfigurationService.find();
	}
	
	public CacheConfiguration getOxMemCacheConfiguration() {
		return applianceService.getAppliance().getCacheConfiguration();
	}
	
	public boolean saveOxMemCacheConfiguration(CacheConfiguration cachedConfiguration) {
		GluuAppliance gluuAppliance = applianceService.getAppliance();
		gluuAppliance.setCacheConfiguration(cachedConfiguration);
		applianceService.updateAppliance(gluuAppliance);
		return true;
	}

	public ImportPersonConfig getOxTrustImportPersonConfiguration() {
		return loadOxTrustConfig().getImportPersonConfig();
	}

	public CacheRefreshConfiguration getOxTrustCacheRefreshConfiguration() {
		return loadOxTrustConfig().getCacheRefresh();
	}

	public String getOxAuthDynamicConfigJson() throws JsonGenerationException, JsonMappingException, IOException {
		return jsonService.objectToJson(oxAuthConfigurationService.find());
	}

	public org.xdi.oxauth.model.configuration.AppConfiguration getOxauthAppConfiguration() throws IOException {
		return jsonService.jsonToObject(getOxAuthDynamicConfigJson(), org.xdi.oxauth.model.configuration.AppConfiguration.class);
	}

	public boolean saveOxTrustappConfiguration(AppConfiguration oxTrustappConfiguration) {
		oxTrustConfigurationService.save(oxTrustappConfiguration);
		return true;
	}

	public boolean saveOxTrustImportPersonConfiguration(ImportPersonConfig oxTrustImportPersonConfiguration) {
		oxTrustConfigurationService.save(oxTrustImportPersonConfiguration);
		return true;
	}

	public boolean saveOxTrustCacheRefreshConfiguration(CacheRefreshConfiguration oxTrustCacheRefreshConfiguration) {
		oxTrustConfigurationService.save(oxTrustCacheRefreshConfiguration);
		return true;
	}

	public boolean saveOxAuthAppConfiguration(org.xdi.oxauth.model.configuration.AppConfiguration appConfiguration) {
		try {
			String appConfigurationJson = jsonService.objectToJson(appConfiguration);
			return saveOxAuthDynamicConfigJson(appConfigurationJson);
		} catch (IOException e) {
			log.error("Failed to serialize AppConfiguration", e);
		}
		return false;
	}

	public boolean saveOxAuthDynamicConfigJson(String oxAuthDynamicConfigJson) throws JsonParseException, JsonMappingException, IOException {
		oxAuthConfigurationService.save(oxAuthConfigObjectMapper.deserialize(oxAuthDynamicConfigJson));
		return true;
	}

	private LdapOxTrustConfiguration loadOxTrustConfig() {
		try {
			return oxTrustConfigurationExtractor.extract();
		} catch (BasePersistenceException ex) {
			log.error("Failed to load configuration from LDAP");
		}

		return null;
	}

}
