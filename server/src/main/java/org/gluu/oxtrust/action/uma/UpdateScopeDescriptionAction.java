package org.gluu.oxtrust.action.uma;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.gluu.oxtrust.ldap.service.ClientService;
import org.gluu.oxtrust.ldap.service.ImageService;
import org.gluu.oxtrust.ldap.service.LookupService;
import org.gluu.oxtrust.ldap.service.uma.ScopeDescriptionService;
import org.gluu.oxtrust.model.DisplayNameEntry;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.OxAuthClient;
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
import org.xdi.model.GluuImage;
import org.xdi.model.SelectableEntity;
import org.xdi.oxauth.model.uma.persistence.InternalExternal;
import org.xdi.oxauth.model.uma.persistence.ScopeDescription;
import org.xdi.service.JsonService;
import org.xdi.util.SelectableEntityHelper;
import org.xdi.util.StringHelper;
import org.xdi.util.Util;

/**
 * Action class for view and update scope description
 * 
 * @author Yuriy Movchan Date: 11/21/2012
 */
@Name("updateScopeDescriptionAction")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{identity.loggedIn}")
public class UpdateScopeDescriptionAction implements Serializable {

	private static final long serialVersionUID = 6180729281938167478L;

	@Logger
	private Log log;

	@In
	protected GluuCustomPerson currentPerson;

	@In
	protected ScopeDescriptionService scopeDescriptionService;

	@In
	private ClientService clientService;

	@In
	private ImageService imageService;
	
	@In
	private JsonService jsonService;

	@In
	private LookupService lookupService;

	private String scopeInum;

	private ScopeDescription scopeDescription;

	private GluuImage curIconImage;

	private List<DisplayNameEntry> clients;

	private List<SelectableEntity<OxAuthClient>> availableClients;
	private String searchAvailableClientPattern, oldSearchAvailableClientPattern;

	private boolean update;

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public String modify() {
		if (this.scopeDescription != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.update = StringHelper.isNotEmpty(this.scopeInum);

		try {
			scopeDescriptionService.prepareScopeDescriptionBranch();
		} catch (Exception ex) {
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
		this.clients = new ArrayList<DisplayNameEntry>();

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

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void searchAvailableClients() {
		if (Util.equals(this.oldSearchAvailableClientPattern, this.searchAvailableClientPattern)) {
			return;
		}

		try {
			this.availableClients = SelectableEntityHelper.convertToSelectableEntityModel(clientService.searchClients(
					this.searchAvailableClientPattern, 100));
			this.oldSearchAvailableClientPattern = this.searchAvailableClientPattern;

			selectAddedClients();
		} catch (Exception ex) {
			log.error("Failed to find clients", ex);
		}
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void selectAddedClients() {
		Set<String> addedClientInums = getAddedClientsInums();

		for (SelectableEntity<OxAuthClient> availableClient : this.availableClients) {
			availableClient.setSelected(addedClientInums.contains(availableClient.getEntity().getInum()));
		}
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void acceptSelectClients() {
		Set<String> addedClientInums = getAddedClientsInums();

		for (SelectableEntity<OxAuthClient> availableClient : this.availableClients) {
			OxAuthClient oxAuthClient = availableClient.getEntity();
			String oxAuthClientInum = oxAuthClient.getInum();
			if (availableClient.isSelected() && !addedClientInums.contains(oxAuthClientInum)) {
				addClient(oxAuthClient);
			}

			if (!availableClient.isSelected() && addedClientInums.contains(oxAuthClientInum)) {
				removeClient(oxAuthClientInum);
			}
		}
	}

	private Set<String> getAddedClientsInums() {
		Set<String> addedClientInums = new HashSet<String>();

		if (this.availableClients == null) {
			return addedClientInums;
		}

		for (DisplayNameEntry group : this.clients) {
			addedClientInums.add(group.getInum());
		}

		return addedClientInums;
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void cancelSelectClients() {
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void addClient(OxAuthClient group) {
		DisplayNameEntry oneClient = new DisplayNameEntry(group.getDn(), group.getInum(), group.getDisplayName());
		this.clients.add(oneClient);
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void removeClient(String inum) {
		if (StringHelper.isEmpty(inum)) {
			return;
		}

		String removeClientInum = clientService.getDnForClient(inum);

		for (Iterator<DisplayNameEntry> iterator = this.clients.iterator(); iterator.hasNext();) {
			DisplayNameEntry oneClient = iterator.next();
			if (removeClientInum.equals(oneClient.getDn())) {
				iterator.remove();
				break;
			}
		}
	}

	public List<DisplayNameEntry> getClients() {
		return clients;
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

	public String getSearchAvailableClientPattern() {
		return searchAvailableClientPattern;
	}

	public void setSearchAvailableClientPattern(String searchAvailableClientPattern) {
		this.searchAvailableClientPattern = searchAvailableClientPattern;
	}

	public List<SelectableEntity<OxAuthClient>> getAvailableClients() {
		return availableClients;
	}

}
