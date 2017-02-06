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
import org.xdi.oxauth.model.uma.persistence.ResourceSet;
import org.xdi.util.INumGenerator;

import com.unboundid.ldap.sdk.Filter;

/**
 * Provides operations with resource sets
 * 
 * @author Yuriy Movchan Date: 12/06/2012
 */
@Scope(ScopeType.STATELESS)
@Name("resourceSetService")
@AutoCreate
public class ResourceSetService implements Serializable {

	private static final long serialVersionUID = -1537567020929600777L;

	@In
	private LdapEntryManager ldapEntryManager;

	@Logger
	private Log log;

	public void addBranch() {
		SimpleBranch branch = new SimpleBranch();
		branch.setOrganizationalUnitName("resource_sets");
		branch.setDn(getDnForResourceSet(null));

		ldapEntryManager.persist(branch);
	}

	public boolean containsBranch() {
		return ldapEntryManager.contains(SimpleBranch.class, getDnForResourceSet(null));
	}

	/**
	 * Create resource set branch if needed
	 */
	public void prepareResourceSetBranch() {
		if (!containsBranch()) {
			addBranch();
		}
	}

	/**
	 * Add new resource set entry
	 * 
	 * @param resourceSet Resource set
	 */
	public void addResourceSet(ResourceSet resourceSet) {
		ldapEntryManager.persist(resourceSet);
	}

	/**
	 * Update resource set entry
	 * 
	 * @param resourceSet Resource set
	 */
	public void updateResourceSet(ResourceSet resourceSet) {
		ldapEntryManager.merge(resourceSet);
	}

	/**
	 * Remove resource set entry
	 * 
	 * @param resourceSet Resource set
	 */
	public void removeResourceSet(ResourceSet resourceSet) {
		ldapEntryManager.remove(resourceSet);
	}

	/**
	 * Check if LDAP server contains resource set with specified attributes
	 * 
	 * @return True if resource set with specified attributes exist
	 */
	public boolean containsResourceSet(ResourceSet resourceSet) {
		return ldapEntryManager.contains(resourceSet);
	}
	
  public List<ResourceSet> getAllResourceSets(int sizeLimit) {		
		return ldapEntryManager.findEntries(getDnForResourceSet(null), ResourceSet.class, null, 0, sizeLimit);
    }

	/**
	 * Get all resource sets
	 * 
	 * @return List of resource sets
	 */
	public List<ResourceSet> getAllResourceSets(String... ldapReturnAttributes) {
		return ldapEntryManager.findEntries(getDnForResourceSet(null), ResourceSet.class, ldapReturnAttributes, null);
	}

	/**
	 * Search resource sets by pattern
	 * 
	 * @param pattern Pattern
	 * @param sizeLimit Maximum count of results
	 * @return List of resource sets
	 */
	public List<ResourceSet> findResourceSets(String pattern, int sizeLimit) {
		String[] targetArray = new String[] { pattern };
		Filter oxIdFilter = Filter.createSubstringFilter("oxId", null, targetArray, null);
		Filter displayNameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
		Filter searchFilter = Filter.createORFilter(oxIdFilter, displayNameFilter);

		List<ResourceSet> result = ldapEntryManager.findEntries(getDnForResourceSet(null), ResourceSet.class, searchFilter, 0, sizeLimit);

		return result;
	}

	/**
	 * Get resource sets by example
	 * 
	 * @param resourceSet Resource set
	 * @return List of ResourceSets which conform example
	 */
	public List<ResourceSet> findResourceSets(ResourceSet resourceSet) {
		return ldapEntryManager.findEntries(resourceSet);
	}

	/**
	 * Get resource sets by Id
	 * 
	 * @param id Id
	 * @return List of ResourceSets which specified id
	 */
	public List<ResourceSet> findResourceSetsById(String id) {
		return ldapEntryManager.findEntries(getDnForResourceSet(null), ResourceSet.class, Filter.createEqualityFilter("oxId", id));
	}

	/**
	 * Get resource set by DN
	 * 
	 * @param DN Resource set DN
	 * @return Resource set
	 */
	public ResourceSet getResourceSetByDn(String dn) {
		return ldapEntryManager.find(ResourceSet.class, dn);
	}

	/**
	 * Generate new inum for resource set
	 * 
	 * @return New inum for resource set
	 */
	public String generateIdForNewResourceSet() {
		ResourceSet resourceSet = new ResourceSet();
		long currentTime = System.currentTimeMillis();
		String newInum = null;
		do {
			newInum = Long.toString(currentTime);
			String newDn = getDnForResourceSet(newInum);
			resourceSet.setDn(newDn);
		} while (ldapEntryManager.contains(resourceSet));

		return newInum;
	}

	/**
	 * Generate new inum for resource set
	 * 
	 * @return New inum for resource set
	 */
	public String generateInumForNewResourceSet() {
		ResourceSet resourceSet = new ResourceSet();
		String newInum = null;
		do {
			newInum = generateInumForNewResourceSetImpl();
			String newDn = getDnForResourceSet(newInum);
			resourceSet.setDn(newDn);
		} while (ldapEntryManager.contains(resourceSet));

		return newInum;
	}

	/**
	 * Generate new inum for resource set
	 * 
	 * @return New inum for resource set
	 */
	private String generateInumForNewResourceSetImpl() {
		String orgInum = OrganizationService.instance().getInumForOrganization();
		return orgInum + OxTrustConstants.inumDelimiter + INumGenerator.generate(2);
	}

	/**
	 * Build DN string for resource set
	 */
	public String getDnForResourceSet(String inum) {
		String orgDn = OrganizationService.instance().getDnForOrganization();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=resource_sets,ou=uma,%s", orgDn);
		}

		return String.format("inum=%s,ou=resource_sets,ou=uma,%s", inum, orgDn);
	}

	/**
	 * Get ResourceSetService instance
	 * 
	 * @return ResourceSetService instance
	 */
	public static ResourceSetService instance() {
		return (ResourceSetService) Component.getInstance(ResourceSetService.class);
	}

}
