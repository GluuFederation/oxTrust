package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.util.UUID;

import javax.inject.Inject;

import org.gluu.oxtrust.model.SamlAcr;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;

public class SamlAcrService implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5692082015849025306L;

	@Inject
	private Logger log;

	@Inject
	private PersistenceEntryManager ldapEntryManager;

	@Inject
	private OrganizationService organizationService;

	public String getDn(String inum) {
		String orgDn = organizationService.getDnForOrganization();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=samlAcrs,%s", orgDn);
		}

		return String.format("inum=%s,ou=samlAcrs,%s", inum, orgDn);
	}

	public SamlAcr getByInum(String inum) {
		SamlAcr samlAcr = null;
		try {
			samlAcr = ldapEntryManager.find(SamlAcr.class, getDn(inum));
		} catch (Exception e) {
			log.error("Failed to find samlAcr by Inum " + inum, e);
		}
		return samlAcr;
	}

	public void update(SamlAcr samlAcr) {
		ldapEntryManager.merge(samlAcr);
	}

	public void add(SamlAcr samlAcr) {
		ldapEntryManager.persist(samlAcr);
	}

	public void remove(SamlAcr samlAcr) {
		if (samlAcr.getDn() == null) {
			samlAcr.setDn(getDn(samlAcr.getInum()));
		}
		ldapEntryManager.remove(samlAcr);
	}

	public SamlAcr[] getAll() {
		return ldapEntryManager.findEntries(getDn(null), SamlAcr.class, null).stream()
				.toArray(size -> new SamlAcr[size]);
	}

	public boolean contains(SamlAcr samlAcr) {
		boolean result = false;
		try {
			result = ldapEntryManager.contains(samlAcr);
		} catch (Exception e) {
			log.debug(e.getMessage(), e);
		}
		return result;
	}

	public String generateInumForSamlAcr() {
		SamlAcr samlAcr = null;
		String newInum = null;
		String newDn = null;
		do {
			newInum = generateInumImpl();
			newDn = getDn(newInum);
			samlAcr = new SamlAcr();
			samlAcr.setDn(newDn);
		} while (ldapEntryManager.contains(newDn, SamlAcr.class));
		return newInum;
	}

	private String generateInumImpl() {
		return UUID.randomUUID().toString();
	}

}
