/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service.uma;

import org.gluu.search.filter.Filter;
import org.gluu.oxtrust.ldap.service.OrganizationService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.ldap.impl.LdapEntryManager;
import org.gluu.persist.model.base.SimpleBranch;
import org.slf4j.Logger;
import org.xdi.oxauth.model.uma.persistence.UmaScopeDescription;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

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
	private LdapEntryManager ldapEntryManager;	
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
		return ldapEntryManager.contains(SimpleBranch.class, getDnForScopeDescription(null));
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
	 * @param dn Scope description DN
	 * @return Scope description
	 */
	public UmaScopeDescription getScopeDescriptionByDn(String dn) {
		return ldapEntryManager.find(UmaScopeDescription.class, dn);
	}

	/**
	 * Add new scope description entry
	 * 
	 * @param scopeDescription
	 *            Scope description
	 */
	public void addScopeDescription(UmaScopeDescription scopeDescription) {
		ldapEntryManager.persist(scopeDescription);
	}

	/**
	 * Update scope description entry
	 * 
	 * @param scopeDescription Scope description
	 */
	public void updateScopeDescription(UmaScopeDescription scopeDescription) {
		ldapEntryManager.merge(scopeDescription);
	}

	/**
	 * Remove scope description entry
	 * 
	 * @param scopeDescription Scope description
	 */
	public void removeScopeDescription(UmaScopeDescription scopeDescription) {
		ldapEntryManager.remove(scopeDescription);
	}

	/**
	 * Check if LDAP server contains scope description with specified attributes
	 * 
	 * @return True if scope description with specified attributes exist
	 */
	public boolean containsScopeDescription(UmaScopeDescription scopeDescription) {
		return ldapEntryManager.contains(scopeDescription);
	}

	/**
	 * Get all scope descriptions
	 * 
	 * @return List of scope descriptions
	 */
	public List<UmaScopeDescription> getAllScopeDescriptions(String... ldapReturnAttributes) {
		return ldapEntryManager.findEntries(getDnForScopeDescription(null), UmaScopeDescription.class, null, ldapReturnAttributes);
	}

	/**
	 * Search scope descriptions by pattern
	 * 
	 * @param pattern Pattern
	 * @param sizeLimit Maximum count of results
	 * @return List of scope descriptions
	 */
	public List<UmaScopeDescription> findScopeDescriptions(String pattern, int sizeLimit) {
		String[] targetArray = new String[] { pattern };
		Filter oxIdFilter = Filter.createSubstringFilter("oxId", null, targetArray, null);
		Filter displayNameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
		Filter searchFilter = Filter.createORFilter(oxIdFilter, displayNameFilter);

		List<UmaScopeDescription> result = ldapEntryManager.findEntries(getDnForScopeDescription(null), UmaScopeDescription.class, searchFilter, sizeLimit);

		return result;
	}
	
	  public List<UmaScopeDescription> getAllScopeDescriptions(int sizeLimit) {
			return ldapEntryManager.findEntries(getDnForScopeDescription(null), UmaScopeDescription.class, null, sizeLimit);
	    }


	/**
	 * Get scope descriptions by example
	 * 
	 * @param scopeDescription Scope description
	 * @return List of ScopeDescription which conform example
	 */
	public List<UmaScopeDescription> findScopeDescriptions(UmaScopeDescription scopeDescription) {
		return ldapEntryManager.findEntries(scopeDescription);
	}
	/**
	 * Get scope descriptions by Id
	 * 
	 * @param id Id
	 * @return List of ScopeDescription which specified id
	 */
	public List<UmaScopeDescription> findScopeDescriptionsById(String id) {
		return ldapEntryManager.findEntries(getDnForScopeDescription(null), UmaScopeDescription.class,
				Filter.createEqualityFilter("oxId", id));
	}

	/**
	 * Generate new inum for scope description
	 * 
	 * @return New inum for scope description
	 */
	public String generateInumForNewScopeDescription() {
        UmaScopeDescription scopeDescription = new UmaScopeDescription();
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
		String orgInum = organizationService.getInumForOrganization();
		return orgInum + OxTrustConstants.inumDelimiter + INumGenerator.generate(2);
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

}
