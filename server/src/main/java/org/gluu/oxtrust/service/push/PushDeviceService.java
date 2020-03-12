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

import org.gluu.oxtrust.model.push.PushDevice;
import org.gluu.oxtrust.service.OrganizationService;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.model.base.SimpleBranch;
import org.gluu.search.filter.Filter;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

/**
 * Provides operations with oxPush devices
 * 
 * @author Yuriy Movchan Date: 01/22/2014
 */
@Stateless
@Named("pushDeviceService")
public class PushDeviceService implements Serializable {

	private static final long serialVersionUID = -920736838757282684L;

	@Inject
	private PersistenceEntryManager ldapEntryManager;
	@Inject
	private OrganizationService organizationService;

	@Inject
	private Logger log;

	public void addBranch() {
		SimpleBranch branch = new SimpleBranch();
		branch.setOrganizationalUnitName("device");
		branch.setDn(getDnForPushDevice(null));

		ldapEntryManager.persist(branch);
	}

	public boolean containsBranch() {
		return ldapEntryManager.contains(getDnForPushDevice(null), SimpleBranch.class);
	}

	/**
	 * Create oxPush Device branch if needed
	 */
	public void preparePushDeviceBranch() {
		if (!containsBranch()) {
			addBranch();
		}
	}

	/**
	 * Get oxPush Device by DN
	 * 
	 * @param DN oxPush Device DN
	 * @return oxPush Device
	 */
	public PushDevice getPushDeviceByDn(String dn) {
		return ldapEntryManager.find(PushDevice.class, dn);
	}

	/**
	 * Add new oxPush Device entry
	 * 
	 * @param PushDevice oxPush Device
	 */
	public void addPushDevice(PushDevice pushDevice) {
		ldapEntryManager.persist(pushDevice);
	}

	/**
	 * Update oxPush Device entry
	 * 
	 * @param PushDevice oxPush Device
	 */
	public void updatePushDevice(PushDevice pushDevice) {
		ldapEntryManager.merge(pushDevice);
	}

	/**
	 * Remove oxPush Device entry
	 * 
	 * @param PushDevice oxPush Device
	 */
	public void removePushDevice(PushDevice pushDevice) {
		ldapEntryManager.remove(pushDevice);
	}

	/**
	 * Check if LDAP server contains oxPush Device with specified attributes
	 * 
	 * @return True if oxPush Device with specified attributes exist
	 */
	public boolean containsPushDevice(String dn) {
		return ldapEntryManager.contains(dn, PushDevice.class);
	}

	/**
	 * Get oxPush devices by example
	 * 
	 * @param PushDevice pushDevice
	 * @return List of PushDevices which conform example
	 */
	public List<PushDevice> findPushDevices(PushDevice pushDevice) {
		return ldapEntryManager.findEntries(pushDevice);
	}

	/**
	 * Get all oxPush devices
	 * 
	 * @return List of oxPush devices
	 */
	public List<PushDevice> getAllPushDevices(String... ldapReturnAttributes) {
		return ldapEntryManager.findEntries(getDnForPushDevice(null), PushDevice.class, null, ldapReturnAttributes);
	}

	/**
	 * Search oxPush devices by pattern
	 * 
	 * @param pattern Pattern
	 * @param sizeLimit Maximum count of results
	 * @return List of oxPush devices
	 */
	public List<PushDevice> findPushDevices(String pattern, int sizeLimit) {
		String[] targetArray = new String[] { pattern };
		Filter oxIdFilter = Filter.createSubstringFilter("oxId", null, targetArray, null);
		Filter oxTypeFilter = Filter.createSubstringFilter("oxType", null, targetArray, null);
		Filter oxAuthUserIdFilter = Filter.createSubstringFilter("oxAuthUserId", null, targetArray, null);
		Filter searchFilter = Filter.createORFilter(oxIdFilter, oxTypeFilter, oxAuthUserIdFilter);

		List<PushDevice> result = ldapEntryManager.findEntries(getDnForPushDevice(null), PushDevice.class, searchFilter, sizeLimit);

		return result;
	}

	/**
	 * Generate new inum for oxPush Device
	 * 
	 * @return New inum for oxPush Device
	 */
	public String generateInumForNewPushDevice() {
		String newDn = null;
		String newInum = null;
		do {
			newInum = generateInumForNewPushDeviceImpl();
			newDn = getDnForPushDevice(newInum);
		} while (containsPushDevice(newDn));

		return newInum;
	}

	/**
	 * Generate new inum for oxPush Device
	 * 
	 * @return New inum for oxPush Device
	 */
	private String generateInumForNewPushDeviceImpl() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Build DN string for oxPush Device
	 */
	public String getDnForPushDevice(String inum) {
		String orgDn = organizationService.getDnForOrganization();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=device,ou=push,%s", orgDn);
		}

		return String.format("inum=%s,ou=device,ou=push,%s", inum, orgDn);
	}

}
