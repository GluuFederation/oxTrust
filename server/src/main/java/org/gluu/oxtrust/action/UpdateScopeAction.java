/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.gluu.oxtrust.ldap.service.AttributeService;
import org.gluu.oxtrust.ldap.service.ScopeService;
import org.gluu.oxtrust.model.OxAuthScope;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.xdi.model.DisplayNameEntry;
import org.xdi.model.GluuAttribute;
import org.xdi.service.LookupService;
import org.xdi.util.StringHelper;
import org.xdi.util.Util;

/**
 * Action class for viewing and updating scopes.
 * 
 * @author Reda Zerrad Date: 06.18.2012
 */
@Scope(ScopeType.CONVERSATION)
@Name("updateScopeAction")
@Restrict("#{identity.loggedIn}")
public class UpdateScopeAction implements Serializable {

	/**
     *
     */
	private static final long serialVersionUID = 8198574569820157032L;

	@Logger
	private Log log;

	private String inum;

	private boolean update;

	private OxAuthScope scope;

	private List<DisplayNameEntry> claims;

	private String searchAvailableClaimPattern;

	private String oldSearchAvailableClaimPattern;

	private List<GluuAttribute> availableClaims;

	@In
	private ScopeService scopeService;

	@In
	private LookupService lookupService;

	@In
	private transient AttributeService attributeService;

	@In
	private FacesMessages facesMessages;

