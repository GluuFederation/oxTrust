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

import org.gluu.oxtrust.ldap.service.ImageService;
import org.gluu.oxtrust.ldap.service.uma.ScopeDescriptionService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.service.custom.CustomScriptService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;
import org.xdi.model.DisplayNameEntry;
import org.xdi.model.GluuImage;
import org.xdi.model.SelectableEntity;
import org.xdi.model.custom.script.CustomScriptType;
import org.xdi.model.custom.script.model.CustomScript;
import org.xdi.oxauth.model.uma.persistence.InternalExternal;
import org.xdi.oxauth.model.uma.persistence.ScopeDescription;
import org.xdi.service.JsonService;
import org.xdi.service.LookupService;
import org.xdi.util.StringHelper;

/**
 * Action class for view and update UMA scope description
 * 
 * @author Yuriy Movchan Date: 11/21/2012
 */
@Name("updateScopeDescriptionAction")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{identity.loggedIn}")
public class UpdateScopeDescriptionAction implements Serializable {

	private static final long serialVersionUID = 6180729281938167478L;

	private static final String[] CUSTOM_SCRIPT_RETURN_ATTRIBUTES = { "inum", "displayName", "description" };

	@Logger
	private Log log;

	@In
	protected GluuCustomPerson currentPerson;

	@In
	protected ScopeDescriptionService scopeDescriptionService;

	@In
	private ImageService imageService;
	
	@In
	private JsonService jsonService;

	@In
	private LookupService lookupService;
	
	@In
	private CustomScriptService customScriptService;

	private String scopeInum;

	private ScopeDescription scopeDescription;

	private GluuImage curIconImage;

	private List<CustomScript> authorizationPolicies;
	private List<SelectableEntity<CustomScript>> availableAuthorizationPolicies;

