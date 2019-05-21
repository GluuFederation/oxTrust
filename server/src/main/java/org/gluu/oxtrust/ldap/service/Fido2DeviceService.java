package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.GluuFido2Device;
import org.gluu.search.filter.Filter;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

public class Fido2DeviceService implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5874835162873627676L;

	@Inject
	private Logger log;

	@Inject
	private PersistenceEntryManager ldapEntryManager;

	@Inject
	private OrganizationService organizationService;

	public boolean removeFido2(GluuCustomPerson person, String deviceID) {
		try {
			String finalDn = String.format("oxId=%s,ou=fido2_register,", deviceID);
			finalDn = finalDn.concat(person.getDn());
			ldapEntryManager.removeRecursively(finalDn);
			return true;
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}

	public String getDnForFido2Device(String oxid, String personInum) {
		String orgDn = organizationService.getDnForOrganization();
		if (!StringHelper.isEmpty(personInum) && StringHelper.isEmpty(oxid)) {
			return String.format("ou=fido2_register,inum=%s,ou=people,%s", personInum, orgDn);
		}
		if (!StringHelper.isEmpty(oxid) && !StringHelper.isEmpty(personInum)) {
			return String.format("oxId=%s,ou=fido2_register,inum=%s,ou=people,%s", oxid, personInum, orgDn);
		}
		return "";
	}

	public List<GluuFido2Device> findAllFido2Device(GluuCustomPerson person) {
		try {
			String baseDn = getDnForFido2Device(null, person.getInum());
			List<GluuFido2Device> results = ldapEntryManager.findEntries(baseDn, GluuFido2Device.class,
					Filter.createPresenceFilter("id"));
			if (results == null) {
				return new ArrayList<GluuFido2Device>();
			}
			return results;
		} catch (Exception e) {
			log.error("Error getting Fido2 devices", e);
			return new ArrayList<GluuFido2Device>();
		}
	}

}
