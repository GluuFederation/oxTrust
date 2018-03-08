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
import org.xdi.oxauth.model.uma.persistence.UmaResource;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

/**
 * Provides operations with resources
 * 
 * @author Yuriy Movchan Date: 12/06/2012
 */
@Stateless
@Named("umaResourcesService")
public class ResourceSetService implements Serializable {

	private static final long serialVersionUID = -1537567020929600777L;

	@Inject
	private LdapEntryManager ldapEntryManager;	
	@Inject
	private OrganizationService organizationService;

	@Inject
	private Logger log;

	public void addBranch() {
		SimpleBranch branch = new SimpleBranch();
		branch.setOrganizationalUnitName("resources");
		branch.setDn(getDnForResource(null));

		ldapEntryManager.persist(branch);
	}

	public boolean containsBranch() {
		return ldapEntryManager.contains(SimpleBranch.class, getDnForResource(null));
	}

	/**
	 * Create resource branch if needed
	 */
	public void prepareResourceBranch() {
		if (!containsBranch()) {
			addBranch();
		}
	}

	/**
	 * Add new resource entry
	 * 
	 * @param resource Resource
	 */
	public void addResource(UmaResource resource) {
		ldapEntryManager.persist(resource);
	}

	/**
	 * Update resource entry
	 * 
	 * @param resource Resource
	 */
	public void updateResource(UmaResource resource) {
		ldapEntryManager.merge(resource);
	}

	/**
	 * Remove resource entry
	 * 
	 * @param resource Resource
	 */
	public void removeResource(UmaResource resource) {
		ldapEntryManager.remove(resource);
	}

	/**
	 * Check if LDAP server contains resource with specified attributes
	 * 
	 * @return True if resource with specified attributes exist
	 */
	public boolean containsResource(UmaResource resource) {
		return ldapEntryManager.contains(resource);
	}
	
  public List<UmaResource> getAllResources(int sizeLimit) {
		return ldapEntryManager.findEntries(getDnForResource(null), UmaResource.class, null, sizeLimit);
    }

	/**
	 * Get all resources
	 * 
	 * @return List of resources
	 */
	public List<UmaResource> getAllResources(String... ldapReturnAttributes) {
		return ldapEntryManager.findEntries(getDnForResource(null), UmaResource.class, null, ldapReturnAttributes);
	}

	/**
	 * Search resources by pattern
	 * 
	 * @param pattern Pattern
	 * @param sizeLimit Maximum count of results
	 * @return List of resources
	 */
	public List<UmaResource> findResources(String pattern, int sizeLimit) {
		String[] targetArray = new String[] { pattern };
		Filter oxIdFilter = Filter.createSubstringFilter("oxId", null, targetArray, null);
		Filter displayNameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
		Filter searchFilter = Filter.createORFilter(oxIdFilter, displayNameFilter);

		List<UmaResource> result = ldapEntryManager.findEntries(getDnForResource(null), UmaResource.class, searchFilter, sizeLimit);

		return result;
	}

	/**
	 * Get resources by example
	 * 
	 * @param resource Resource
	 * @return List of Resources which conform example
	 */
	public List<UmaResource> findResourceSets(UmaResource resource) {
		return ldapEntryManager.findEntries(resource);
	}

	/**
	 * Get resources by Id
	 * 
	 * @param id Id
	 * @return List of Resources which specified id
	 */
	public List<UmaResource> findResourcesById(String id) {
		return ldapEntryManager.findEntries(getDnForResource(null), UmaResource.class, Filter.createEqualityFilter("oxId", id));
	}

	/**
	 * Get resource set by DN
	 * 
	 * @param dn Resource set DN
	 * @return Resource set
	 */
	public UmaResource getResourceByDn(String dn) {
		return ldapEntryManager.find(UmaResource.class, dn);
	}

	/**
	 * Generate new inum for resource set
	 * 
	 * @return New inum for resource set
	 */
	public String generateInumForNewResource() {
		UmaResource resource = new UmaResource();
		String newInum = null;
		do {
			newInum = generateInumForNewResourceImpl();
			String newDn = getDnForResource(newInum);
			resource.setDn(newDn);
		} while (ldapEntryManager.contains(resource));

		return newInum;
	}

	/**
	 * Generate new inum for resource set
	 * 
	 * @return New inum for resource set
	 */
	private String generateInumForNewResourceImpl() {
		String orgInum = organizationService.getInumForOrganization();
		return orgInum + OxTrustConstants.inumDelimiter + INumGenerator.generate(2);
	}

	/**
	 * Build DN string for resource
	 */
	public String getDnForResource(String oxId) {
		String orgDn = organizationService.getDnForOrganization();
		if (StringHelper.isEmpty(oxId)) {
			return String.format("ou=resources,ou=uma,%s", orgDn);
		}

		return String.format("oxId=%s,ou=resources,ou=uma,%s", oxId, orgDn);
	}
	
	/**
	 * Get resources by scope
	 * 
	 * @param id Id
	 * @return List of Resources which specified scope
	 */
	public List<UmaResource> findResourcesByScope(String scopeId) {
		return ldapEntryManager.findEntries(getDnForResource(null), UmaResource.class, Filter.createEqualityFilter("oxAuthUmaScope", scopeId));
	}

}
