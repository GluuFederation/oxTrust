/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service.uma;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.PersistenceEntryManager;
import org.gluu.persist.model.base.SimpleBranch;
import org.gluu.search.filter.Filter;
import org.gluu.util.StringHelper;
import org.oxauth.persistence.model.Scope;
import org.slf4j.Logger;

/**
 * Provides operations with scope descriptions
 * 
 * @author Yuriy Movchan Date: 12/07/2012
 */
@Stateless
@Named("scopeDescriptionService")
public class ScopeDescriptionService implements Serializable {

	private static final long serialVersionUID = -3537567020929600777L;

	@Inject
	private PersistenceEntryManager ldapEntryManager;
	@Inject
	private OrganizationService organizationService;

	@Inject
	private Logger log;

	public void addBranch() {
		SimpleBranch branch = new SimpleBranch();
		branch.setOrganizationalUnitName("scopes");
		branch.setDn(getDnForScopeDescription(null));

		ldapEntryManager.persist(branch);
	}

	public boolean containsBranch() {
		return ldapEntryManager.contains(getDnForScopeDescription(null), SimpleBranch.class);
	}

	/**
	 * Create scope description branch if needed
	 */
	public void prepareScopeDescriptionBranch() {
		if (!containsBranch()) {
			addBranch();
		}
	}

	/**
	 * Get scope description by DN
	 * 
	 * @param dn
	 *            Scope description DN
	 * @return Scope description
	 */
	public Scope getScopeDescriptionByDn(String dn) {
		return ldapEntryManager.find(Scope.class, dn);
	}

	/**
	 * Add new scope description entry
	 * 
	 * @param scopeDescription
	 *            Scope description
	 */
	public void addScopeDescription(Scope scopeDescription) {
		ldapEntryManager.persist(scopeDescription);
	}

	/**
	 * Update scope description entry
	 * 
	 * @param scopeDescription
	 *            Scope description
	 */
	public void updateScopeDescription(Scope scopeDescription) {
		ldapEntryManager.merge(scopeDescription);
	}

	/**
	 * Remove scope description entry
	 * 
	 * @param scopeDescription
	 *            Scope description
	 */
	public void removeScopeDescription(Scope scopeDescription) {
		ldapEntryManager.remove(scopeDescription);
	}

	/**
	 * Check if LDAP server contains scope description with specified attributes
	 * 
	 * @return True if scope description with specified attributes exist
	 */
	public boolean containsScopeDescription(Scope scopeDescription) {
		return ldapEntryManager.contains(scopeDescription);
	}

	/**
	 * Get all scope descriptions
	 * 
	 * @return List of scope descriptions
	 */
	public List<Scope> getAllScopeDescriptions(String... ldapReturnAttributes) {
		return ldapEntryManager.findEntries(getDnForScopeDescription(null), Scope.class, null,
				ldapReturnAttributes);
	}

	/**
	 * Search scope descriptions by pattern
	 * 
	 * @param pattern
	 *            Pattern
	 * @param sizeLimit
	 *            Maximum count of results
	 * @return List of scope descriptions
	 */
	public List<Scope> findScopeDescriptions(String pattern, int sizeLimit) {
		String[] targetArray = new String[] { pattern };
		Filter oxIdFilter = Filter.createSubstringFilter("oxId", null, targetArray, null);
		Filter displayNameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
		Filter searchFilter = Filter.createORFilter(oxIdFilter, displayNameFilter);

		List<Scope> result = ldapEntryManager.findEntries(getDnForScopeDescription(null),
				Scope.class, searchFilter, sizeLimit);

		return result;
	}

	public List<Scope> getAllScopeDescriptions(int sizeLimit) {
		return ldapEntryManager.findEntries(getDnForScopeDescription(null), Scope.class, null, sizeLimit);
	}

	/**
	 * Get scope descriptions by example
	 * 
	 * @param scopeDescription
	 *            Scope description
	 * @return List of ScopeDescription which conform example
	 */
	public List<Scope> findScopeDescriptions(Scope scopeDescription) {
		return ldapEntryManager.findEntries(scopeDescription);
	}

	/**
	 * Get scope descriptions by Id
	 * 
	 * @param id
	 *            Id
	 * @return List of ScopeDescription which specified id
	 */
	public List<Scope> findScopeDescriptionsById(String id) {
		return ldapEntryManager.findEntries(getDnForScopeDescription(null), Scope.class,
				Filter.createEqualityFilter("oxId", id));
	}

	/**
	 * Generate new inum for scope description
	 * 
	 * @return New inum for scope description
	 */
	public String generateInumForNewScopeDescription() {
		Scope scopeDescription = new Scope();
		String newInum = null;
		do {
			newInum = generateInumForNewScopeDescriptionImpl();
			String newDn = getDnForScopeDescription(newInum);
			scopeDescription.setDn(newDn);
		} while (ldapEntryManager.contains(scopeDescription));

		return newInum;
	}

	/**
	 * Generate new inum for scope description
	 * 
	 * @return New inum for scope description
	 */
	private String generateInumForNewScopeDescriptionImpl() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Build DN string for scope description
	 */
	public String getDnForScopeDescription(String inum) {
		String orgDn = organizationService.getDnForOrganization();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=scopes,ou=uma,%s", orgDn);
		}

		return String.format("inum=%s,ou=scopes,ou=uma,%s", inum, orgDn);
	}

	public Scope getUmaScopeByInum(String inum) {
		Scope umaScope = null;
		try {
			umaScope = ldapEntryManager.find(Scope.class, getDnForScopeDescription(inum));
		} catch (Exception e) {
			log.error("Failed to find scope by Inum " + inum, e);
		}

		return umaScope;
	}

	public Scope getScopeByDn(String Dn) {
		try {
			return ldapEntryManager.find(Scope.class, Dn);
		} catch (Exception e) {
			log.warn("", e);
			return null;
		}
	}

}
