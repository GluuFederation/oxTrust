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
import org.xdi.service.JsonService;

/**
 * Provides operations with velocity templates
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
	@In
	private JsonService jsonService;
	
	private LdapOxTrustConfiguration ldapOxTrustConfiguration;
	
	private LdapOxAuthConfiguration ldapOxAuthConfiguration;
		
	@In(value = "#{oxTrustConfiguration.configurationDn}")
	private String configurationDn;
	
	public String getOxTrustConfigJson() throws JsonGenerationException, JsonMappingException, IOException{
		ldapOxTrustConfiguration = loadOxTrustConfig(ldapEntryManager, configurationDn);
		return ldapOxTrustConfiguration.getApplication();
	}
	
	public String getOxAuthDynamicConfigJson() throws JsonGenerationException, JsonMappingException, IOException{
		ldapOxAuthConfiguration= loadOxAuthConfig(ldapEntryManager, configurationDn);
		return ldapOxAuthConfiguration.getOxAuthConfigDynamic();
	}
	
	public boolean saveOxTrustConfigJson(String oxTrustConfigJson) throws JsonParseException, JsonMappingException, IOException{
		ldapOxTrustConfiguration = loadOxTrustConfig(ldapEntryManager, configurationDn);
		ldapOxTrustConfiguration.setApplication(oxTrustConfigJson);
		ldapEntryManager.merge(ldapOxTrustConfiguration);
		return true;
	}
	
	public boolean saveOxAuthDynamicConfigJson(String oxAuthDynamicConfigJson) throws JsonParseException, JsonMappingException, IOException{
		ldapOxAuthConfiguration= loadOxAuthConfig(ldapEntryManager, configurationDn);
		ldapOxAuthConfiguration.setOxAuthConfigDynamic(oxAuthDynamicConfigJson);
		ldapEntryManager.merge(ldapOxAuthConfiguration);
		return true;
	}
	
	public static JsonConfigurationService instance() {
		return (JsonConfigurationService) Component.getInstance(JsonConfigurationService.class);
	}
	
	public LdapOxTrustConfiguration loadOxTrustConfig(LdapEntryManager ldapEntryManager, String configurationDn) {
		try {
			LdapOxTrustConfiguration conf = ldapEntryManager.find(LdapOxTrustConfiguration.class, configurationDn);

			return conf;
		} catch (LdapMappingException ex) {
			log.error("Failed to load configuration from LDAP");
		}

		return null;
	}

	public LdapOxAuthConfiguration loadOxAuthConfig(LdapEntryManager ldapEntryManager, String configurationDn) {
		try {
			configurationDn = configurationDn.replace("ou=oxtrust", "ou=oxauth");
			LdapOxAuthConfiguration conf = ldapEntryManager.find(LdapOxAuthConfiguration.class, configurationDn);
//			ldapEntryManager.findEntries(LdapOxAuthConfiguration.class, 1);
			return conf;
		} catch (LdapMappingException ex) {
			log.error("Failed to load configuration from LDAP");
		}

		return null;
	}
}
