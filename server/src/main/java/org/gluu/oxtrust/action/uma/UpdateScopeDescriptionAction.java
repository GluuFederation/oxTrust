/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action.uma;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.ldap.service.ImageService;
import org.gluu.oxtrust.ldap.service.uma.ScopeDescriptionService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.custom.CustomScriptService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.exception.mapping.BaseMappingException;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;
import org.slf4j.Logger;
import org.xdi.model.DisplayNameEntry;
import org.xdi.model.GluuImage;
import org.xdi.model.SelectableEntity;
import org.xdi.model.custom.script.CustomScriptType;
import org.xdi.model.custom.script.model.CustomScript;
import org.xdi.oxauth.model.uma.UmaMetadata;
import org.xdi.oxauth.model.uma.persistence.UmaScopeDescription;
import org.xdi.service.JsonService;
import org.xdi.service.LookupService;
import org.xdi.service.security.Secure;
import org.xdi.util.StringHelper;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Action class for view and update UMA resource
 * 
 * @author Yuriy Movchan Date: 11/21/2012
 */
@ConversationScoped
@Named
@Secure("#{permissionService.hasPermission('uma', 'access')}")
public class UpdateScopeDescriptionAction implements Serializable {

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
	protected ScopeDescriptionService scopeDescriptionService;

	@Inject
	private ImageService imageService;
	
	@Inject
	private JsonService jsonService;

	@Inject
	private LookupService lookupService;
	
	@Inject
	private CustomScriptService customScriptService;

    @Inject
   	private UmaMetadata umaMetadataConfiguration;

	private String scopeInum;

	private UmaScopeDescription scopeDescription;

	private GluuImage curIconImage;

	private List<CustomScript> authorizationPolicies;
	private List<SelectableEntity<CustomScript>> availableAuthorizationPolicies;

	private boolean update;

	public String modify() {
		if (this.scopeDescription != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.update = StringHelper.isNotEmpty(this.scopeInum);

		try {
			scopeDescriptionService.prepareScopeDescriptionBranch();
		} catch (BaseMappingException ex) {
			log.error("Failed to initialize form", ex);

			if (update) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to find UMA resource");
			} else {
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to add UMA resource");
			}
			conversationService.endConversation();

			return OxTrustConstants.RESULT_FAILURE;
		}

		if (update) {
			return update();
		} else {
			return add();
		}
	}

