/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.ldap.model.GluuBoolean;
import org.xdi.model.ProgrammingLanguage;
import org.xdi.oxauth.model.uma.persistence.InternalExternal;
import org.xdi.service.CacheService;
import org.xdi.util.ArrayHelper;
import org.xdi.util.StringHelper;

/**
 * Provides operations with organization
 * 
 * @author Yuriy Movchan Date: 11.02.2010
 */
@Scope(ScopeType.STATELESS)
@Name("organizationService")
@AutoCreate
public class OrganizationService extends org.xdi.service.OrganizationService{

	private static final long serialVersionUID = -1959146007518514678L;

	@In
	private CacheService cacheService;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	/**
	 * Update organization entry
	 * 
	 * @param organization
	 *            Organization
	 */
	public void updateOrganization(GluuOrganization organization) {
		getLdapEntryManager().merge(organization);

	}

	/**
	 * Get organizationService instance
	 * 
	 * @return OrganizationService instance
	 */
	public static OrganizationService instance() {
		return (OrganizationService) Component.getInstance(OrganizationService.class);
	}
	
	/**
	 * Check if LDAP server contains organization with specified attributes
	 * 
	 * @return True if organization with specified attributes exist
	 */
	public boolean containsOrganization(GluuOrganization organization) {
		return getLdapEntryManager().contains(organization);
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
		String key = OxTrustConstants.CACHE_ORGANIZATION_KEY + "_" + inum;
		GluuOrganization organization = (GluuOrganization) cacheService.get(OxTrustConstants.CACHE_APPLICATION_NAME, key);
		if (organization == null) {
			organization = getLdapEntryManager().find(GluuOrganization.class, getDnForOrganization(inum));
			cacheService.put(OxTrustConstants.CACHE_APPLICATION_NAME, key, organization);
		}

		return organization;
	}

	public String getDnForOrganization(String inum) {
		return getDnForOrganization(inum, applicationConfiguration.getBaseDN());
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
		Map<String, String> organizationCustomMessage = (Map<String, String>) cacheService.get(OxTrustConstants.CACHE_APPLICATION_NAME, key);
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
			cacheService.put(OxTrustConstants.CACHE_APPLICATION_NAME, key, organizationCustomMessage);
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
	 * Remove organization from cache after receiving event that organization
	 * were changed
	 */
	@Observer(OxTrustConstants.EVENT_CLEAR_ORGANIZATION)
	public void clearOrganizationCache() throws Exception {
		getLog().debug("Removing organization from cache");
		cacheService.removeAll(OxTrustConstants.CACHE_APPLICATION_NAME);
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
		return applicationConfiguration.getBaseDN();
	}

	/**
	 * Get Inum for organization
	 * 
	 * @return Inum for organization
	 */
	public String getInumForOrganization() {
		return applicationConfiguration.getOrgInum();
	}

	public boolean isAllowPersonModification() {
		return applicationConfiguration.isAllowPersonModification(); // todo &&
																		// ApplianceService.instance().getAppliance().getManageIdentityPermission()
																		// !=
																		// null
																		// &&
																		// ApplianceService.instance().getAppliance().getProfileManagment().isBooleanValue();
	}

	public String getOrganizationInum() {
		return applicationConfiguration.getOrgInum();
	}



	public GluuBoolean[] getBooleanSelectionTypes() {
		return new GluuBoolean[] { GluuBoolean.DISABLED, GluuBoolean.ENABLED };
	}

	public GluuBoolean[] getJavaBooleanSelectionTypes() {
		return new GluuBoolean[] { GluuBoolean.TRUE, GluuBoolean.FALSE };
	}

	public ProgrammingLanguage[] getProgrammingLanguageTypes() {
		return new ProgrammingLanguage[] { ProgrammingLanguage.PYTHON, ProgrammingLanguage.JAVA_SCRIPT };
	}

	public InternalExternal[] getInternalExternalTypes() {
		return new InternalExternal[] { InternalExternal.INTERNAL, InternalExternal.EXTERNAL };
	}

}
