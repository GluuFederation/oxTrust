/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action.uma;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.model.DisplayNameEntry;
import org.gluu.model.SelectableEntity;
import org.gluu.oxauth.model.uma.persistence.UmaResource;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.security.Identity;
import org.gluu.oxtrust.service.ClientService;
import org.gluu.oxtrust.service.uma.ResourceSetService;
import org.gluu.oxtrust.service.uma.UmaScopeService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.annotation.ObjectClass;
import org.gluu.persist.exception.BasePersistenceException;
import org.gluu.service.LookupService;
import org.gluu.service.security.Secure;
import org.gluu.util.SelectableEntityHelper;
import org.gluu.util.StringHelper;
import org.gluu.util.Util;
import org.oxauth.persistence.model.Scope;
import org.slf4j.Logger;

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
	private UmaScopeService scopeDescriptionService;

	@Inject
	private ClientService clientService;

	@Inject
	private LookupService lookupService;

	private String oxId;
	private UmaResource resource;
	private List<DisplayNameEntry> scopes;
	private List<DisplayNameEntry> clients;
	private List<String> resources;

	private List<SelectableEntity<Scope>> availableScopes;
	private String searchAvailableScopePattern, oldSearchAvailableScopePattern;

	private List<SelectableEntity<OxAuthClient>> availableClients;
	private String searchAvailableClientPattern, oldSearchAvailableClientPattern;

	private String newResource;

	private boolean update;
	private String scopeSelection = "Scopes";

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
		this.clientList = new ArrayList<OxAuthClient>();
		this.resources = new ArrayList<String>();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private String update() {
		log.debug("Loading UMA resource set '{}'", this.oxId);
		try {
			String resourceDn = umaResourcesService.getDnForResource(this.oxId);
			this.resource = umaResourcesService.getResourceByDn(resourceDn);
		} catch (BasePersistenceException ex) {
			log.error("Failed to find resource set '{}'", this.oxId, ex);
			return OxTrustConstants.RESULT_FAILURE;
		}

		if (this.resource == null) {
			log.error("Resource set is null");
			return OxTrustConstants.RESULT_FAILURE;
		}

		this.scopes = getScopesDisplayNameEntries();
		this.clients = getClientDisplayNameEntries();

		this.clientList = new ArrayList<OxAuthClient>();
		List<String> list = this.resource.getClients();
		if (list != null) {
			for (String clientDn : list) {
				OxAuthClient oxAuthClient = clientService.getClientByDn(clientDn);
				if (oxAuthClient != null) {
					clientList.add(oxAuthClient);
				}

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
			facesMessages.add(FacesMessage.SEVERITY_INFO,
					"UMA resource '#{updateResourceAction.resource.name}' not updated");
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
			if (resourceWithSameNameExistInUpdate()) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "A resource with same name already exist 1.");
				return OxTrustConstants.RESULT_FAILURE;
			}
			resource.setRev(resource.getRev() + 1);
			try {
				umaResourcesService.updateResource(this.resource);
			} catch (BasePersistenceException ex) {
				log.error("Failed to update resource set '{}'", this.resource.getInum(), ex);
				facesMessages.add(FacesMessage.SEVERITY_ERROR,
						"Failed to update UMA resource '#{updateResourceAction.resource.name}'");
				return OxTrustConstants.RESULT_FAILURE;
			}
			log.debug("Resource were updated successfully");
			facesMessages.add(FacesMessage.SEVERITY_INFO,
					"UMA resource '#{updateResourceAction.resource.name}' updated successfully");
			return OxTrustConstants.RESULT_SUCCESS;
		} else {
			if (resourceWithSameNameExist()) {
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "A resource with same name already exist 2.");
				return OxTrustConstants.RESULT_FAILURE;
			}
			String id = String.valueOf(System.currentTimeMillis());
			String resourceSetDn = umaResourcesService.getDnForResource(id);
			this.resource.setDn(resourceSetDn);
			this.resource.setId(id);
			this.resource.setRev(1);
			this.resource.setCreator(identity.getUser().getDn());
			try {
				umaResourcesService.addResource(this.resource);
			} catch (BasePersistenceException ex) {
				log.error("Failed to add new resource set '{}'", this.resource.getInum(), ex);
				facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to add new UMA resource");
				return OxTrustConstants.RESULT_FAILURE;
			}
			facesMessages.add(FacesMessage.SEVERITY_INFO,
					"New UMA resource '#{updateResourceAction.resource.name}' added successfully");
			conversationService.endConversation();
			this.update = true;
			this.oxId = id;
			return OxTrustConstants.RESULT_UPDATE;
		}
	}

	private boolean resourceWithSameNameExist() {
		return umaResourcesService.getAllResources(1000).stream()
				.anyMatch(e -> e.getName().equalsIgnoreCase(this.resource.getName()));
	}

	private boolean resourceWithSameNameExistInUpdate() {
		List<UmaResource> values = umaResourcesService.getAllResources(1000).stream()
				.filter(e -> e.getId().equalsIgnoreCase(this.resource.getId())).collect(Collectors.toList());
		return !values.stream().noneMatch(e -> !e.getId().equalsIgnoreCase(this.resource.getId()));
	}

	public String delete() {
		if (update) {
			try {
				umaResourcesService.removeResource(this.resource);
				facesMessages.add(FacesMessage.SEVERITY_INFO,
						"UMA resource '#{updateResourceAction.resource.name}' removed successfully");
				conversationService.endConversation();
				return OxTrustConstants.RESULT_SUCCESS;
			} catch (BasePersistenceException ex) {
				log.error("Failed to remove resource set {}", this.resource.getInum(), ex);
			}
		}
		facesMessages.add(FacesMessage.SEVERITY_ERROR,
				"Failed to remove UMA resource '#{updateResourceAction.resource.name}'");
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
			List<Scope> resultScopeDescriptions;
			if (StringHelper.isEmpty(this.searchAvailableScopePattern)) {
				resultScopeDescriptions = scopeDescriptionService.getAllUmaScopes(100);
			} else {
				resultScopeDescriptions = scopeDescriptionService.findUmaScopes(this.searchAvailableScopePattern, 100);
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

		for (SelectableEntity<Scope> availableScope : this.availableScopes) {
			availableScope.setSelected(addedScopeInums.contains(availableScope.getEntity().getInum()));
		}
	}

	public void acceptSelectScopes() {
		Set<String> addedScopeInums = getAddedScopesInums();
		for (SelectableEntity<Scope> availableScope : this.availableScopes) {
			Scope scopeDescription = availableScope.getEntity();
			String scopeDescriptionInum = scopeDescription.getInum();
			if (availableScope.isSelected() && !addedScopeInums.contains(scopeDescriptionInum)) {
				removeScope(scopeDescriptionInum);
				addScope(scopeDescription);
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

	public void addScope(Scope scope) {
		DisplayNameEntry oneScope = new DisplayNameEntry(scope.getDn(), scope.getId(), scope.getDisplayName());
		this.scopes.add(oneScope);
	}

	public void removeScope(String inum) {
		if (StringHelper.isEmpty(inum)) {
			return;
		}

		String removeScopeDn = scopeDescriptionService.getDnForScope(inum);

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
		List<ScopeDisplayNameEntry> tmp = lookupService.getDisplayNameEntries(scopeDescriptionService.getDnForScope(null),
				ScopeDisplayNameEntry.class, this.resource.getScopes());
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
			this.availableClients = SelectableEntityHelper.convertToSelectableEntityModel(
					clientService.searchClients(this.searchAvailableClientPattern, 100));
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
				removeClient(oxAuthClientInum);
				addClient(oxAuthClient);
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

	public void addClient(OxAuthClient client) {
		this.clientList.add(client);
	}

	public void removeClient(String inum) {
		if (StringHelper.isEmpty(inum)) {
			return;
		}

		String removeClientInum = clientService.getDnForClient(inum);

		for (Iterator<OxAuthClient> iterator = this.clientList.iterator(); iterator.hasNext();) {
			OxAuthClient oneClient = iterator.next();
			if (removeClientInum.equals(oneClient.getDn())) {
				iterator.remove();
				break;
			}
		}
	}

	private void updateClients() {
		if ((this.clientList == null) || (this.clientList.size() == 0)) {
			this.resource.setClients(null);
			return;
		}

		List<String> tmpClients = new ArrayList<String>();
		for (OxAuthClient client : this.clientList) {
			tmpClients.add(client.getDn());
		}

		this.resource.setClients(tmpClients);
	}

	private List<DisplayNameEntry> getClientDisplayNameEntries() {
		List<DisplayNameEntry> result = new ArrayList<DisplayNameEntry>();
		List<ClientDisplayNameEntry> tmp = lookupService.getDisplayNameEntries(clientService.getDnForClient(null),
				ClientDisplayNameEntry.class, this.resource.getClients());
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

	public List<SelectableEntity<Scope>> getAvailableScopes() {
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

	@ObjectClass(value = "oxAuthCustomScope")
	class ScopeDisplayNameEntry extends DisplayNameEntry {

		public ScopeDisplayNameEntry() {
			super();
		}
	}

	@ObjectClass(value = "oxAuthClient")
	class ClientDisplayNameEntry extends DisplayNameEntry {

		public ClientDisplayNameEntry() {
			super();
		}
	}

}
