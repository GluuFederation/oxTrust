/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.model.DisplayNameEntry;
import org.gluu.model.GluuAttribute;
import org.gluu.model.SelectableEntity;
import org.gluu.model.custom.script.CustomScriptType;
import org.gluu.model.custom.script.model.CustomScript;
import org.gluu.oxauth.model.common.ScopeType;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.AttributeService;
import org.gluu.oxtrust.service.OxTrustAuditService;
import org.gluu.oxtrust.service.ScopeService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.annotation.ObjectClass;
import org.gluu.persist.exception.BasePersistenceException;
import org.gluu.service.LookupService;
import org.gluu.service.custom.CustomScriptService;
import org.gluu.service.security.Secure;
import org.gluu.util.StringHelper;
import org.gluu.util.Util;
import org.oxauth.persistence.model.Scope;
import org.oxauth.persistence.model.ScopeAttributes;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Action class for viewing and updating scopes.
 * 
 * @author Reda Zerrad Date: 06.18.2012
 */
@ConversationScoped
@Named("updateScopeAction")
@Secure("#{permissionService.hasPermission('scope', 'access')}")
public class UpdateScopeAction implements Serializable {

	private static final long serialVersionUID = 8198574569820157032L;

	private String[] CUSTOM_SCRIPT_RETURN_ATTRIBUTES = { "inum", "displayName", "description",
			"gluuStatus" };

	@Inject
	private Logger log;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	private String inum;

	private boolean update;

	private Scope scope;

	private List<DisplayNameEntry> claims = new ArrayList<>();
	private String searchAvailableClaimPattern;
	private String oldSearchAvailableClaimPattern;
	private List<GluuAttribute> availableClaims = new ArrayList<>();
	@Inject
	private ScopeService scopeService;
	@Inject
	private LookupService lookupService;
	@Inject
	private AttributeService attributeService;
	@Inject
	private CustomScriptService customScriptService;
	@Inject
	private Identity identity;
	@Inject
	private OxTrustAuditService oxTrustAuditService;
	private List<CustomScript> dynamicScripts;
	private List<SelectableEntity<CustomScript>> availableDynamicScripts = new ArrayList<>();

	private String oxAttributesJson;

