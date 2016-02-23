/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service.uma;

import java.io.Serializable;
import java.util.List;

import org.gluu.oxtrust.ldap.service.OrganizationService;
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
import org.xdi.oxauth.model.uma.persistence.ScopeDescription;
import org.xdi.util.INumGenerator;

import com.unboundid.ldap.sdk.Filter;

/**
 * Provides operations with scope descriptions
 * 
 * @author Yuriy Movchan Date: 12/07/2012
 */
@Scope(ScopeType.STATELESS)
@Name("scopeDescriptionService")
@AutoCreate
public class ScopeDescriptionService implements Serializable {

	private static final long serialVersionUID = -3537567020929600777L;

	@In
	private LdapEntryManager ldapEntryManager;

	@Logger
	private Log log;

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
	 * @param DN Scope description DN
	 * @return Scope description
	 */
	public ScopeDescription getScopeDescriptionByDn(String dn) {
		return ldapEntryManager.find(ScopeDescription.class, dn);
	}

	/**
	 * Add new scope description entry
	 * 
	 * @param scopeDescription
	 *            Scope description
	 */
	public void addScopeDescription(ScopeDescription scopeDescription) {
		ldapEntryManager.persist(scopeDescription);
	}

	/**
	 * Update scope description entry
	 * 
	 * @param scopeDescription Scope description
	 */
	public void updateScopeDescription(ScopeDescription scopeDescription) {
		ldapEntryManager.merge(scopeDescription);
	}

	/**
	 * Remove scope description entry
	 * 
	 * @param scopeDescription Scope description
	 */
	public void removeScopeDescription(ScopeDescription scopeDescription) {
		ldapEntryManager.remove(scopeDescription);
	}

	/**
	 * Check if LDAP server contains scope description with specified attributes
	 * 
	 * @return True if scope description with specified attributes exist
	 */
	public boolean containsScopeDescription(ScopeDescription scopeDescription) {
		return ldapEntryManager.contains(scopeDescription);
	}

	/**
	 * Get all scope descriptions
	 * 
	 * @return List of scope descriptions
	 */
	public List<ScopeDescription> getAllScopeDescriptions(String... ldapReturnAttributes) {
		return ldapEntryManager.findEntries(getDnForScopeDescription(null), ScopeDescription.class, ldapReturnAttributes, null);
	}

	/**
	 * Search scope descriptions by pattern
	 * 
	 * @param pattern Pattern
	 * @param sizeLimit Maximum count of results
	 * @return List of scope descriptions
	 */
	public List<ScopeDescription> findScopeDescriptions(String pattern, int sizeLimit) {
		String[] targetArray = new String[] { pattern };
		Filter oxIdFilter = Filter.createSubstringFilter("oxId", null, targetArray, null);
		Filter displayNameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
		Filter searchFilter = Filter.createORFilter(oxIdFilter, displayNameFilter);

		List<ScopeDescription> result = ldapEntryManager.findEntries(getDnForScopeDescription(null), ScopeDescription.class, searchFilter, 0, sizeLimit);

		return result;
	}

	/**
	 * Get scope descriptions by example
	 * 
	 * @param scopeDescription Scope description
	 * @return List of ScopeDescription which conform example
	 */
	public List<ScopeDescription> findScopeDescriptions(ScopeDescription scopeDescription) {
		return ldapEntryManager.findEntries(scopeDescription);
	}
	/**
	 * Get scope descriptions by Id
	 * 
	 * @param id Id
	 * @return List of ScopeDescription which specified id
	 */
	public List<ScopeDescription> findScopeDescriptionsById(String id) {
		return ldapEntryManager.findEntries(getDnForScopeDescription(null), ScopeDescription.class,
				Filter.createEqualityFilter("oxId", id));
	}

	/**
	 * Generate new inum for scope description
	 * 
	 * @return New inum for scope description
	 */
	public String generateInumForNewScopeDescription() {
		ScopeDescription scopeDescription = new ScopeDescription();
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
		String orgInum = OrganizationService.instance().getInumForOrganization();
		return orgInum + OxTrustConstants.inumDelimiter + INumGenerator.generate(2);
	}

	/**
	 * Build DN string for scope description
	 */
	public String getDnForScopeDescription(String inum) {
		String orgDn = OrganizationService.instance().getDnForOrganization();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=scopes,ou=uma,%s", orgDn);
		}

		return String.format("inum=%s,ou=scopes,ou=uma,%s", inum, orgDn);
	}

	/**
	 * Get ScopeDescriptionService instance
	 * 
	 * @return ScopeDescriptionService instance
	 */
	public static ScopeDescriptionService instance() {
		return (ScopeDescriptionService) Component.getInstance(ScopeDescriptionService.class);
	}

}
