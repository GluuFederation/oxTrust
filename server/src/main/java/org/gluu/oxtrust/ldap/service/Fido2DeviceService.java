package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.gluu.oxtrust.model.fido2.Fido2AuthenticationEntry;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.model.base.SimpleBranch;
import org.gluu.search.filter.Filter;
import org.slf4j.Logger;

public class Fido2DeviceService implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5874835162873627676L;

	@Inject
	private Logger log;

	@Inject
	private IPersonService personService;

	@Inject
	private PersistenceEntryManager ldapEntryManager;

	public String getDnForFido2Device(String userId, String id) {
		String baseDn;
		if (userId != null && !userId.isEmpty()) {
			baseDn = "ou=fido2_auth," + personService.getDnForPerson(userId);
			if (id != null && !id.isEmpty()) {
				baseDn = "oxId=" + id + "," + baseDn;
			}
		} else {
			baseDn = personService.getDnForPerson(null);
		}
		return baseDn;
	}

	public Fido2AuthenticationEntry getFido2DeviceById(String userId, String id) {
		Fido2AuthenticationEntry entry = null;
		try {
			String dn = getDnForFido2Device(userId, id);
			if (StringUtils.isNotEmpty(userId))
				entry = ldapEntryManager.find(Fido2AuthenticationEntry.class, dn);
			else {
				Filter filter = Filter.createEqualityFilter("oxId", id);
				entry = ldapEntryManager.findEntries(dn, Fido2AuthenticationEntry.class, filter).get(0);
			}
		} catch (Exception e) {
			log.error("Failed to find device by id " + id, e);
		}
		return entry;
	}

	public Fido2AuthenticationEntry searchFidoDevice(Filter filter, String userId, String id) throws Exception {
		Fido2AuthenticationEntry entry = null;
		List<Fido2AuthenticationEntry> gluuCustomFidoDevices = ldapEntryManager
				.findEntries(getDnForFido2Device(userId, id), Fido2AuthenticationEntry.class, filter, 1);
		if (gluuCustomFidoDevices != null && !gluuCustomFidoDevices.isEmpty()) {
			entry = gluuCustomFidoDevices.get(0);
		}
		return entry;
	}

	public void updateGluuCustomFidoDevice(Fido2AuthenticationEntry entry) {
		ldapEntryManager.merge(entry);
	}

	public void removeFido2Device(Fido2AuthenticationEntry gluuCustomFidoDevice) {
		ldapEntryManager.removeRecursively(gluuCustomFidoDevice.getDn());
	}

	public List<Fido2AuthenticationEntry> searchFido2Devices(String userInum, String... returnAttributes) {
		if (containsBranch(userInum)) {
			String baseDnForU2fDevices = getDnForFido2Device(userInum, null);
			return ldapEntryManager.findEntries(baseDnForU2fDevices, Fido2AuthenticationEntry.class, null,
					returnAttributes);
		}
		return null;
	}

	private boolean containsBranch(final String userInum) {
		return ldapEntryManager.contains(getDnForFido2Device(userInum, null), SimpleBranch.class);
	}

}
