/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action.uma;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.model.DisplayNameEntry;
import org.gluu.model.GluuImage;
import org.gluu.model.SelectableEntity;
import org.gluu.model.custom.script.CustomScriptType;
import org.gluu.model.custom.script.model.CustomScript;
import org.gluu.oxauth.model.common.ScopeType;
import org.gluu.oxauth.model.uma.persistence.UmaResource;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.ClientService;
import org.gluu.oxtrust.service.ImageService;
import org.gluu.oxtrust.service.uma.ResourceSetService;
import org.gluu.oxtrust.service.uma.UmaScopeService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.annotation.ObjectClass;
import org.gluu.persist.exception.BasePersistenceException;
import org.gluu.service.LookupService;
import org.gluu.service.custom.CustomScriptService;
import org.gluu.service.security.Secure;
import org.gluu.util.StringHelper;
import org.oxauth.persistence.model.Scope;
import org.oxauth.persistence.model.ScopeAttributes;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Action class for view and update UMA resource
 * 
 * @author Yuriy Movchan Date: 11/21/2012
 */
@Named("updateUmaScopeAction")
@ConversationScoped
@Secure("#{permissionService.hasPermission('uma', 'access')}")
public class UpdateUmaScopeAction implements Serializable {

	private static final long serialVersionUID = 6180729281938167478L;

	private static final String[] CUSTOM_SCRIPT_RETURN_ATTRIBUTES = { "inum", "displayName", "description" };

	@Inject
	private Logger log;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	@Inject
	private Identity identity;

	@Inject
	protected UmaScopeService scopeDescriptionService;

	@Inject
	private ImageService imageService;

	@Inject
	private LookupService lookupService;

	@Inject
	private CustomScriptService customScriptService;

	@Inject
	private ResourceSetService resourceSetService;

	@Inject
	private ClientService clientService;

	private String scopeInum;

	private Scope umaScope;

	private GluuImage curIconImage;

	private List<CustomScript> authorizationPolicies;
	private List<SelectableEntity<CustomScript>> availableAuthorizationPolicies;

	private boolean update;

	private String oxAttributesJson;

	private List<OxAuthClient> clientList;

	public List<OxAuthClient> getClientList() {
		return clientList;
	}

	public void setClientList(List<OxAuthClient> clientList) {
		this.clientList = clientList;
	}

