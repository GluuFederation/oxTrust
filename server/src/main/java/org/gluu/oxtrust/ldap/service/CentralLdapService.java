/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import javax.inject.Inject;
import javax.inject.Named;
import javax.enterprise.context.ConversationScoped;

/**
 * Provides operations with central LDAP server
 * 
 * @author Yuriy Movchan Date: 11.23.2010
 */
@Scope(ScopeType.STATELESS)
@Named("centralLdapService")
@AutoCreate
public class CentralLdapService {

	@Inject(required = false)
	private LdapEntryManager centralLdapEntryManager;

	/**
	 * Add appliance entry
	 * 
	 * @param appliance
	 *            GluuAppliance
	 */
	public void addAppliance(GluuAppliance appliance) {
		centralLdapEntryManager.persist(appliance);
	}

	/**
	 * Update appliance entry
	 * 
	 * @param appliance
	 *            GluuAppliance
	 */
	public void updateAppliance(GluuAppliance appliance) {
		centralLdapEntryManager.merge(appliance);
	}

	/**
	 * Check if LDAP server contains appliance with specified attributes
	 * 
	 * @return True if appliance with specified attributes exist
	 */
	public boolean containsAppliance(GluuAppliance appliance) {
		return centralLdapEntryManager.contains(appliance);
	}
	
	public boolean isUseCentralServer() {
		return centralLdapEntryManager != null;
	}

}