	private boolean update;

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public String modify() {
		if (this.scopeDescription != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.update = StringHelper.isNotEmpty(this.scopeInum);

		try {
			scopeDescriptionService.prepareScopeDescriptionBranch();
		} catch (LdapMappingException ex) {
			log.error("Failed to initialize form", ex);
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

		this.scopeDescription = new ScopeDescription();
		this.authorizationPolicies = getInitialAuthorizationPolicies();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private String update() {
		if (this.scopeDescription != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		log.debug("Loading UMA scope description '{0}'", this.scopeInum);
		try {
			String scopeDn = scopeDescriptionService.getDnForScopeDescription(this.scopeInum);
			this.scopeDescription = scopeDescriptionService.getScopeDescriptionByDn(scopeDn);
			this.authorizationPolicies = getInitialAuthorizationPolicies();
		} catch (LdapMappingException ex) {
			log.error("Failed to find scope description '{0}'", ex, this.scopeInum);
			return OxTrustConstants.RESULT_FAILURE;
		}

		if (this.scopeDescription == null) {
			log.error("Scope description is null");
			return OxTrustConstants.RESULT_FAILURE;
		}

		initIconImage();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void cancel() {
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public String save() {
		updateAuthorizationPolicies();

		if (this.update) {
			scopeDescription.setRevision(String.valueOf(StringHelper.toInteger(scopeDescription.getRevision(), 0) + 1));
			// Update scope description
			try {
				scopeDescriptionService.updateScopeDescription(this.scopeDescription);
			} catch (LdapMappingException ex) {
				log.error("Failed to update scope description '{0}'", ex, this.scopeDescription.getId());
				return OxTrustConstants.RESULT_FAILURE;
			}
		} else {
			// Check if scope description with this name already exist
			ScopeDescription exampleScopeDescription = new ScopeDescription();
			exampleScopeDescription.setDn(scopeDescriptionService.getDnForScopeDescription(null));
			exampleScopeDescription.setId(scopeDescription.getId());
			if (InternalExternal.INTERNAL.equals(scopeDescription.getType()) && scopeDescriptionService.containsScopeDescription(exampleScopeDescription)) {
				return OxTrustConstants.RESULT_DUPLICATE;
			}

			// Prepare score description
			this.scopeDescription.setRevision(String.valueOf(0));

			String inum = scopeDescriptionService.generateInumForNewScopeDescription();
			String scopeDescriptionDn = scopeDescriptionService.getDnForScopeDescription(inum);

			this.scopeDescription.setInum(inum);
			this.scopeDescription.setDn(scopeDescriptionDn);
			this.scopeDescription.setOwner(currentPerson.getDn());

			// Save scope description
			try {
				scopeDescriptionService.addScopeDescription(this.scopeDescription);
			} catch (LdapMappingException ex) {
				log.error("Failed to add new scope description '{0}'", ex, this.scopeDescription.getId());
				return OxTrustConstants.RESULT_FAILURE;
			}

			this.update = true;
		}

		log.debug("Scope description were {0} successfully", (this.update ? "added" : "updated"));
		return OxTrustConstants.RESULT_SUCCESS;
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public String delete() {
		if (update) {
			// Remove scope description
			try {
				scopeDescriptionService.removeScopeDescription(this.scopeDescription);
				return OxTrustConstants.RESULT_SUCCESS;
			} catch (LdapMappingException ex) {
				log.error("Failed to remove scope description {0}", ex, this.scopeDescription.getId());
			}
		}

		return OxTrustConstants.RESULT_FAILURE;
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void removeIconImage() {
		this.curIconImage = null;
		this.scopeDescription.setFaviconImageAsXml(null);
	}

	@Destroy
	public void destroy() throws Exception {
		cancel();
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
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

		GluuImage newIcon = imageService.constructImageWithThumbnail(currentPerson, uploadedFile, 16, 16);
		this.curIconImage = newIcon;
		try {
			this.scopeDescription.setFaviconImageAsXml(jsonService.objectToJson(this.curIconImage));
		} catch (Exception ex) {
			log.error("Failed to store icon image: '{0}'", ex, newIcon);
		}
	}

	private void initIconImage() {
		String faviconImageAsXml = this.scopeDescription.getFaviconImageAsXml();
		if (StringHelper.isNotEmpty(faviconImageAsXml)) {
			try {
				this.curIconImage = jsonService.jsonToObject(faviconImageAsXml, GluuImage.class);
			} catch (Exception ex) {
				log.error("Faield to deserialize image: '{0}'", ex, faviconImageAsXml);
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

	@Restrict("#{s:hasPermission('uma', 'access')}")
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

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void cancelSelectAuthorizationPolicies() {
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void addAuthorizationPolicy(CustomScript addAuthorizationPolicy) {
		if (addAuthorizationPolicy == null) {
			return;
		}

		this.authorizationPolicies.add(addAuthorizationPolicy);
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
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
	
	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void searchAvailableAuthorizationPolicies() {
		if (this.availableAuthorizationPolicies != null) {
			selectAddedAuthorizationPolicies();
			return;
		}

		try {
			List<CustomScript> availableScripts = customScriptService.findCustomScripts(Arrays.asList(CustomScriptType.UMA_AUTHORIZATION_POLICY), CUSTOM_SCRIPT_RETURN_ATTRIBUTES);

			List<SelectableEntity<CustomScript>> tmpAvailableAuthorizationPolicies = new ArrayList<SelectableEntity<CustomScript>>();
			for (CustomScript authorizationPolicy : availableScripts) {
				tmpAvailableAuthorizationPolicies.add(new SelectableEntity<CustomScript>(authorizationPolicy));
			}
			
			this.availableAuthorizationPolicies = tmpAvailableAuthorizationPolicies;
			selectAddedAuthorizationPolicies();
		} catch (LdapMappingException ex) {
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

	public ScopeDescription getScopeDescription() {
		return scopeDescription;
	}

	public List<SelectableEntity<CustomScript>> getAvailableAuthorizationPolicies() {
		return this.availableAuthorizationPolicies;
	}

	public List<CustomScript> getAuthorizationPolicies() {
		return authorizationPolicies;
	}

}
