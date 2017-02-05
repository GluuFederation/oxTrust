/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.Serializable;
import java.util.List;

import org.gluu.oxtrust.model.OxAuthScope;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.LdapEntryManager;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.xdi.util.INumGenerator;
import org.xdi.util.StringHelper;

import com.unboundid.ldap.sdk.Filter;

/**
 * Provides operations with Scopes
 * 
 * @author Reda Zerrad Date: 06.18.2012
 */
@Scope(ScopeType.STATELESS)
@Name("scopeService")
@AutoCreate
public class ScopeService implements Serializable {

	/**
     *
     */
	private static final long serialVersionUID = 65734145678106186L;

	@In
	private LdapEntryManager ldapEntryManager;

	// @Logger
	// private Log log;

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
		String orgDn = OrganizationService.instance().getDnForOrganization();
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
		if(pattern!=null && ! pattern.isEmpty() ){
			String[] targetArray = new String[] { pattern };
			Filter displayNameFilter = Filter.createSubstringFilter(OxTrustConstants.displayName, null, targetArray, null);
			Filter descriptionFilter = Filter.createSubstringFilter(OxTrustConstants.description, null, targetArray, null);
			Filter inameFilter = Filter.createSubstringFilter(OxTrustConstants.iname, null, targetArray, null);
			searchFilter = Filter.createORFilter(displayNameFilter, descriptionFilter, inameFilter);
		}
		List<OxAuthScope> result = ldapEntryManager.findEntries(getDnForScope(null), OxAuthScope.class, searchFilter, 0, sizeLimit);

		return result;
	}

	/**
	 * Generate new inum for Scope
	 * 
	 * @return New inum for Scope
	 * @throws Exception
	 */
	private String generateInumForNewScopeImpl() throws Exception {
		String orgInum = OrganizationService.instance().getInumForOrganization();
		return orgInum + OxTrustConstants.inumDelimiter + "0009" + OxTrustConstants.inumDelimiter + INumGenerator.generate(2);

	}

	/**
	 * returns a list of all scopes
	 * 
	 * @return list of scopes
	 * @throws Exception
	 */

	public List<OxAuthScope> getAllScopesList() throws Exception {

		List<OxAuthScope> result = ldapEntryManager.findEntries(getDnForScope(null), OxAuthScope.class,
				Filter.createPresenceFilter("inum"), 0, 10);

		return result;
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
	public org.xdi.oxauth.model.common.ScopeType[] getScopeTypes() {
		return org.xdi.oxauth.model.common.ScopeType.values();
	}

	/**
	 * Get ScopeService instance
	 * 
	 * @return ScopeService instance
	 */
	public static ScopeService instance() throws Exception {
		return (ScopeService) Component.getInstance(ScopeService.class);
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
