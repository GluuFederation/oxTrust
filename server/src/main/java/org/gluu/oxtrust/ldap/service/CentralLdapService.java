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
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Provides operations with central LDAP server
 * 
 * @author Yuriy Movchan Date: 11.23.2010
 */
@Scope(ScopeType.STATELESS)
@Name("centralLdapService")
@AutoCreate
public class CentralLdapService {

	@In(required = false)
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
