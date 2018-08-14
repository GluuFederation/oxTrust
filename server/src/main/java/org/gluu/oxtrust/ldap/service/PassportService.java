/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.exception.MappingException;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.LdapOxPassportConfiguration;
import org.xdi.util.StringHelper;
import org.xdi.util.properties.FileConfiguration;

/**
 * Passport service
 * 
 * @author Shekhar L
 */
@Stateless
@Named("passportService")
public class PassportService implements Serializable {

	private static final long serialVersionUID = -4787990021407949332L;

	@Inject
	private Logger log;
	
	@Inject
	private OxTrustAuditService oxTrustAuditService;

	@Inject
	private PersistenceEntryManager ldapEntryManager;
	@Inject
	private ConfigurationFactory configurationFactory;

	public boolean containsPassportConfiguration() {
		String configurationDn = getConfigurationDn();
		if (StringHelper.isEmpty(configurationDn)) {
			return false;
		}

		return ldapEntryManager.contains(LdapOxPassportConfiguration.class, configurationDn);
	}

	public LdapOxPassportConfiguration loadConfigurationFromLdap() {
		boolean contains = containsPassportConfiguration();
		if (contains) {
			String configurationDn = getConfigurationDn();
			try {
				return ldapEntryManager.find(LdapOxPassportConfiguration.class, configurationDn);
			} catch (MappingException ex) {
				log.error("Failed to load passport configuration from LDAP", ex);
			}
		}

		return null;
	}

	private String getConfigurationDn() {
		FileConfiguration fc = configurationFactory.getPersistenceConfiguration().getConfiguration();
		String configurationDn = fc.getString("oxpassport_ConfigurationEntryDN");
		return configurationDn;
	}

	/**
	 * Update LdapOxPassportConfiguration entry
	 * 
	 * @param LdapOxPassportConfiguration
	 *            LdapOxPassportConfiguration
	 */
	public void updateLdapOxPassportConfiguration(LdapOxPassportConfiguration ldapOxPassportConfiguration) {
		ldapOxPassportConfiguration.setDn(getConfigurationDn());

		boolean contains = containsPassportConfiguration();
		if (contains) {
			ldapEntryManager.merge(ldapOxPassportConfiguration);
			oxTrustAuditService.audit("OXPASSORT CONFIG "+ldapOxPassportConfiguration.getBaseDn()+ " SUCCESSFULLY UPDATE");
		} else {
			ldapEntryManager.persist(ldapOxPassportConfiguration);
			oxTrustAuditService.audit("OXPASSORT CONFIG "+ldapOxPassportConfiguration.getBaseDn()+ " SUCCESSFULLY ADDED");
		}

	}

}
