/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action.uma;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.ldap.service.ClientService;
import org.gluu.oxtrust.ldap.service.uma.ResourceSetService;
import org.gluu.oxtrust.ldap.service.uma.ScopeDescriptionService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.exception.mapping.BaseMappingException;
import org.slf4j.Logger;
import org.xdi.model.DisplayNameEntry;
import org.xdi.model.SelectableEntity;
import org.xdi.oxauth.model.uma.persistence.UmaResource;
import org.xdi.oxauth.model.uma.persistence.UmaScopeDescription;
import org.xdi.service.LookupService;
import org.xdi.service.security.Secure;
import org.xdi.util.SelectableEntityHelper;
import org.xdi.util.StringHelper;
import org.xdi.util.Util;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Action class for view and update resource sets
 *
 * @author Yuriy Movchan Date: 11/21/2012
 */
@ConversationScoped
@Named
@Secure("#{permissionService.hasPermission('uma', 'access')}")
public class UpdateResourceAction implements Serializable {

	private static final long serialVersionUID = 9180729281938167478L;

	@Inject
	private Logger log;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	@Inject
	private Identity identity;

	@Inject
	private ResourceSetService umaResourcesService;

	@Inject
	private ScopeDescriptionService scopeDescriptionService;

	@Inject
	private ClientService clientService;

	@Inject
	private LookupService lookupService;

	private String oxId;
	private UmaResource resource;
	private List<DisplayNameEntry> scopes;
	private List<DisplayNameEntry> clients;
	private List<String> resources;

	private List<SelectableEntity<UmaScopeDescription>> availableScopes;
	private String searchAvailableScopePattern, oldSearchAvailableScopePattern;

	private List<SelectableEntity<OxAuthClient>> availableClients;
	private String searchAvailableClientPattern, oldSearchAvailableClientPattern;

	private String newResource;

	private boolean update;
	private String scopeSelection="Scopes";
	
	private List<OxAuthClient> clientList;
	
	public List<OxAuthClient> getClientList() {
		return clientList;
	}
	
	public void setClientList(List<OxAuthClient> clientList) {
		this.clientList = clientList;
	}
	

	public String getScopeSelection() {
		return scopeSelection;
	}

	public void setScopeSelection(String scopeSelection) {
		this.scopeSelection = scopeSelection;
	}

