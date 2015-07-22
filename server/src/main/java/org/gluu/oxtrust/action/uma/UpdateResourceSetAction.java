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

import org.gluu.oxtrust.ldap.service.ClientService;
import org.gluu.oxtrust.ldap.service.uma.ResourceSetService;
import org.gluu.oxtrust.ldap.service.uma.ScopeDescriptionService;
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
import org.xdi.model.DisplayNameEntry;
import org.xdi.model.SelectableEntity;
import org.xdi.oxauth.model.uma.persistence.ResourceSet;
import org.xdi.oxauth.model.uma.persistence.ScopeDescription;
import org.xdi.service.LookupService;
import org.xdi.util.SelectableEntityHelper;
import org.xdi.util.StringHelper;
import org.xdi.util.Util;

/**
 * Action class for view and update resource sets
 * 
 * @author Yuriy Movchan Date: 11/21/2012
 */
@Name("updateResourceSetAction")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{identity.loggedIn}")
public class UpdateResourceSetAction implements Serializable {

	private static final long serialVersionUID = 9180729281938167478L;

	@Logger
	private Log log;

	@In
	protected GluuCustomPerson currentPerson;

	@In
	private ResourceSetService resourceSetService;

	@In
	private ScopeDescriptionService scopeDescriptionService;

	@In
	private ClientService clientService;

	@In
	private LookupService lookupService;

	private String resourceInum;

	private ResourceSet resourceSet;
	private List<DisplayNameEntry> scopes;
	private List<DisplayNameEntry> clients;
	private List<String> resources;

	private List<SelectableEntity<ScopeDescription>> availableScopes;
	private String searchAvailableScopePattern, oldSearchAvailableScopePattern;

	private List<SelectableEntity<OxAuthClient>> availableClients;
	private String searchAvailableClientPattern, oldSearchAvailableClientPattern;

	private String newResource;

