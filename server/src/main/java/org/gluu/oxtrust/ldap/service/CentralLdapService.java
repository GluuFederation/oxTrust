/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.model.GluuOxTrustStat;
import org.gluu.oxtrust.service.cdi.event.CentralLdap;
import org.gluu.persist.PersistenceEntryManager;

/**
 * Provides operations with central LDAP server
 * 
 * @author Yuriy Movchan Date: 11.23.2010
 */
@Stateless
@Named
public class CentralLdapService {

	@Inject @CentralLdap
	private PersistenceEntryManager centralLdapEntryManager;
	
	@Inject
	private ConfigurationFactory configurationFactory;

	/**
	 * Add configuration entry
	 * 
	 * @param configuration
	 *            GluuConfiguration
	 */
	public void addConfiguration(GluuConfiguration configuration) {
		centralLdapEntryManager.persist(configuration);
	}

	/**
	 * Update configuration entry
	 * 
	 * @param configuration
	 *            GluuConfiguration
	 */
	public void updateConfiguration(GluuConfiguration configuration) {
		centralLdapEntryManager.merge(configuration);
	}
	
	
	public void addOxtrustStat(GluuOxTrustStat gluuOxTrustStat) {
		centralLdapEntryManager.persist(gluuOxTrustStat);
	}

	public void updateOxtrustStat(GluuOxTrustStat gluuOxTrustStat) {
		centralLdapEntryManager.merge(gluuOxTrustStat);
	}


	/**
	 * Check if LDAP server contains configuration with specified attributes
	 * 
	 * @return True if configuration with specified attributes exist
	 */
	public boolean containsConfiguration(String dn) {
		return centralLdapEntryManager.contains(dn, GluuConfiguration.class);
	}
	
	public boolean containsOxtrustStatForToday(String dn) {
		return centralLdapEntryManager.contains(dn, GluuOxTrustStat.class);
	}

	public boolean isUseCentralServer() {
		return (configurationFactory.getLdapCentralConfiguration() != null)
				&& configurationFactory.getAppConfiguration().isUpdateStatus();
	}

}
