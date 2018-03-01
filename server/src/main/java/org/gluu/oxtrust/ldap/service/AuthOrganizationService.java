/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.model.GluuOrganization;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.ldap.impl.LdapEntryManager;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.service.CacheService;
import org.xdi.util.ArrayHelper;
import org.xdi.util.OxConstants;
import org.xdi.util.StringHelper;
import static org.gluu.oxtrust.ldap.service.AppInitializer.LDAP_ENTRY_MANAGER_NAME;

/**
 * Provides operations with organization
 * 
 * @author Yuriy Movchan Date: 11.02.2010
 */
@Stateless
@Named
public class AuthOrganizationService implements Serializable {

	private static final long serialVersionUID = 5537567020929600777L;

	@Inject @Named(LDAP_ENTRY_MANAGER_NAME)
	private LdapEntryManager ldapAuthEntryManager;

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
	public void updateOrganization(GluuOrganization organization) throws Exception {
		ldapAuthEntryManager.merge(organization);

	}

	/**
	 * Get organization
	 * 
	 * @return Organization entry
	 */
	public GluuOrganization getOrganization() throws Exception {
		return getOrganizationByInum(getInumForOrganization());
	}

	/**
	 * Get organization by DN
	 * 
	 * @param inum
	 *            inum
	 * @return Organization
	 */
	public GluuOrganization getOrganizationByInum(String inum) throws Exception {
		String key = OxConstants.CACHE_ORGANIZATION_KEY + "_" + inum;
		GluuOrganization organization = (GluuOrganization) cacheService.get(OxConstants.CACHE_APPLICATION_NAME, key);
		if (organization == null) {
			organization = ldapAuthEntryManager.find(GluuOrganization.class, getDnForOrganization(inum));
			cacheService.put(OxConstants.CACHE_APPLICATION_NAME, key, organization);

		}

		return organization;
	}

	/**
	 * Returns custom message defined for the organization
	 * 
	 * @param customMessageId
	 *            message id
	 * @return custom message
	 */
	public String getOrganizationCustomMessage(String customMessageId) throws Exception {
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

	public String[] buildOrganizationCustomMessages(String[][] customMessages) throws Exception {
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
	public String getDnForOrganization() throws Exception {
		return getDnForOrganization(getOrganizationInum());
	}

	/**
	 * Build DN string for organization
	 * 
	 * @return DN string for organization
	 */
	public String getDnForOrganization(String inum) throws Exception {
		return String.format("o=%s,%s", inum, appConfiguration.getBaseDN());
	}

	/**
	 * Build DN string for organization
	 * 
	 * @return DN string for organization
	 */
	public String getBaseDn() throws Exception {
		return appConfiguration.getBaseDN();
	}

	/**
	 * Get Inum for organization
	 * 
	 * @return Inum for organization
	 */
	public String getInumForOrganization() throws Exception {
		return appConfiguration.getOrgInum();
	}

	public boolean isAllowPersonModification() throws Exception {
		return appConfiguration.isAllowPersonModification(); // todo &&
																		// applianceService.getAppliance().getManageIdentityPermission()
																		// !=
																		// null
																		// &&
																		// applianceService.getAppliance().getProfileManagment().isBooleanValue();
	}

	public String getOrganizationInum() throws Exception {
		return appConfiguration.getOrgInum();
	}

}