	private boolean update;

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public String modify() {
		if (this.resourceSet != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.update = StringHelper.isNotEmpty(this.resourceInum);

		try {
			resourceSetService.prepareResourceSetBranch();
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
		this.resourceSet = new ResourceSet();

		this.scopes = new ArrayList<DisplayNameEntry>();
		this.clients = new ArrayList<DisplayNameEntry>();
		this.resources = new ArrayList<String>();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private String update() {
		log.debug("Loading UMA resource set '{0}'", this.resourceInum);
		try {
			String resourceDn = resourceSetService.getDnForResourceSet(this.resourceInum);
			this.resourceSet = resourceSetService.getResourceSetByDn(resourceDn);
		} catch (LdapMappingException ex) {
			log.error("Failed to find resource set '{0}'", ex, this.resourceInum);
			return OxTrustConstants.RESULT_FAILURE;
		}

		if (this.resourceSet == null) {
			log.error("Resource set is null");
			return OxTrustConstants.RESULT_FAILURE;
		}

		this.scopes = getScopesDisplayNameEntries();
		this.clients = getClientDisplayNameEntries();

		if (this.resourceSet.getResources() == null) {
			this.resources = new ArrayList<String>();
		} else {
			this.resources = new ArrayList<String>(this.resourceSet.getResources());
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void cancel() {
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public String save() {
		updateScopes();
		updateClients();
		updateResources();

		if (this.update) {
			resourceSet.setRev(String.valueOf(StringHelper.toInteger(resourceSet.getRev(), 0) + 1));
			// Update resource set
			try {
				resourceSetService.updateResourceSet(this.resourceSet);
			} catch (LdapMappingException ex) {
				log.error("Failed to update resource set '{0}'", ex, this.resourceSet.getInum());
				return OxTrustConstants.RESULT_FAILURE;
			}
		} else {
			// Prepare resource set
		    String id = String.valueOf(System.currentTimeMillis());
			String inum = resourceSetService.generateInumForNewResourceSet();
			String resourceSetDn = resourceSetService.getDnForResourceSet(inum);

			this.resourceSet.setId(id);
			this.resourceSet.setInum(inum);
			this.resourceSet.setDn(resourceSetDn);
			this.resourceSet.setRev(String.valueOf(0));
			this.resourceSet.setCreator(currentPerson.getDn());

			// Save resource set
			try {
				resourceSetService.addResourceSet(this.resourceSet);
			} catch (LdapMappingException ex) {
				log.error("Failed to add new resource set '{0}'", ex, this.resourceSet.getInum());
				return OxTrustConstants.RESULT_FAILURE;
			}

			this.update = true;
		}

		log.debug("Resource set were {0} successfully", (this.update ? "added" : "updated"));
		return OxTrustConstants.RESULT_SUCCESS;
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public String delete() {
		if (update) {
			// Remove resource set
			try {
				resourceSetService.removeResourceSet(this.resourceSet);
				return OxTrustConstants.RESULT_SUCCESS;
			} catch (LdapMappingException ex) {
				log.error("Failed to remove resource set {0}", ex, this.resourceSet.getInum());
			}
		}

		return OxTrustConstants.RESULT_FAILURE;
	}

	@Destroy
	public void destroy() throws Exception {
		cancel();
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void searchAvailableScopes() {
		if (Util.equals(this.oldSearchAvailableScopePattern, this.searchAvailableScopePattern)) {
			return;
		}

		try {
			this.availableScopes = SelectableEntityHelper.convertToSelectableEntityModel(scopeDescriptionService.findScopeDescriptions(
					this.searchAvailableScopePattern, 100));
			this.oldSearchAvailableScopePattern = this.searchAvailableScopePattern;

			selectAddedScopes();
		} catch (Exception ex) {
			log.error("Failed to find scopes", ex);
		}
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void selectAddedScopes() {
		Set<String> addedScopeInums = getAddedScopesInums();

		for (SelectableEntity<ScopeDescription> availableScope : this.availableScopes) {
			availableScope.setSelected(addedScopeInums.contains(availableScope.getEntity().getInum()));
		}
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void acceptSelectScopes() {
		Set<String> addedScopeInums = getAddedScopesInums();

		for (SelectableEntity<ScopeDescription> availableScope : this.availableScopes) {
			ScopeDescription scopeDescription = availableScope.getEntity();
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

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void cancelSelectScopes() {
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void addScope(ScopeDescription scope) {
		DisplayNameEntry oneScope = new DisplayNameEntry(scope.getDn(), scope.getId(), scope.getDisplayName());
		this.scopes.add(oneScope);
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
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
			this.resourceSet.setScopes(null);
			return;
		}

		List<String> tmpScopes = new ArrayList<String>();
		for (DisplayNameEntry scope : this.scopes) {
			tmpScopes.add(scope.getDn());
		}

		this.resourceSet.setScopes(tmpScopes);
	}

	private List<DisplayNameEntry> getScopesDisplayNameEntries() {
		List<DisplayNameEntry> result = new ArrayList<DisplayNameEntry>();
		List<DisplayNameEntry> tmp = lookupService.getDisplayNameEntries(scopeDescriptionService.getDnForScopeDescription(null),
				this.resourceSet.getScopes());
		if (tmp != null) {
			result.addAll(tmp);
		}

		return result;
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

		for (DisplayNameEntry clietn : this.clients) {
			addedClientInums.add(clietn.getInum());
		}

		return addedClientInums;
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void cancelSelectClients() {
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void addClient(OxAuthClient clietn) {
		DisplayNameEntry oneClient = new DisplayNameEntry(clietn.getDn(), clietn.getInum(), clietn.getDisplayName());
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

	private void updateClients() {
		if ((this.clients == null) || (this.clients.size() == 0)) {
			this.resourceSet.setClients(null);
			return;
		}

		List<String> tmpClients = new ArrayList<String>();
		for (DisplayNameEntry client : this.clients) {
			tmpClients.add(client.getDn());
		}

		this.resourceSet.setClients(tmpClients);
	}

	private List<DisplayNameEntry> getClientDisplayNameEntries() {
		List<DisplayNameEntry> result = new ArrayList<DisplayNameEntry>();
		List<DisplayNameEntry> tmp = lookupService.getDisplayNameEntries(clientService.getDnForClient(null), this.resourceSet.getClients());
		if (tmp != null) {
			result.addAll(tmp);
		}

		return result;
	}

	private void updateResources() {
		if ((this.resources == null) || (this.resources.size() == 0)) {
			this.resourceSet.setResources(null);
			return;
		}

		this.resourceSet.setResources(this.resources);
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void acceptResource() {
		if (!this.resources.contains(this.newResource)) {
			this.resources.add(this.newResource);
		}

		cancelResource();
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void cancelResource() {
		this.newResource = null;
	}

	@Restrict("#{s:hasPermission('uma', 'access')}")
	public void removeResource(String resource) {
		if (StringHelper.isNotEmpty(resource)) {
			this.resources.remove(resource);
		}
	}

	public boolean isUpdate() {
		return update;
	}

	public String getResourceInum() {
		return resourceInum;
	}

	public void setResourceInum(String resourceInum) {
		this.resourceInum = resourceInum;
	}

	public ResourceSet getResourceSet() {
		return resourceSet;
	}

	public List<DisplayNameEntry> getScopes() {
		return scopes;
	}

	public List<SelectableEntity<ScopeDescription>> getAvailableScopes() {
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

}
