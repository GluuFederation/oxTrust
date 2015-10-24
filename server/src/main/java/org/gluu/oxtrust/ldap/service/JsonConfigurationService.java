/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.IOException;
import java.io.Serializable;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xdi.config.oxtrust.LdapOxAuthConfiguration;
import org.xdi.config.oxtrust.LdapOxTrustConfiguration;

/**
 * Provides operations with JSON oxAuth/oxTrust configuration
 * 
 * @author Yuriy Movchan Date: 12.15.2010
 */
@Scope(ScopeType.STATELESS)
@Name("jsonConfigurationService")
@AutoCreate
public class JsonConfigurationService implements Serializable {

	private static final long serialVersionUID = -3840968275007784641L;

	@Logger
	private Log log;

	@In
	private LdapEntryManager ldapEntryManager;

	@In(value = "#{oxTrustConfiguration.configurationDn}")
	private String configurationDn;

	public String getOxTrustConfigJson() throws JsonGenerationException, JsonMappingException, IOException {
		LdapOxTrustConfiguration ldapOxTrustConfiguration = getOxTrustConfiguration();
		return ldapOxTrustConfiguration.getApplication();
	}

	public String getOxTrustCacheRefreshConfigJson() throws JsonGenerationException, JsonMappingException, IOException {
		LdapOxTrustConfiguration ldapOxTrustConfiguration = getOxTrustConfiguration();
		return ldapOxTrustConfiguration.getCacheRefresh();
	}

	private LdapOxTrustConfiguration getOxTrustConfiguration() {
		LdapOxTrustConfiguration ldapOxTrustConfiguration = loadOxTrustConfig(ldapEntryManager, configurationDn);
		return ldapOxTrustConfiguration;
	}

	public String getOxAuthDynamicConfigJson() throws JsonGenerationException, JsonMappingException, IOException {
		LdapOxAuthConfiguration ldapOxAuthConfiguration = loadOxAuthConfig(ldapEntryManager, configurationDn);
		return ldapOxAuthConfiguration.getOxAuthConfigDynamic();
	}

	public boolean saveOxTrustConfigJson(String oxTrustConfigJson) throws JsonParseException, JsonMappingException, IOException {
		LdapOxTrustConfiguration ldapOxTrustConfiguration = getOxTrustConfiguration();
		ldapOxTrustConfiguration.setApplication(oxTrustConfigJson);
		ldapOxTrustConfiguration.setRevision(ldapOxTrustConfiguration.getRevision() + 1);
		ldapEntryManager.merge(ldapOxTrustConfiguration);
		return true;
	}

	public boolean saveOxTrustCacheRefreshConfigJson(String oxTrustConfigCacheRefreshJson) throws JsonParseException, JsonMappingException, IOException {
		LdapOxTrustConfiguration ldapOxTrustConfiguration = getOxTrustConfiguration();
		ldapOxTrustConfiguration.setCacheRefresh(oxTrustConfigCacheRefreshJson);
		ldapOxTrustConfiguration.setRevision(ldapOxTrustConfiguration.getRevision() + 1);
		ldapEntryManager.merge(ldapOxTrustConfiguration);
		return true;
	}

	public boolean saveOxAuthDynamicConfigJson(String oxAuthDynamicConfigJson) throws JsonParseException, JsonMappingException, IOException {
		LdapOxAuthConfiguration ldapOxAuthConfiguration = loadOxAuthConfig(ldapEntryManager, configurationDn);
		ldapOxAuthConfiguration.setOxAuthConfigDynamic(oxAuthDynamicConfigJson);
		ldapOxAuthConfiguration.setRevision(ldapOxAuthConfiguration.getRevision() + 1);
		ldapEntryManager.merge(ldapOxAuthConfiguration);
		return true;
	}

	private LdapOxTrustConfiguration loadOxTrustConfig(LdapEntryManager ldapEntryManager, String configurationDn) {
		try {
			LdapOxTrustConfiguration conf = ldapEntryManager.find(LdapOxTrustConfiguration.class, configurationDn);

			return conf;
		} catch (LdapMappingException ex) {
			log.error("Failed to load configuration from LDAP");
		}

		return null;
	}

	private LdapOxAuthConfiguration loadOxAuthConfig(LdapEntryManager ldapEntryManager, String configurationDn) {
		try {
			configurationDn = configurationDn.replace("ou=oxtrust", "ou=oxauth");
			LdapOxAuthConfiguration conf = ldapEntryManager.find(LdapOxAuthConfiguration.class, configurationDn);
			return conf;
		} catch (LdapMappingException ex) {
			log.error("Failed to load configuration from LDAP");
		}

		return null;
	}

	public static JsonConfigurationService instance() {
		return (JsonConfigurationService) Component.getInstance(JsonConfigurationService.class);
	}
}
