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
public class OxPassportService {


	@Logger
	private Log log;

	@In
	private JsonService jsonService;
	
	@In
	private LdapEntryManager ldapEntryManager;
	
	@In
	private OxTrustConfiguration oxTrustConfiguration;
	
	
	public LdapOxPassportConfiguration loadConfigurationFromLdap(String ... returnAttributes) {
		FileConfiguration fc = oxTrustConfiguration.getLdapConfiguration();
		final String configurationDn = fc.getString("oxpassport_ConfigurationEntryDN");
    	final LdapEntryManager ldapEntryManager = (LdapEntryManager) Component.getInstance("ldapEntryManager");
    	//final String configurationDn = "ou=oxpassport,ou=configuration,inum=@!8214.2DD7.3392.F903!0002!1DE1.8947,ou=appliances,o=gluu";
		log.info("########## configurationDn = " + configurationDn);
        try {
            final LdapOxPassportConfiguration conf = ldapEntryManager.find(LdapOxPassportConfiguration.class, configurationDn, returnAttributes);
            log.info("########## LdapOxPassportConfiguration  status = " + conf.getStatus());
            log.info("########## LdapOxPassportConfiguration  size = " + conf.getPassportConfigurations().size());
            log.info("########## LdapOxPassportConfiguration  provider = " + conf.getPassportConfigurations().get(1).getProvider());
            log.info("########## LdapOxPassportConfiguration  2 provider = " + conf.getPassportConfigurations().get(2).getProvider());
            
            return conf;
        } catch (LdapMappingException ex) {
            log.error("Failed to load configuration from LDAP", ex);
        }
        
        return null;
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
