/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import org.gluu.oxtrust.config.OxTrustConfiguration;
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
import org.xdi.config.oxtrust.LdapOxPassportConfiguration;
import org.xdi.service.JsonService;
import org.xdi.util.properties.FileConfiguration;

/**
 * Passport service
 * 
 * @author Shekhar L
 */
@Scope(ScopeType.STATELESS)
@Name("passportService")
@AutoCreate
public class PassportService {

	@Logger
	private Log log;

	@In
	private JsonService jsonService;

	@In
	private LdapEntryManager ldapEntryManager;

	@In
	private OxTrustConfiguration oxTrustConfiguration;

	public LdapOxPassportConfiguration loadConfigurationFromLdap() {
		try {
			String configurationDn = geConfigurationDn();
			log.debug("########## configurationDn = " + configurationDn);
			if ((configurationDn != null) && !(configurationDn.trim().equals(""))) {
				LdapEntryManager ldapEntryManager = (LdapEntryManager) Component.getInstance("ldapEntryManager");
				LdapOxPassportConfiguration conf = ldapEntryManager.find(LdapOxPassportConfiguration.class, configurationDn);
				log.info("########## LdapOxPassportConfiguration  size = '{0}'", conf.getPassportConfigurations().size());
				return conf;
			}
		} catch (LdapMappingException ex) {
			log.error("Failed to load configuration from LDAP", ex);
		} catch (Exception ex) {
			log.error("Exception ", ex);
		}

		return null;
	}

	public String geConfigurationDn() {
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
		ldapEntryManager.merge(ldapOxPassportConfiguration);

	}

}
