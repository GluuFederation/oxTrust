/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.push;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.model.push.PushApplication;
import org.gluu.oxtrust.service.OrganizationService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.model.base.SimpleBranch;
import org.gluu.search.filter.Filter;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

/**
 * Provides operations with oxPush applications
 * 
 * @author Yuriy Movchan Date: 01/22/2014
 */
@Stateless
@Named("pushApplicationService")
public class PushApplicationService implements Serializable {

	private static final long serialVersionUID = -1537567020929607771L;

	@Inject
	private OrganizationService organizationService;

	@Inject
	private PersistenceEntryManager ldapEntryManager;
	@Inject
	private Logger log;

	public void addBranch() {
		SimpleBranch branch = new SimpleBranch();
		branch.setOrganizationalUnitName("application");
		branch.setDn(getDnForPushApplication(null));

		ldapEntryManager.persist(branch);
	}

	public boolean containsBranch() {
		return ldapEntryManager.contains(getDnForPushApplication(null), SimpleBranch.class);
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
	public boolean containsPushApplication(String dn) {
		return ldapEntryManager.contains(dn, PushApplication.class);
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
		return ldapEntryManager.findEntries(getDnForPushApplication(null), PushApplication.class, null, ldapReturnAttributes);
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
		String newDn = null;
		String newInum = null;
		do {
			newInum = generateInumForNewPushApplicationImpl();
			newDn = getDnForPushApplication(newInum);
		} while (containsPushApplication(newDn));

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
		String orgDn = organizationService.getDnForOrganization();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=application,ou=push,%s", orgDn);
		}

		return String.format("inum=%s,ou=application,ou=push,%s", inum, orgDn);
	}

}