	public String add() {
		try {
			if (this.umaScope != null) {
				this.oxAttributesJson = getScopeAttributesJson();
				return OxTrustConstants.RESULT_SUCCESS;
			}
			this.umaScope = new Scope();
			this.update = false;
			this.oxAttributesJson = getScopeAttributesJson();
			this.authorizationPolicies = getInitialAuthorizationPolicies();
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception e) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to load scope add from");
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}

	}

	public String update() {
		this.update = true;
		if (this.umaScope != null) {
			this.oxAttributesJson = getScopeAttributesJson();
			return OxTrustConstants.RESULT_SUCCESS;
		}
		try {
			String scopeDn = scopeDescriptionService.getDnForScope(this.scopeInum);
			this.umaScope = scopeDescriptionService.getUmaScopeByDn(scopeDn);
			this.oxAttributesJson = getScopeAttributesJson();
			this.authorizationPolicies = getInitialAuthorizationPolicies();
			List<UmaResource> umaResourceList = resourceSetService.findResourcesByScope(scopeDn);
			if (umaResourceList != null) {
				for (UmaResource umaResource : umaResourceList) {
					List<String> list = umaResource.getClients();
					if (list != null) {
						clientList = new ArrayList<OxAuthClient>();
						for (String clientDn : list) {
							OxAuthClient oxAuthClient = clientService.getClientByDn(clientDn);
							if (oxAuthClient != null) {
								clientList.add(oxAuthClient);
							}

						}
					}
				}
			}
		} catch (BasePersistenceException ex) {
			log.error("Failed to find scope description '{}'", this.scopeInum, ex);
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}
		if (this.umaScope == null) {
			log.error("Scope description is null");
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String cancel() {
		if (update) {
			facesMessages.add(FacesMessage.SEVERITY_INFO,
					"UMA resource '#{updateScopeDescriptionAction.scopeDescription.displayName}' not updated");
		} else {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "New UMA resource not added");
		}
		conversationService.endConversation();
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String save() throws Exception {
		this.umaScope.setDisplayName(this.umaScope.getDisplayName().trim());
		this.umaScope.setScopeType(ScopeType.UMA);
		updateAuthorizationPolicies();
		saveAttributesJson();
		if (this.update) {
			if (scopeWithSameNameExistInUpdate()) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "A scope with same name already exist");
				return OxTrustConstants.RESULT_FAILURE;
			}
			try {
				scopeDescriptionService.updateUmaScope(this.umaScope);
			} catch (BasePersistenceException ex) {
				log.error("Failed to update scope description '{}'", this.umaScope.getId(), ex);
				facesMessages.add(FacesMessage.SEVERITY_ERROR,
						"Failed to update UMA resource '#{updateScopeDescriptionAction.scopeDescription.displayName}'");
				return OxTrustConstants.RESULT_FAILURE;
			}
			log.debug("Scope description were updated successfully");
			facesMessages.add(FacesMessage.SEVERITY_INFO,
					"UMA resource '#{updateScopeDescriptionAction.scopeDescription.displayName}' updated successfully");
			return OxTrustConstants.RESULT_UPDATE;
		} else {
			if (scopeWithSameNameExist()) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "A scope with same name already exist");
				return OxTrustConstants.RESULT_FAILURE;
			}
			Scope exampleScopeDescription = new Scope();
			exampleScopeDescription.setDn(scopeDescriptionService.getDnForScope(null));
			exampleScopeDescription.setId(umaScope.getId());
			String inum = scopeDescriptionService.generateInumForNewScope();
			String scopeDescriptionDn = scopeDescriptionService.getDnForScope(inum);
			this.umaScope.setInum(inum);
			this.umaScope.setDn(scopeDescriptionDn);
			this.umaScope.setId(umaScope.getId());
			try {
				scopeDescriptionService.addUmaScope(this.umaScope);
			} catch (BasePersistenceException ex) {
				log.error("Failed to add new UMA resource '{}'", this.umaScope.getId(), ex);
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to add new UMA resource");
				return OxTrustConstants.RESULT_FAILURE;
			}
			log.debug("Scope description were add successfully");
			facesMessages.add(FacesMessage.SEVERITY_INFO,
					"New UMA resource '#{updateScopeDescriptionAction.scopeDescription.displayName}' added successfully");
			conversationService.endConversation();
			this.update = true;
			this.scopeInum = inum;
			return OxTrustConstants.RESULT_SUCCESS;
		}
	}

	private boolean scopeWithSameNameExist() {
		return scopeDescriptionService.getAllUmaScopes(1000).stream()
				.anyMatch(e -> e.getDisplayName().equalsIgnoreCase(this.umaScope.getDisplayName()));
	}

	private boolean scopeWithSameNameExistInUpdate() {
		return scopeDescriptionService.getAllUmaScopes(1000).stream()
				.filter(e -> !e.getInum().equalsIgnoreCase(this.umaScope.getInum()))
				.anyMatch(e -> e.getDisplayName().equalsIgnoreCase(this.umaScope.getDisplayName()));
	}

	public String delete() {
		try {
			scopeDescriptionService.removeUmaScope(this.umaScope);
			facesMessages.add(FacesMessage.SEVERITY_INFO,
					"UMA resource '#{updateScopeDescriptionAction.scopeDescription.displayName}' removed successfully");
			conversationService.endConversation();
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (BasePersistenceException ex) {
			log.error("Failed to remove scope description {}", this.umaScope.getId(), ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR,
					"Failed to remove UMA resource '#{updateScopeDescriptionAction.scopeDescription.displayName}'");
			return OxTrustConstants.RESULT_FAILURE;
		}
	}

	public void removeIconImage() {
		this.curIconImage = null;
	}

	public void setIconImage(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getUploadedFile();
		try {
			setIconImageImpl(uploadedFile);
		} finally {
			try {
				uploadedFile.delete();
			} catch (IOException ex) {
				log.error("Failed to remove temporary image", ex);
			}
		}
	}

	private void setIconImageImpl(UploadedFile uploadedFile) {
		removeIconImage();

		GluuImage newIcon = imageService.constructImageWithThumbnail(identity.getUser(), uploadedFile, 16, 16);
		this.curIconImage = newIcon;
		try {
		} catch (Exception ex) {
			log.error("Failed to store icon image: '{}'", newIcon, ex);
		}
	}

	public byte[] getIconImageThumbData() {
		if ((this.curIconImage != null) && (this.curIconImage.getThumbData() != null)) {
			return this.curIconImage.getThumbData();
		}

		return imageService.getBlankImageData();
	}

	public String getIconImageSourceName() {
		if (this.curIconImage != null) {
			return this.curIconImage.getSourceName();
		}

		return null;
	}

	public boolean isIconExist() {
		return this.curIconImage != null;
	}

	private List<CustomScript> getInitialAuthorizationPolicies() {
		List<CustomScript> result = new ArrayList<CustomScript>();
		if ((this.umaScope.getUmaAuthorizationPolicies() == null)
				|| (this.umaScope.getUmaAuthorizationPolicies().size() == 0)) {
			return result;
		}

		List<ScriptDisplayNameEntry> displayNameEntries = lookupService.getDisplayNameEntries(customScriptService.baseDn(),
				ScriptDisplayNameEntry.class, this.umaScope.getUmaAuthorizationPolicies());
		if (displayNameEntries != null) {
			for (DisplayNameEntry displayNameEntry : displayNameEntries) {
				result.add(new CustomScript(displayNameEntry.getDn(), displayNameEntry.getInum(),
						displayNameEntry.getDisplayName()));
			}
		}

		return result;
	}

	private void updateAuthorizationPolicies() {
		if (this.authorizationPolicies == null || this.authorizationPolicies.size() == 0) {
			this.umaScope.setUmaAuthorizationPolicies(null);
			return;
		}

		List<String> tmpAuthorizationPolicies = new ArrayList<String>();
		for (CustomScript authorizationPolicy : this.authorizationPolicies) {
			tmpAuthorizationPolicies.add(authorizationPolicy.getDn());
		}

		this.umaScope.setUmaAuthorizationPolicies(tmpAuthorizationPolicies);
	}

	public void acceptSelectAuthorizationPolicies() {
		if (this.availableAuthorizationPolicies == null) {
			return;
		}

		Set<String> addedAuthorizationPolicyInums = getAddedAuthorizationPolicyInums();

		for (SelectableEntity<CustomScript> availableAuthorizationPolicy : this.availableAuthorizationPolicies) {
			CustomScript authorizationPolicy = availableAuthorizationPolicy.getEntity();
			if (availableAuthorizationPolicy.isSelected()
					&& !addedAuthorizationPolicyInums.contains(authorizationPolicy.getInum())) {
				addAuthorizationPolicy(authorizationPolicy);
			}

			if (!availableAuthorizationPolicy.isSelected()
					&& addedAuthorizationPolicyInums.contains(authorizationPolicy.getInum())) {
				removeAuthorizationPolicy(authorizationPolicy);
			}
		}
	}

	public void cancelSelectAuthorizationPolicies() {
	}

	public void addAuthorizationPolicy(CustomScript addAuthorizationPolicy) {
		if (addAuthorizationPolicy == null) {
			return;
		}

		this.authorizationPolicies.add(addAuthorizationPolicy);
	}

	public void removeAuthorizationPolicy(CustomScript removeAuthorizationPolicy) {
		if (removeAuthorizationPolicy == null) {
			return;
		}

		for (Iterator<CustomScript> it = this.authorizationPolicies.iterator(); it.hasNext();) {
			CustomScript authorizationPolicy = (CustomScript) it.next();

			if (StringHelper.equalsIgnoreCase(removeAuthorizationPolicy.getInum(), authorizationPolicy.getInum())) {
				it.remove();
				break;
			}
		}
	}

	public void searchAvailableAuthorizationPolicies() {
		if (this.availableAuthorizationPolicies != null) {
			selectAddedAuthorizationPolicies();
			return;
		}

		try {
			List<CustomScript> availableScripts = customScriptService
					.findCustomScripts(Arrays.asList(CustomScriptType.UMA_RPT_POLICY), CUSTOM_SCRIPT_RETURN_ATTRIBUTES);

			List<SelectableEntity<CustomScript>> tmpAvailableAuthorizationPolicies = new ArrayList<SelectableEntity<CustomScript>>();
			for (CustomScript authorizationPolicy : availableScripts) {
				tmpAvailableAuthorizationPolicies.add(new SelectableEntity<CustomScript>(authorizationPolicy));
			}

			this.availableAuthorizationPolicies = tmpAvailableAuthorizationPolicies;
			selectAddedAuthorizationPolicies();
		} catch (BasePersistenceException ex) {
			log.error("Failed to find available authorization policies", ex);
		}

	}

	private void selectAddedAuthorizationPolicies() {
		Set<String> addedAuthorizationPolicyInums = getAddedAuthorizationPolicyInums();

		for (SelectableEntity<CustomScript> availableAuthorizationPolicy : this.availableAuthorizationPolicies) {
			availableAuthorizationPolicy.setSelected(
					addedAuthorizationPolicyInums.contains(availableAuthorizationPolicy.getEntity().getInum()));
		}
	}

	private Set<String> getAddedAuthorizationPolicyInums() {
		Set<String> addedAuthorizationPolicyInums = new HashSet<String>();

		for (CustomScript authorizationPolicy : this.authorizationPolicies) {
			addedAuthorizationPolicyInums.add(authorizationPolicy.getInum());
		}

		return addedAuthorizationPolicyInums;
	}

	public boolean isUpdate() {
		return update;
	}

	public String getScopeInum() {
		return scopeInum;
	}

	public void setScopeInum(String scopeInum) {
		this.scopeInum = scopeInum;
	}

	public Scope getUmaScope() {
		return umaScope;
	}

	public void setUmaScope(Scope umaScope) {
		this.umaScope = umaScope;
	}

	public List<SelectableEntity<CustomScript>> getAvailableAuthorizationPolicies() {
		return this.availableAuthorizationPolicies;
	}

	public List<CustomScript> getAuthorizationPolicies() {
		return authorizationPolicies;
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

		this.umaScope.setAttributes(scopeAttributes);
	}

	private String getScopeAttributesJson() {
		if (umaScope != null) {
			try {
				return new ObjectMapper().writeValueAsString(this.umaScope.getAttributes());
			} catch (Exception e) {
				return "{}";
			}
		} else {
			return "{}";
		}
	}

	@ObjectClass(value = "oxCustomScript")
	class ScriptDisplayNameEntry extends DisplayNameEntry {

		public ScriptDisplayNameEntry() {
			super();
		}
	}

}