	@Restrict("#{s:hasPermission('scope', 'access')}")
	public String add() throws Exception {
		if (this.scope != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.update = false;
		this.scope = new OxAuthScope();

		try {
			if (this.scope.getOxAuthClaims() != null && this.scope.getOxAuthClaims().size() > 0) {
				this.claims = getClaimDisplayNameEntiries();
			} else {
				this.claims = new ArrayList<DisplayNameEntry>();
			}

		} catch (LdapMappingException ex) {
			log.error("Failed to load scopes", ex);

			return OxTrustConstants.RESULT_FAILURE;
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	@Restrict("#{s:hasPermission('scope', 'access')}")
	public String update() throws Exception {
		if (this.scope != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.update = true;
		log.info("this.update : " + this.update);
		try {

			log.info("inum : " + this.inum);
			this.scope = scopeService.getScopeByInum(this.inum);
		} catch (LdapMappingException ex) {
			log.error("Failed to find scope {0}", ex, inum);

		}

		if (this.scope == null) {
			log.info("Group is null ");
			return OxTrustConstants.RESULT_FAILURE;
		}

		try {
			if (this.scope.getOxAuthClaims() != null && this.scope.getOxAuthClaims().size() > 0) {
				this.claims = getClaimDisplayNameEntiries();
			} else {
				this.claims = new ArrayList<DisplayNameEntry>();
			}

		} catch (LdapMappingException ex) {
			log.error("Failed to load claims", ex);

			return OxTrustConstants.RESULT_FAILURE;
		}
		log.info("returning Success");
		return OxTrustConstants.RESULT_SUCCESS;
	}

	@Restrict("#{s:hasPermission('scope', 'access')}")
	public void cancel() {
	}

	@Restrict("#{s:hasPermission('scope', 'access')}")
	public String save() throws Exception {
		// List<DisplayNameEntry> oldClaims = null;
		// try {
		// oldClaims = getClaimDisplayNameEntiries();

		// } catch (LdapMappingException ex) {
		// log.error("error getting oldClaims",ex);
		//
		// facesMessages.add(Severity.ERROR, "Failed to update scope");
		// return Configuration.RESULT_FAILURE;
		// }

		updateClaims();
		if (update) {
			// Update scope
			try {
				scopeService.updateScope(this.scope);
			} catch (LdapMappingException ex) {

				log.info("error updating scope ", ex);
				log.error("Failed to update scope {0}", ex, this.inum);

				facesMessages.add(Severity.ERROR, "Failed to update scope");
				return OxTrustConstants.RESULT_FAILURE;
			}
		} else {
			this.inum = scopeService.generateInumForNewScope();
			String dn = scopeService.getDnForScope(this.inum);

			// Save scope
			this.scope.setDn(dn);
			this.scope.setInum(this.inum);
			try {
				scopeService.addScope(this.scope);
			} catch (Exception ex) {
				log.info("error saving scope ");
				log.error("Failed to add new scope {0}", ex, this.scope.getInum());

				facesMessages.add(Severity.ERROR, "Failed to add new scope");
				return OxTrustConstants.RESULT_FAILURE;

			}
			this.update = true;
		}
		log.info(" returning success updating or saving scope");
		return OxTrustConstants.RESULT_SUCCESS;
	}

	private void updateClaims() {

		if (this.claims == null || this.claims.size() < 1) {
			this.scope.setOxAuthClaims(null);
			return;
		}
		List<String> TMPclaims = new ArrayList<String>();
		this.scope.setOxAuthClaims(TMPclaims);

		for (DisplayNameEntry claim : this.claims) {
			TMPclaims.add(claim.getDn());
		}

		this.scope.setOxAuthClaims(TMPclaims);
	}

	@Restrict("#{s:hasPermission('scope', 'access')}")
	public String delete() throws Exception {
		if (update) {
			// Remove scope
			try {
				scopeService.removeScope(this.scope);
				return OxTrustConstants.RESULT_SUCCESS;
			} catch (LdapMappingException ex) {
				log.error("Failed to remove scope {0}", ex, this.scope.getInum());

			}
		}

		return OxTrustConstants.RESULT_FAILURE;
	}

	public void addClaim(GluuAttribute claim) {
		DisplayNameEntry oneClaim = new DisplayNameEntry(claim.getDn(), claim.getInum(), claim.getDisplayName());
		this.claims.add(oneClaim);
	}

	public void removeClaim(String inum) throws Exception {
		if (StringHelper.isEmpty(inum)) {
			return;
		}

		String removeClaimInum = attributeService.getDnForAttribute(inum);

		for (Iterator<DisplayNameEntry> iterator = this.claims.iterator(); iterator.hasNext();) {
			DisplayNameEntry oneClaim = iterator.next();
			if (removeClaimInum.equals(oneClaim.getDn())) {
				iterator.remove();
				break;
			}
		}

	}

	public String getSearchAvailableClaimPattern() {
		return this.searchAvailableClaimPattern;
	}

	public void setSearchAvailableClaimPattern(String searchAvailableClaimPattern) {
		this.searchAvailableClaimPattern = searchAvailableClaimPattern;
	}

	public List<GluuAttribute> getAvailableClaims() {
		return this.availableClaims;
	}

	public void searchAvailableClaims() {
		if (Util.equals(this.oldSearchAvailableClaimPattern, this.searchAvailableClaimPattern)) {
			return;
		}

		try {
			this.availableClaims = attributeService
					.searchAttributes(this.searchAvailableClaimPattern, OxTrustConstants.searchPersonsSizeLimit);
			//
			removeDuplicates();
			this.oldSearchAvailableClaimPattern = this.searchAvailableClaimPattern;
			selectAddedClaims();
		} catch (Exception ex) {
			log.error("Failed to find attributes", ex);
		}
	}

	public void removeDuplicates() {
		List<GluuAttribute> tempAvailableClaims = new ArrayList<GluuAttribute>();
		for (int i = 0; i < this.availableClaims.size(); i++) {
			for (int j = i + 1; j < this.availableClaims.size();) {
				if (this.availableClaims.get(i).getDisplayName().equalsIgnoreCase(this.availableClaims.get(j).getDisplayName())) {
					this.availableClaims.remove(j);
				} else {
					j++;
				}
			}
		}

		for (GluuAttribute availableClaim : this.availableClaims) {
			if (availableClaim != null) {
				tempAvailableClaims.add(availableClaim);
			}
		}

		this.availableClaims = tempAvailableClaims;

	}

	public void selectAddedClaims() {
		if (this.availableClaims == null) {
			return;
		}

		Set<String> addedClaimInums = new HashSet<String>();

		for (DisplayNameEntry claim : claims) {
			addedClaimInums.add(claim.getInum());
		}

		for (GluuAttribute attribute : this.availableClaims) {
			if (attribute.isSelected() && !addedClaimInums.contains(attribute.getInum())) {
				addClaim(attribute);
			}
		}

	}

	public void acceptSelectClaims() {
		if (this.availableClaims == null) {
			return;
		}

		Set<String> addedClaimsInums = new HashSet<String>();

		for (DisplayNameEntry claim : claims) {
			addedClaimsInums.add(claim.getInum());
		}

		for (GluuAttribute attribute : this.availableClaims) {
			if (attribute.isSelected() && !addedClaimsInums.contains(attribute.getInum())) {
				addClaim(attribute);
			}
		}

	}

	public void cancelSelectClaims() {
	}

	private List<DisplayNameEntry> getClaimDisplayNameEntiries() throws Exception {
		List<DisplayNameEntry> result = new ArrayList<DisplayNameEntry>();
		List<DisplayNameEntry> tmp = lookupService.getDisplayNameEntries(attributeService.getDnForAttribute(null),
				this.scope.getOxAuthClaims());
		if (tmp != null) {
			result.addAll(tmp);
		}

		return result;
	}

	public String getInum() {
		return inum;
	}

	public void setInum(String inum) {
		this.inum = inum;
	}

	public OxAuthScope getScope() {
		return this.scope;
	}

	public void setScope(OxAuthScope scope) {
		this.scope = scope;
	}

	public List<DisplayNameEntry> getClaims() {
		return this.claims;
	}

	public void setClaims(List<DisplayNameEntry> claims) {
		this.claims = claims;
	}

	public boolean isUpdate() {
		return update;
	}
}
