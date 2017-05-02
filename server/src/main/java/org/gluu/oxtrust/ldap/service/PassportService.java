/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.gluu.site.ldap.persistence.exception.MappingException;
import javax.enterprise.context.ApplicationScoped;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;
import javax.enterprise.context.ConversationScoped;
import org.slf4j.Logger;
import org.xdi.config.oxtrust.LdapOxPassportConfiguration;
import org.xdi.service.JsonService;
import org.xdi.util.StringHelper;
import org.xdi.util.properties.FileConfiguration;

/**
 * Passport service
 * 
 * @author Shekhar L
 */
@Stateless
@Named("passportService")
public class PassportService {

	@Inject
	private Logger log;

	@Inject
	private JsonService jsonService;

	@Inject
	private LdapEntryManager ldapEntryManager;

	@Inject
	private OxTrustConfiguration oxTrustConfiguration;

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
		FileConfiguration fc = oxTrustConfiguration.getLdapConfiguration();
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
		} else {
			ldapEntryManager.persist(ldapOxPassportConfiguration);
		}

	}

}
