/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ejb.Stateless;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.exception.BasePersistenceException;
import org.gluu.persist.model.base.GluuBoolean;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.config.oxtrust.LdapOxAuthConfiguration;
import org.xdi.model.GluuStatus;
import org.xdi.model.ProgrammingLanguage;
import org.xdi.service.CacheService;
import org.xdi.util.ArrayHelper;
import org.xdi.util.OxConstants;
import org.xdi.util.StringHelper;

/**
 * Provides operations with organization
 * 
 * @author Yuriy Movchan Date: 11.02.2010
 */
@Stateless
@Named("organizationService")
public class OrganizationService extends org.xdi.service.OrganizationService {

	private static final long serialVersionUID = -1959146007518514678L;

	@Inject
    private Logger log;

    @Inject
    private PersistenceEntryManager ldapEntryManager;

	@Inject
	private CacheService cacheService;

	@Inject
	private AppConfiguration appConfiguration;

	/**
	 * Update organization entry
	 * 
	 * @param organization
	 *            Organization
	 */
	public void updateOrganization(GluuOrganization organization) {
		ldapEntryManager.merge(organization);

	}
	
	/**
	 * Check if LDAP server contains organization with specified attributes
	 * 
	 * @return True if organization with specified attributes exist
	 */
	public boolean containsOrganization(GluuOrganization organization) {
		return ldapEntryManager.contains(organization);
	}

	/**
	 * Get organization
	 * 
	 * @return Organization entry
	 */
	public GluuOrganization getOrganization() {
		return getOrganizationByInum(getInumForOrganization());
	}

	/**
	 * Get organization by DN
	 * 
	 * @param inum
	 *            inum
	 * @return Organization
	 */
	public GluuOrganization getOrganizationByInum(String inum) {
		String key = OxConstants.CACHE_ORGANIZATION_KEY + "_" + inum;
		GluuOrganization organization = (GluuOrganization) cacheService.get(OxConstants.CACHE_APPLICATION_NAME, key);
		if (organization == null) {
			organization = ldapEntryManager.find(GluuOrganization.class, getDnForOrganization(inum));
			cacheService.put(OxConstants.CACHE_APPLICATION_NAME, key, organization);
		}

		return organization;
	}

	public String getDnForOrganization(String inum) {
		return getDnForOrganization(inum, appConfiguration.getBaseDN());
	}

	/**
	 * Returns custom message defined for the organization
	 * 
	 * @param customMessageId
	 *            message id
	 * @return custom message
	 */
	public String getOrganizationCustomMessage(String customMessageId) {
		GluuOrganization organization = getOrganization();

		String key = OxTrustConstants.CACHE_ORGANIZATION_CUSTOM_MESSAGE_KEY + "_" + organization.getInum();
		@SuppressWarnings("unchecked")
		Map<String, String> organizationCustomMessage = (Map<String, String>) cacheService.get(OxConstants.CACHE_APPLICATION_NAME, key);
		if (organizationCustomMessage == null) {
			organizationCustomMessage = new HashMap<String, String>();

			String[] customMessages = organization.getCustomMessages();
			if (ArrayHelper.isNotEmpty(customMessages)) {
				for (String customMessage : customMessages) {
					int idx = customMessage.indexOf(':');
					if ((idx > 0) && (idx + 1 < customMessage.length())) {
						String msgKey = customMessage.substring(0, idx).trim();
						String msgValue = customMessage.substring(idx + 1).trim();

						if (StringHelper.isNotEmpty(msgKey) && StringHelper.isNotEmpty(msgValue)) {
							organizationCustomMessage.put(msgKey, msgValue);
						}
					}
				}
			}
			cacheService.put(OxConstants.CACHE_APPLICATION_NAME, key, organizationCustomMessage);
		}

		return organizationCustomMessage.get(customMessageId);
	}

	public String[] buildOrganizationCustomMessages(String[][] customMessages) {
		List<String> result = new ArrayList<String>();

		for (String[] customMessage : customMessages) {
			if (ArrayHelper.isEmpty(customMessage) || customMessage.length != 2) {
				continue;
			}
			String msgKey = customMessage[0];
			String msgValue = customMessage[1];

			if (StringHelper.isNotEmpty(msgKey) && StringHelper.isNotEmpty(msgValue)) {
				result.add(msgKey + ": " + msgValue);
			}
		}

		return result.toArray(new String[0]);
	}

	/**
	 * Build DN string for organization
	 * 
	 * @return DN string for organization
	 */
	public String getDnForOrganization() {
		return getDnForOrganization(getOrganizationInum());
	}



	/**
	 * Build DN string for organization
	 * 
	 * @return DN string for organization
	 */
	public String getBaseDn() {
		return appConfiguration.getBaseDN();
	}

	/**
	 * Get Inum for organization
	 * 
	 * @return Inum for organization
	 */
	public String getInumForOrganization() {
		return appConfiguration.getOrgInum();
	}

	public boolean isAllowPersonModification() {
		return appConfiguration.isAllowPersonModification(); // todo &&
																		// applianceService.getAppliance().getManageIdentityPermission()
																		// !=
																		// null
																		// &&
																		// applianceService.getAppliance().getProfileManagment().isBooleanValue();
	}

	public String getOrganizationInum() {
		return appConfiguration.getOrgInum();
	}



	public GluuBoolean[] getBooleanSelectionTypes() {
		return new GluuBoolean[] { GluuBoolean.DISABLED, GluuBoolean.ENABLED };
	}

	public GluuBoolean[] getJavaBooleanSelectionTypes() {
		return new GluuBoolean[] { GluuBoolean.TRUE, GluuBoolean.FALSE };
	}

	public GluuStatus[] getActiveInactiveStatuses() {
		return new GluuStatus[] { GluuStatus.ACTIVE, GluuStatus.INACTIVE };
	}

	public ProgrammingLanguage[] getProgrammingLanguageTypes() {
		return new ProgrammingLanguage[] { ProgrammingLanguage.PYTHON, ProgrammingLanguage.JAVA_SCRIPT };
	}

	/**
	 * Get version for organization
	 * 
	 * @return version string for organization
	 */
	public String getVersion() {
		String version  = getClass().getPackage().getImplementationVersion();
    	if (version==null) {
    	    Properties prop = new Properties();
    	    try {
    	        prop.load(FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/META-INF/MANIFEST.MF"));
    	        version = prop.getProperty("Implementation-Version");
    	    } catch (IOException e) {
    	        log.error(e.toString());
    	    }
    	}
    	log.info("Starting App version "+version);
    	if(version != null){
    		version = version.replace("-SNAPSHOT","");
    		return version;
    	}    		
       	return "";
	}
	
	public LdapOxAuthConfiguration getOxAuthSetting(String configurationDn) {
		//String configurationDn = configurationFactory.getConfigurationDn();

		LdapOxAuthConfiguration ldapOxAuthConfiguration = null;
		try {
			configurationDn = configurationDn.replace("ou=oxtrust", "ou=oxauth");
			ldapOxAuthConfiguration = ldapEntryManager.find(LdapOxAuthConfiguration.class, configurationDn);
			return ldapOxAuthConfiguration;
		} catch (BasePersistenceException ex) {
			log.error("Failed to load configuration from LDAP");
		}

		return null;
	}

	public void saveLdapOxAuthConfiguration(LdapOxAuthConfiguration ldapOxAuthConfiguration) {
		ldapEntryManager.merge(ldapOxAuthConfiguration);
		
	}
}
