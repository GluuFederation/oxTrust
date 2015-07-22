/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.push;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.model.push.PushApplication;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.hibernate.annotations.common.util.StringHelper;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.xdi.ldap.model.SimpleBranch;

import com.unboundid.ldap.sdk.Filter;

/**
 * Provides operations with oxPush applications
 * 
 * @author Yuriy Movchan Date: 01/22/2014
 */
@Scope(ScopeType.STATELESS)
@Name("pushApplicationService")
@AutoCreate
public class PushApplicationService implements Serializable {

	private static final long serialVersionUID = -1537567020929607771L;

	@In
	private LdapEntryManager ldapEntryManager;

	@Logger
	private Log log;

	public void addBranch() {
		SimpleBranch branch = new SimpleBranch();
		branch.setOrganizationalUnitName("application");
		branch.setDn(getDnForPushApplication(null));

		ldapEntryManager.persist(branch);
	}

	public boolean containsBranch() {
		return ldapEntryManager.contains(SimpleBranch.class, getDnForPushApplication(null));
	}

	/**
	 * Create oxPush Application branch if needed
	 */
	public void preparePushApplicationBranch() {
		if (!containsBranch()) {
			addBranch();
		}
	}

	/**
	 * Get oxPush Application by DN
	 * 
	 * @param DN oxPush Application DN
	 * @return oxPush Application
	 */
	public PushApplication getPushApplicationByDn(String dn) {
		return ldapEntryManager.find(PushApplication.class, dn);
	}

	/**
	 * Add new oxPush Application entry
	 * 
	 * @param PushApplication oxPush Application
	 */
	public void addPushApplication(PushApplication pushApplication) {
		ldapEntryManager.persist(pushApplication);
	}

	/**
	 * Update oxPush Application entry
	 * 
	 * @param PushApplication oxPush Application
	 */
	public void updatePushApplication(PushApplication pushApplication) {
		ldapEntryManager.merge(pushApplication);
	}

	/**
	 * Remove oxPush Application entry
	 * 
	 * @param PushApplication oxPush Application
	 */
	public void removePushApplication(PushApplication pushApplication) {
		ldapEntryManager.remove(pushApplication);
	}

	/**
	 * Check if LDAP server contains oxPush Application with specified attributes
	 * 
	 * @return True if oxPush Application with specified attributes exist
	 */
	public boolean containsPushApplication(PushApplication pushApplication) {
		return ldapEntryManager.contains(pushApplication);
	}

	/**
	 * Get oxPush applications by example
	 * 
	 * @param PushApplication pushApplication
	 * @return List of PushApplications which conform example
	 */
	public List<PushApplication> findPushApplications(PushApplication pushApplication) {
		return ldapEntryManager.findEntries(pushApplication);
	}

	/**
	 * Get all oxPush applications
	 * 
	 * @return List of oxPush applications
	 */
	public List<PushApplication> getAllPushApplications(String... ldapReturnAttributes) {
		return ldapEntryManager.findEntries(getDnForPushApplication(null), PushApplication.class, ldapReturnAttributes, null);
	}

	/**
	 * Search oxPush applications by pattern
	 * 
	 * @param pattern Pattern
	 * @param sizeLimit Maximum count of results
	 * @return List of oxPush applications
	 */
	public List<PushApplication> findPushApplications(String pattern, int sizeLimit) {
		String[] targetArray = new String[] { pattern };
		Filter oxIdFilter = Filter.createSubstringFilter("oxId", null, targetArray, null);
		Filter oxNameFilter = Filter.createSubstringFilter("oxName", null, targetArray, null);
		Filter displayNameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
		Filter searchFilter = Filter.createORFilter(oxIdFilter, oxNameFilter, displayNameFilter);

		List<PushApplication> result = ldapEntryManager.findEntries(getDnForPushApplication(null), PushApplication.class, searchFilter, sizeLimit);

		return result;
	}

	/**
	 * Generate new inum for oxPush Application
	 * 
	 * @return New inum for oxPush Application
	 */
	public String generateInumForNewPushApplication() {
		PushApplication pushApplication = new PushApplication();
		String newInum = null;
		do {
			newInum = generateInumForNewPushApplicationImpl();
			String newDn = getDnForPushApplication(newInum);
			pushApplication.setDn(newDn);
		} while (ldapEntryManager.contains(pushApplication));

		return newInum;
	}

	/**
	 * Generate new inum for oxPush Application
	 * 
	 * @return New inum for oxPush Application
	 */
	private String generateInumForNewPushApplicationImpl() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Build DN string for oxPush Application
	 */
	public String getDnForPushApplication(String inum) {
		String orgDn = OrganizationService.instance().getDnForOrganization();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=application,ou=push,%s", orgDn);
		}

		return String.format("inum=%s,ou=application,ou=push,%s", inum, orgDn);
	}

	/**
	 * Get PushApplicationService instance
	 * 
	 * @return PushApplicationService instance
	 */
	public static PushApplicationService instance() {
		return (PushApplicationService) Component.getInstance(PushApplicationService.class);
	}

}