	public String add() throws Exception {
		if (this.scope != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		this.update = false;
		this.scope = new Scope();
		this.scope.setScopeType(ScopeType.OAUTH);
		try {
			if (this.scope.getOxAuthClaims() != null && this.scope.getOxAuthClaims().size() > 0) {
				this.claims = getClaimDisplayNameEntiries();
			} else {
				this.claims = new ArrayList<DisplayNameEntry>();
			}
		} catch (BasePersistenceException ex) {
			log.error("Failed to load scopes", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to add new scope");
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}
		this.dynamicScripts = getInitialDynamicScripts();
		fillAvailableDynScript();
		this.oxAttributesJson = getScopeAttributesJson();
		return OxTrustConstants.RESULT_SUCCESS;
	}

	private void fillAvailableDynScript() {
		List<CustomScript> availableScripts = customScriptService
				.findCustomScripts(Arrays.asList(CustomScriptType.DYNAMIC_SCOPE));
		List<SelectableEntity<CustomScript>> tmpAvailableDynamicScripts = new ArrayList<SelectableEntity<CustomScript>>();
		for (CustomScript dynamicScript : availableScripts) {
			if (dynamicScript.isEnabled()) {
				tmpAvailableDynamicScripts.add(new SelectableEntity<CustomScript>(dynamicScript));
			}
		}
		availableDynamicScripts.addAll(tmpAvailableDynamicScripts);
	}

	public String update() throws Exception {
		if (this.scope != null) {
			if (this.scope.getDisplayName() == null) {
				this.scope.setDisplayName(this.scope.getId());
			}
			return OxTrustConstants.RESULT_SUCCESS;
		}
		this.update = true;
		try {
			this.scope = scopeService.getScopeByInum(this.inum);
			if (this.scope == null) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to load scope");
				conversationService.endConversation();
				return OxTrustConstants.RESULT_FAILURE;
			}
			if (this.scope.getDisplayName() == null) {
				this.scope.setDisplayName(this.scope.getId());
			}
		} catch (BasePersistenceException ex) {
			log.error("Failed to find scope {}", inum, ex);
		}
		if (this.scope == null) {
			log.error("Failed to load scope {}", inum);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to find scope");
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}
		try {
			if (this.scope.getOxAuthClaims() != null && this.scope.getOxAuthClaims().size() > 0) {
				this.claims = getClaimDisplayNameEntiries();
			} else {
				this.claims = new ArrayList<DisplayNameEntry>();
			}
			this.dynamicScripts = getInitialDynamicScripts();

		} catch (BasePersistenceException ex) {
			log.error("Failed to load claims", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to load scope");
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}
		fillAvailableDynScript();
		this.oxAttributesJson = getScopeAttributesJson();
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String cancel() {
		if (update) {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "Scope '#{updateScopeAction.scope.displayName}' not updated");
		} else {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "New scope not added");
		}
		conversationService.endConversation();
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String save() throws Exception {
		try {
			this.scope.setDisplayName(this.scope.getDisplayName().trim());
			this.scope.setId(this.scope.getDisplayName());
			updateDynamicScripts();
			updateClaims();
			saveAttributesJson();
			if (update) {
				try {
					scopeService.updateScope(this.scope);
					oxTrustAuditService.audit(
							"OPENID SCOPE " + this.scope.getInum() + " **" + this.scope.getDisplayName() + "** UPDATED",
							identity.getUser(),
							(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
				} catch (BasePersistenceException ex) {
					log.error("Failed to update scope {}", this.inum, ex);
					facesMessages.add(FacesMessage.SEVERITY_ERROR,
							"Failed to update scope '#{updateScopeAction.scope.displayName}'");
					return OxTrustConstants.RESULT_FAILURE;
				}
				facesMessages.add(FacesMessage.SEVERITY_INFO,
						"Scope '#{updateScopeAction.scope.displayName}' updated successfully");
			} else {
				this.inum = scopeService.generateInumForNewScope();
				String dn = scopeService.getDnForScope(this.inum);
				this.scope.setDn(dn);
				this.scope.setInum(this.inum);
				try {
					scopeService.addScope(this.scope);
					oxTrustAuditService.audit(
							"OPENID SCOPE " + this.scope.getInum() + " **" + this.scope.getDisplayName() + "** ADDED",
							identity.getUser(),
							(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
				} catch (Exception ex) {
					log.error("Failed to add new scope {}", this.scope.getInum(), ex);
					facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to add new scope");
					return OxTrustConstants.RESULT_FAILURE;
				}
				facesMessages.add(FacesMessage.SEVERITY_INFO,
						"New scope '#{updateScopeAction.scope.displayName}' added successfully");
				conversationService.endConversation();
				this.update = true;
			}
			log.debug(" returning success updating or saving scope");
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			log.info("", e);
			return OxTrustConstants.RESULT_FAILURE;
		}

	}

	private void updateClaims() {
		if ((org.gluu.oxauth.model.common.ScopeType.DYNAMIC == this.scope.getScopeType()) || (this.claims == null)
				|| (this.claims.size() == 0)) {
			this.scope.setOxAuthClaims(null);
			return;
		}
		List<String> resultClaims = new ArrayList<String>();
		this.scope.setOxAuthClaims(resultClaims);
		for (DisplayNameEntry claim : this.claims) {
			resultClaims.add(claim.getDn());
		}
		this.scope.setOxAuthClaims(resultClaims);
	}

	public String delete() throws Exception {
		if (update) {
			try {
				scopeService.removeScope(this.scope);
				oxTrustAuditService.audit(
						"OPENID SCOPE " + this.scope.getInum() + " **" + this.scope.getDisplayName() + "** REMOVED",
						identity.getUser(),
						(HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
				facesMessages.add(FacesMessage.SEVERITY_INFO,
						"Scope '#{updateScopeAction.scope.displayName}' removed successfully");
				conversationService.endConversation();
				return OxTrustConstants.RESULT_SUCCESS;
			} catch (BasePersistenceException ex) {
				log.error("Failed to remove scope {}", this.scope.getInum(), ex);

			}
		}
		return OxTrustConstants.RESULT_FAILURE;
	}

	public void addClaim(GluuAttribute claim) {
		DisplayNameEntry oneClaim = new DisplayNameEntry(claim.getDn(), claim.getInum(), claim.getDisplayName());
		this.claims.add(oneClaim);
		this.searchAvailableClaimPattern = "";
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
			this.availableClaims = attributeService.searchAttributes(this.searchAvailableClaimPattern,
					OxTrustConstants.searchSizeLimit);
			removeDuplicates();
			this.oldSearchAvailableClaimPattern = this.searchAvailableClaimPattern;
			selectAddedClaims();
		} catch (Exception ex) {
			log.error("Failed to find attributes", ex);
		}
	}

	public void clearAvailableClaims() {
		this.availableClaims = new ArrayList<>();
	}

	public void removeDuplicates() {
		List<GluuAttribute> tempAvailableClaims = new ArrayList<GluuAttribute>();
		for (int i = 0; i < this.availableClaims.size(); i++) {
			for (int j = i + 1; j < this.availableClaims.size();) {
				if (this.availableClaims.get(i).getDisplayName()
						.equalsIgnoreCase(this.availableClaims.get(j).getDisplayName())) {
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
		List<AttributeDisplayNameEntry> tmp = lookupService.getDisplayNameEntries(attributeService.getDnForAttribute(null),
				AttributeDisplayNameEntry.class, this.scope.getOxAuthClaims());
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

	public Scope getScope() {
		return this.scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public List<DisplayNameEntry> getClaims() {
		return this.claims;
	}

	public void setClaims(List<DisplayNameEntry> claims) {
		this.claims = claims;
	}

	private List<CustomScript> getInitialDynamicScripts() {
		List<CustomScript> result = new ArrayList<CustomScript>();
		if ((this.scope.getDynamicScopeScripts() == null) || (this.scope.getDynamicScopeScripts().size() == 0)) {
			return result;
		}
		List<AttributeDisplayNameEntry> displayNameEntries = lookupService.getDisplayNameEntries(customScriptService.baseDn(),
				AttributeDisplayNameEntry.class, this.scope.getDynamicScopeScripts());
		if (displayNameEntries != null) {
			for (DisplayNameEntry displayNameEntry : displayNameEntries) {
				result.add(new CustomScript(displayNameEntry.getDn(), displayNameEntry.getInum(),
						displayNameEntry.getDisplayName()));
			}
		}
		return result;
	}

	private void updateDynamicScripts() {
		if ((this.dynamicScripts == null) || (this.dynamicScripts.size() == 0)) {
			this.scope.setDynamicScopeScripts(null);
			return;
		}
		List<String> resultDynamicScripts = new ArrayList<String>();
		for (CustomScript dynamicScript : this.dynamicScripts) {
			resultDynamicScripts.add(dynamicScript.getDn());
		}
		this.scope.setDynamicScopeScripts(resultDynamicScripts);
	}

	public void acceptSelectDynamicScripts() {
		if (this.availableDynamicScripts == null) {
			return;
		}

		Set<String> addedDynamicScriptInums = getAddedDynamicScriptInums();

		for (SelectableEntity<CustomScript> availableDynamicScript : this.availableDynamicScripts) {
			CustomScript dynamicScript = availableDynamicScript.getEntity();
			if (availableDynamicScript.isSelected() && !addedDynamicScriptInums.contains(dynamicScript.getInum())) {
				addDynamicScript(dynamicScript);
			}

			if (!availableDynamicScript.isSelected() && addedDynamicScriptInums.contains(dynamicScript.getInum())) {
				removeDynamicScript(dynamicScript);
			}
		}
	}

	public void cancelSelectDynamicScripts() {
	}

	public void addDynamicScript(CustomScript addDynamicScript) {
		if (addDynamicScript == null) {
			return;
		}
		this.dynamicScripts.add(addDynamicScript);
	}

	public void removeDynamicScript(CustomScript removeDynamicScript) {
		if (removeDynamicScript == null) {
			return;
		}
		for (Iterator<CustomScript> it = this.dynamicScripts.iterator(); it.hasNext();) {
			CustomScript dynamicScript = (CustomScript) it.next();
			if (StringHelper.equalsIgnoreCase(removeDynamicScript.getInum(), dynamicScript.getInum())) {
				it.remove();
				break;
			}
		}
	}

	public void searchAvailableDynamicScripts() {
		if (this.availableDynamicScripts != null) {
			selectAddedDynamicScripts();
			return;
		}
		try {
			List<CustomScript> availableScripts = customScriptService
					.findCustomScripts(Arrays.asList(CustomScriptType.DYNAMIC_SCOPE), CUSTOM_SCRIPT_RETURN_ATTRIBUTES);
			List<SelectableEntity<CustomScript>> tmpAvailableDynamicScripts = new ArrayList<SelectableEntity<CustomScript>>();
			for (CustomScript dynamicScript : availableScripts) {
				if (dynamicScript.isEnabled()) {
					tmpAvailableDynamicScripts.add(new SelectableEntity<CustomScript>(dynamicScript));
				}
			}
			this.availableDynamicScripts = tmpAvailableDynamicScripts;
			selectAddedDynamicScripts();
		} catch (BasePersistenceException ex) {
			log.error("Failed to find available authorization policies", ex);
		}

	}

	private void selectAddedDynamicScripts() {
		Set<String> addedDynamicScriptInums = getAddedDynamicScriptInums();
		for (SelectableEntity<CustomScript> availableDynamicScript : this.availableDynamicScripts) {
			availableDynamicScript
					.setSelected(addedDynamicScriptInums.contains(availableDynamicScript.getEntity().getInum()));
		}
	}

	private Set<String> getAddedDynamicScriptInums() {
		Set<String> addedDynamicScriptInums = new HashSet<String>();
		for (CustomScript dynamicScript : this.dynamicScripts) {
			addedDynamicScriptInums.add(dynamicScript.getInum());
		}
		return addedDynamicScriptInums;
	}

	public List<SelectableEntity<CustomScript>> getAvailableDynamicScripts() {
		return this.availableDynamicScripts;
	}

	public List<CustomScript> getDynamicScripts() {
		return dynamicScripts;
	}

	public boolean isUpdate() {
		return update;
	}

	public List<ScopeType> getScopeTypes() {
		return scopeService.getScopeTypes();
	}

	public String getOxAttributesJson() {
		return oxAttributesJson;
	}

	public void setOxAttributesJson(String oxAttributesJson) {
		this.oxAttributesJson = oxAttributesJson;
	}

	private void saveAttributesJson() {
		ScopeAttributes scopeAttributes = new ScopeAttributes();
		try {
			scopeAttributes = new ObjectMapper().readValue(this.oxAttributesJson, ScopeAttributes.class);
		} catch (Exception e) {
			log.info("error parsing json:" + e);
		}

		this.scope.setAttributes(scopeAttributes);
	}

	private String getScopeAttributesJson() {
		if (scope != null) {
			try {
				return new ObjectMapper().writeValueAsString(this.scope.getAttributes());
			} catch (Exception e) {
				return "{}";
			}
		} else {
			return "{}";
		}
	}

	@ObjectClass(value = "gluuAttribute")
	class AttributeDisplayNameEntry extends DisplayNameEntry {
		public AttributeDisplayNameEntry() {
			super();
		}
	}

}