	private String add() {
		if (this.scopeDescription != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.scopeDescription = new UmaScopeDescription();

		this.authorizationPolicies = getInitialAuthorizationPolicies();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private String update() {
		if (this.scopeDescription != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		log.debug("Loading UMA resource '{}'", this.scopeInum);
		try {
			String scopeDn = scopeDescriptionService.getDnForScopeDescription(this.scopeInum);
			this.scopeDescription = scopeDescriptionService.getScopeDescriptionByDn(scopeDn);
			this.authorizationPolicies = getInitialAuthorizationPolicies();
		} catch (BaseMappingException ex) {
			log.error("Failed to find scope description '{}'", this.scopeInum, ex);
			return OxTrustConstants.RESULT_FAILURE;
		}

		if (this.scopeDescription == null) {
			log.error("Scope description is null");
			return OxTrustConstants.RESULT_FAILURE;
		}

		initIconImage();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String cancel() {
		if (update) {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "UMA resource '#{updateScopeDescriptionAction.scopeDescription.displayName}' not updated");
		} else {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "New UMA resource not added");
		}

		conversationService.endConversation();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String save() {
		updateAuthorizationPolicies();
		this.scopeDescription.setDisplayName(this.scopeDescription.getDisplayName().trim());

		if (this.update) {
			// Update scope description
			try {
				scopeDescriptionService.updateScopeDescription(this.scopeDescription);
			} catch (BaseMappingException ex) {
				log.error("Failed to update scope description '{}'", this.scopeDescription.getId(), ex);
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update UMA resource '#{updateScopeDescriptionAction.scopeDescription.displayName}'");
				return OxTrustConstants.RESULT_FAILURE;
			}

	        log.debug("Scope description were updated successfully");
			facesMessages.add(FacesMessage.SEVERITY_INFO, "UMA resource '#{updateScopeDescriptionAction.scopeDescription.displayName}' updated successfully");

			return OxTrustConstants.RESULT_SUCCESS;
		} else {
			// Check if scope description with this name already exist
			UmaScopeDescription exampleScopeDescription = new UmaScopeDescription();
			exampleScopeDescription.setDn(scopeDescriptionService.getDnForScopeDescription(null));
			exampleScopeDescription.setId(scopeDescription.getId());

			String inum = scopeDescriptionService.generateInumForNewScopeDescription();
			String scopeDescriptionDn = scopeDescriptionService.getDnForScopeDescription(inum);

			this.scopeDescription.setInum(inum);
			this.scopeDescription.setDn(scopeDescriptionDn);
			this.scopeDescription.setOwner(identity.getUser().getDn());
            this.scopeDescription.setId(scopeDescription.getId());

			// Save scope description
			try {
				scopeDescriptionService.addScopeDescription(this.scopeDescription);
			} catch (BaseMappingException ex) {
				log.error("Failed to add new UMA resource '{}'", this.scopeDescription.getId(), ex);
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to add new UMA resource");

                return OxTrustConstants.RESULT_FAILURE;
			}

	        log.debug("Scope description were add successfully");
			facesMessages.add(FacesMessage.SEVERITY_INFO, "New UMA resource '#{updateScopeDescriptionAction.scopeDescription.displayName}' added successfully");
			conversationService.endConversation();

			this.update = true;
			this.scopeInum = inum; 

			return OxTrustConstants.RESULT_UPDATE;
		}
	}

	public String delete() {
		if (update) {
			// Remove scope description
			try {
				scopeDescriptionService.removeScopeDescription(this.scopeDescription);

				facesMessages.add(FacesMessage.SEVERITY_INFO, "UMA resource '#{updateScopeDescriptionAction.scopeDescription.displayName}' removed successfully");
				conversationService.endConversation();

				return OxTrustConstants.RESULT_SUCCESS;
			} catch (BaseMappingException ex) {
				log.error("Failed to remove scope description {}", this.scopeDescription.getId(), ex);
			}
		}

		facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to remove UMA resource '#{updateScopeDescriptionAction.scopeDescription.displayName}'");

		return OxTrustConstants.RESULT_FAILURE;
	}

	public void removeIconImage() {
		this.curIconImage = null;
		this.scopeDescription.setFaviconImageAsXml(null);
	}

	@PreDestroy
	public void destroy() throws Exception {
		cancel();
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
			this.scopeDescription.setFaviconImageAsXml(jsonService.objectToJson(this.curIconImage));
		} catch (Exception ex) {
			log.error("Failed to store icon image: '{}'", newIcon, ex);
		}
	}

	private void initIconImage() {
		String faviconImageAsXml = this.scopeDescription.getFaviconImageAsXml();
		if (StringHelper.isNotEmpty(faviconImageAsXml)) {
			try {
				this.curIconImage = jsonService.jsonToObject(faviconImageAsXml, GluuImage.class);
			} catch (Exception ex) {
				log.error("Faield to deserialize image: '{}'", faviconImageAsXml, ex);
			}
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
		if ((this.scopeDescription.getAuthorizationPolicies() == null) || (this.scopeDescription.getAuthorizationPolicies().size() == 0)) {
			return result;
		}

		List<DisplayNameEntry> displayNameEntries = lookupService.getDisplayNameEntries(customScriptService.baseDn(),
				this.scopeDescription.getAuthorizationPolicies());
		if (displayNameEntries != null) {
			for (DisplayNameEntry displayNameEntry : displayNameEntries) {
				result.add(new CustomScript(displayNameEntry.getDn(), displayNameEntry.getInum(), displayNameEntry.getDisplayName()));
			}
		}

		return result;
	}

	private void updateAuthorizationPolicies() {
		if (this.authorizationPolicies == null || this.authorizationPolicies.size() == 0) {
			this.scopeDescription.setAuthorizationPolicies(null);
			return;
		}

		List<String> tmpAuthorizationPolicies = new ArrayList<String>();
		for (CustomScript authorizationPolicy : this.authorizationPolicies) {
			tmpAuthorizationPolicies.add(authorizationPolicy.getDn());
		}

		this.scopeDescription.setAuthorizationPolicies(tmpAuthorizationPolicies);
	}

	public void acceptSelectAuthorizationPolicies() {
		if (this.availableAuthorizationPolicies == null) {
			return;
		}

		Set<String> addedAuthorizationPolicyInums = getAddedAuthorizationPolicyInums();

		for (SelectableEntity<CustomScript> availableAuthorizationPolicy : this.availableAuthorizationPolicies) {
			CustomScript authorizationPolicy = availableAuthorizationPolicy.getEntity();
			if (availableAuthorizationPolicy.isSelected() && !addedAuthorizationPolicyInums.contains(authorizationPolicy.getInum())) {
				addAuthorizationPolicy(authorizationPolicy);
			}

			if (!availableAuthorizationPolicy.isSelected() && addedAuthorizationPolicyInums.contains(authorizationPolicy.getInum())) {
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
			List<CustomScript> availableScripts = customScriptService.findCustomScripts(Arrays.asList(CustomScriptType.UMA_RPT_POLICY), CUSTOM_SCRIPT_RETURN_ATTRIBUTES);

			List<SelectableEntity<CustomScript>> tmpAvailableAuthorizationPolicies = new ArrayList<SelectableEntity<CustomScript>>();
			for (CustomScript authorizationPolicy : availableScripts) {
				tmpAvailableAuthorizationPolicies.add(new SelectableEntity<CustomScript>(authorizationPolicy));
			}
			
			this.availableAuthorizationPolicies = tmpAvailableAuthorizationPolicies;
			selectAddedAuthorizationPolicies();
		} catch (BaseMappingException ex) {
			log.error("Failed to find available authorization policies", ex);
		}

	}

	private void selectAddedAuthorizationPolicies() {
		Set<String> addedAuthorizationPolicyInums = getAddedAuthorizationPolicyInums();

		for (SelectableEntity<CustomScript> availableAuthorizationPolicy : this.availableAuthorizationPolicies) {
			availableAuthorizationPolicy.setSelected(addedAuthorizationPolicyInums.contains(availableAuthorizationPolicy.getEntity().getInum()));
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

	public UmaScopeDescription getScopeDescription() {
		return scopeDescription;
	}

	public List<SelectableEntity<CustomScript>> getAvailableAuthorizationPolicies() {
		return this.availableAuthorizationPolicies;
	}

	public List<CustomScript> getAuthorizationPolicies() {
		return authorizationPolicies;
	}
}