	public String modify() {
		if (this.resource != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.update = StringHelper.isNotEmpty(this.oxId);

		try {
			umaResourcesService.prepareResourceBranch();
		} catch (Exception ex) {
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
		this.resource = new UmaResource();

		this.scopes = new ArrayList<DisplayNameEntry>();
		this.clients = new ArrayList<DisplayNameEntry>();
		this.resources = new ArrayList<String>();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private String update() {
		log.debug("Loading UMA resource set '{}'", this.oxId);
		try {
			String resourceDn = umaResourcesService.getDnForResource(this.oxId);
			this.resource = umaResourcesService.getResourceByDn(resourceDn);
		} catch (BaseMappingException ex) {
			log.error("Failed to find resource set '{}'", this.oxId, ex);
			return OxTrustConstants.RESULT_FAILURE;
		}

		if (this.resource == null) {
			log.error("Resource set is null");
			return OxTrustConstants.RESULT_FAILURE;
		}

		this.scopes = getScopesDisplayNameEntries();
		this.clients = getClientDisplayNameEntries();
		
		
		List<String> list = this.resource.getClients();
		if (list != null) {
				clientList = new ArrayList<OxAuthClient>();
				for (String clientDn : list) {
					OxAuthClient oxAuthClient = clientService.getClientByDn(clientDn);
					clientList.add(oxAuthClient);
				}
		}
						

		if (this.resource.getResources() == null) {
			this.resources = new ArrayList<String>();
		} else {
			this.resources = new ArrayList<String>(this.resource.getResources());
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String cancel() {
		if (update) {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "UMA resource '#{updateResourceAction.resource.name}' not updated");
		} else {
			facesMessages.add(FacesMessage.SEVERITY_INFO, "New UMA resource not added");
		}

		conversationService.endConversation();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String save() {
		updateScopes();
		updateClients();
		updateResources();

		if (this.update) {
			resource.setRev(String.valueOf(StringHelper.toInteger(resource.getRev(), 0) + 1));
			// Update resource set
			try {
				umaResourcesService.updateResource(this.resource);
			} catch (BaseMappingException ex) {
				log.error("Failed to update resource set '{}'", this.resource.getInum(), ex);
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update UMA resource '#{updateResourceAction.resource.name}'");
				return OxTrustConstants.RESULT_FAILURE;
			}

			log.debug("Resource were updated successfully");
			facesMessages.add(FacesMessage.SEVERITY_INFO, "UMA resource '#{updateResourceAction.resource.name}' updated successfully");

			return OxTrustConstants.RESULT_SUCCESS;
		} else {
			// Prepare resource set
		    String id = String.valueOf(System.currentTimeMillis());
			String resourceSetDn = umaResourcesService.getDnForResource(id);
			this.resource.setDn(resourceSetDn);
			this.resource.setRev(String.valueOf(0));
			this.resource.setCreator(identity.getUser().getDn());

			// Save resource set
			try {
				umaResourcesService.addResource(this.resource);
			} catch (BaseMappingException ex) {
				log.error("Failed to add new resource set '{}'", this.resource.getInum(), ex);
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to add new UMA resource");

				return OxTrustConstants.RESULT_FAILURE;
			}
			 
			log.debug("Resource were add successfully");
			facesMessages.add(FacesMessage.SEVERITY_INFO, "New UMA resource '#{updateResourceAction.resource.name}' added successfully");
			conversationService.endConversation();

			this.update = true;
			this.oxId = id;
			
			return OxTrustConstants.RESULT_UPDATE;
		}
	}

	public String delete() {
		if (update) {
			// Remove resource set
			try {
				umaResourcesService.removeResource(this.resource);

				facesMessages.add(FacesMessage.SEVERITY_INFO, "UMA resource '#{updateResourceAction.resource.name}' removed successfully");
				conversationService.endConversation();

				return OxTrustConstants.RESULT_SUCCESS;
			} catch (BaseMappingException ex) {
				log.error("Failed to remove resource set {}", this.resource.getInum(), ex);
			}
		}

		facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to remove UMA resource '#{updateResourceAction.resource.name}'");

		return OxTrustConstants.RESULT_FAILURE;
	}

	@PreDestroy
	public void destroy() throws Exception {
		cancel();
	}

	public void searchAvailableScopes() {
		if (Util.equals(this.oldSearchAvailableScopePattern, this.searchAvailableScopePattern)) {
			return;
		}

		try {
			List<UmaScopeDescription> resultScopeDescriptions;
			if (StringHelper.isEmpty(this.searchAvailableScopePattern)) {
				resultScopeDescriptions = scopeDescriptionService.getAllScopeDescriptions(100);
			} else {
				resultScopeDescriptions = scopeDescriptionService.findScopeDescriptions(this.searchAvailableScopePattern, 100);
			}

			this.availableScopes = SelectableEntityHelper.convertToSelectableEntityModel(resultScopeDescriptions);
			this.oldSearchAvailableScopePattern = this.searchAvailableScopePattern;

			selectAddedScopes();
		} catch (Exception ex) {
			log.error("Failed to find scopes", ex);
		}
	}

	public void selectAddedScopes() {
		Set<String> addedScopeInums = getAddedScopesInums();

		for (SelectableEntity<UmaScopeDescription> availableScope : this.availableScopes) {
			availableScope.setSelected(addedScopeInums.contains(availableScope.getEntity().getInum()));
		}
	}

	public void acceptSelectScopes() {
		Set<String> addedScopeInums = getAddedScopesInums();

		for (SelectableEntity<UmaScopeDescription> availableScope : this.availableScopes) {
            UmaScopeDescription scopeDescription = availableScope.getEntity();
			String scopeDescriptionInum = scopeDescription.getInum();

			if (availableScope.isSelected() && !addedScopeInums.contains(scopeDescriptionInum)) {
				addScope(scopeDescription);
			}

			if (!availableScope.isSelected() && addedScopeInums.contains(scopeDescriptionInum)) {
				removeScope(scopeDescriptionInum);
			}
		}

	}

	private Set<String> getAddedScopesInums() {
		Set<String> addedScopeInums = new HashSet<String>();

		if (this.availableScopes == null) {
			return addedScopeInums;
		}

		for (DisplayNameEntry scope : this.scopes) {
			addedScopeInums.add(scope.getInum());
		}
		return addedScopeInums;
	}

	public void cancelSelectScopes() {
	}

	public void addScope(UmaScopeDescription scope) {
		DisplayNameEntry oneScope = new DisplayNameEntry(scope.getDn(), scope.getId(), scope.getDisplayName());
		this.scopes.add(oneScope);
	}

	public void removeScope(String inum) {
		if (StringHelper.isEmpty(inum)) {
			return;
		}

		String removeScopeDn = scopeDescriptionService.getDnForScopeDescription(inum);

		for (Iterator<DisplayNameEntry> iterator = this.scopes.iterator(); iterator.hasNext();) {
			DisplayNameEntry oneScope = iterator.next();
			if (removeScopeDn.equals(oneScope.getDn())) {
				iterator.remove();
				break;
			}
		}
	}

	private void updateScopes() {
		if ((this.scopes == null) || (this.scopes.size() == 0)) {
			this.resource.setScopes(null);
			return;
		}

		List<String> tmpScopes = new ArrayList<String>();
		for (DisplayNameEntry scope : this.scopes) {
			tmpScopes.add(scope.getDn());
		}

		this.resource.setScopes(tmpScopes);
	}

	private List<DisplayNameEntry> getScopesDisplayNameEntries() {
		List<DisplayNameEntry> result = new ArrayList<DisplayNameEntry>();
		List<DisplayNameEntry> tmp = lookupService.getDisplayNameEntries(scopeDescriptionService.getDnForScopeDescription(null),
				this.resource.getScopes());
		if (tmp != null) {
			result.addAll(tmp);
		}

		return result;
	}

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

	public void selectAddedClients() {
		Set<String> addedClientInums = getAddedClientsInums();

		for (SelectableEntity<OxAuthClient> availableClient : this.availableClients) {
			availableClient.setSelected(addedClientInums.contains(availableClient.getEntity().getInum()));
		}
	}

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

		for (DisplayNameEntry clietn : this.clients) {
			addedClientInums.add(clietn.getInum());
		}

		return addedClientInums;
	}

	public void cancelSelectClients() {
	}

	public void addClient(OxAuthClient clietn) {
		DisplayNameEntry oneClient = new DisplayNameEntry(clietn.getDn(), clietn.getInum(), clietn.getDisplayName());
		this.clients.add(oneClient);
	}

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

	private void updateClients() {
		if ((this.clients == null) || (this.clients.size() == 0)) {
			this.resource.setClients(null);
			return;
		}

		List<String> tmpClients = new ArrayList<String>();
		for (DisplayNameEntry client : this.clients) {
			tmpClients.add(client.getDn());
		}

		this.resource.setClients(tmpClients);
	}

	private List<DisplayNameEntry> getClientDisplayNameEntries() {
		List<DisplayNameEntry> result = new ArrayList<DisplayNameEntry>();
		List<DisplayNameEntry> tmp = lookupService.getDisplayNameEntries(clientService.getDnForClient(null), this.resource.getClients());
		if (tmp != null) {
			result.addAll(tmp);
		}

		return result;
	}

	private void updateResources() {
		if ((this.resources == null) || (this.resources.size() == 0)) {
			this.resource.setResources(null);
			return;
		}

		this.resource.setResources(this.resources);
	}

	public void acceptResource() {
		if (!this.resources.contains(this.newResource)) {
			this.resources.add(this.newResource);
		}

		cancelResource();
	}

	public void cancelResource() {
		this.newResource = null;
	}

	public void removeResource(String resource) {
		if (StringHelper.isNotEmpty(resource)) {
			this.resources.remove(resource);
		}
	}

	public boolean isUpdate() {
		return update;
	}

	public UmaResource getResource() {
		return resource;
	}

	public List<DisplayNameEntry> getScopes() {
		return scopes;
	}

	public List<SelectableEntity<UmaScopeDescription>> getAvailableScopes() {
		return availableScopes;
	}

	public String getSearchAvailableScopePattern() {
		return searchAvailableScopePattern;
	}

	public void setSearchAvailableScopePattern(String searchAvailableScopePattern) {
		this.searchAvailableScopePattern = searchAvailableScopePattern;
	}

	public List<DisplayNameEntry> getClients() {
		return clients;
	}

	public List<SelectableEntity<OxAuthClient>> getAvailableClients() {
		return this.availableClients;
	}

	public String getSearchAvailableClientPattern() {
		return searchAvailableClientPattern;
	}

	public void setSearchAvailableClientPattern(String searchAvailableClientPattern) {
		this.searchAvailableClientPattern = searchAvailableClientPattern;
	}

	public String getNewResource() {
		return newResource;
	}

	public void setNewResource(String newResource) {
		this.newResource = newResource;
	}

	public List<String> getResources() {
		return resources;
	}	

	public String getOxId() {
		return oxId;
	}

	public void setOxId(String oxId) {
		this.oxId = oxId;
	}

}
