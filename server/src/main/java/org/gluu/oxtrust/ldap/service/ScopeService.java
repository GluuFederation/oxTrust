/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import static org.gluu.oxtrust.ldap.service.AppInitializer.LDAP_ENTRY_MANAGER_NAME;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.model.OxAuthScope;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.ldap.impl.LdapEntryManager;
import org.xdi.oxauth.model.common.ScopeType;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;

import org.gluu.search.filter.Filter;

/**
 * Provides operations with Scopes
 * 
 * @author Reda Zerrad Date: 06.18.2012
 */
@Stateless
@Named
public class ScopeService implements Serializable {

	private static final long serialVersionUID = 65734145678106186L;

	@Inject
	private LdapEntryManager ldapEntryManager;	
	@Inject
	private OrganizationService organizationService;

	// @Inject
	// private Logger log;

	/**
	 * Add new scope entry
	 * 
	 * @param scope
	 *            scope
	 */
	public void addScope(OxAuthScope scope) throws Exception {
		ldapEntryManager.persist(scope);

	}

	/**
	 * Remove scope entry
	 * 
	 * @param scope
	 *            scope
	 */
	public void removeScope(OxAuthScope scope) throws Exception {

		ldapEntryManager.remove(scope);

	}

	/**
	 * Get scope by inum
	 * 
	 * @param inum
	 *            scope Inum
	 * @return scope
	 */
	public OxAuthScope getScopeByInum(String inum) throws Exception {

		OxAuthScope result = ldapEntryManager.find(OxAuthScope.class, getDnForScope(inum));

		return result;

	}

	/**
	 * Build DN string for scope
	 * 
	 * @param inum
	 *            scope Inum
	 * @return DN string for specified scope or DN for scopes branch if inum is
	 *         null
	 * @throws Exception
	 */
	public String getDnForScope(String inum) throws Exception {
		String orgDn = organizationService.getDnForOrganization();
		if (StringHelper.isEmpty(inum)) {
			return String.format("ou=scopes,%s", orgDn);
		}

		return String.format("inum=%s,ou=scopes,%s", inum, orgDn);
	}

	/**
	 * Update scope entry
	 * 
	 * @param scope
	 *            scope
	 */
	public void updateScope(OxAuthScope scope) throws Exception {
		ldapEntryManager.merge(scope);

	}

	/**
	 * Generate new inum for scope
	 * 
	 * @return New inum for scope
	 */
	public String generateInumForNewScope() throws Exception {
		OxAuthScope scope = new OxAuthScope();
		String newInum = null;
		do {
			newInum = generateInumForNewScopeImpl();
			String newDn = getDnForScope(newInum);
			scope.setDn(newDn);
		} while (ldapEntryManager.contains(scope));

		return newInum;
	}

	/**
	 * Search scopes by pattern
	 * 
	 * @param pattern
	 *            Pattern
	 * @param sizeLimit
	 *            Maximum count of results
	 * @return List of scopes
	 * @throws Exception
	 */
	public List<OxAuthScope> searchScopes(String pattern, int sizeLimit) throws Exception {
		Filter searchFilter = null;
		if (StringHelper.isNotEmpty(pattern)) {
			String[] targetArray = new String[] { pattern };
			Filter displayNameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
			Filter descriptionFilter = Filter.createSubstringFilter(OxTrustConstants.description, null, targetArray, null);
			Filter inameFilter = Filter.createSubstringFilter(OxTrustConstants.iname, null, targetArray, null);
			searchFilter = Filter.createORFilter(displayNameFilter, descriptionFilter, inameFilter);
		}
		List<OxAuthScope> result = ldapEntryManager.findEntries(getDnForScope(null), OxAuthScope.class, searchFilter, sizeLimit);

		return result;
	}

	/**
	 * Generate new inum for Scope
	 * 
	 * @return New inum for Scope
	 * @throws Exception
	 */
	private String generateInumForNewScopeImpl() throws Exception {
		String orgInum = organizationService.getInumForOrganization();
		return orgInum + OxTrustConstants.inumDelimiter + "0009" + OxTrustConstants.inumDelimiter + INumGenerator.generate(2);

	}

	/**
	 * returns oxAuthScope by Dn
	 * 
	 * @return oxAuthScope
	 */

	public OxAuthScope getScopeByDn(String Dn) throws Exception {
		OxAuthScope result = ldapEntryManager.find(OxAuthScope.class, Dn);

		return result;
	}

	/**
	 * Get all available scope types
	 * 
	 * @return Array of scope types
	 */
	public List<ScopeType> getScopeTypes() {		
		List<ScopeType> scopeTypes= new ArrayList<ScopeType>(Arrays.asList(org.xdi.oxauth.model.common.ScopeType.values()));
		return scopeTypes;
	}

	/**
	 * Get scope by DisplayName
	 * 
	 * @param DisplayName
	 * @return scope
	 */
	public OxAuthScope getScopeByDisplayName(String DisplayName) throws Exception {
		OxAuthScope scope = new OxAuthScope();
		scope.setBaseDn(getDnForScope(null));
		scope.setDisplayName(DisplayName);

		List<OxAuthScope> scopes = ldapEntryManager.findEntries(scope);

		if ((scopes != null) && (scopes.size() > 0)) {
			return scopes.get(0);
		}

		return null;
	}

}
