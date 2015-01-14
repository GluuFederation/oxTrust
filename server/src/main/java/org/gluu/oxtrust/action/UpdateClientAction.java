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

import org.apache.commons.lang.StringUtils;
import org.gluu.oxtrust.ldap.service.ClientService;
import org.gluu.oxtrust.ldap.service.GroupService;
import org.gluu.oxtrust.ldap.service.ScopeService;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.OxAuthClient;
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
import org.xdi.model.SelectableEntity;
import org.xdi.oxauth.model.common.ResponseType;
import org.xdi.service.LookupService;
import org.xdi.util.StringHelper;
import org.xdi.util.Util;

/**
 * Action class for viewing and updating clients.
 * 
 * @author Reda Zerrad Date: 06.11.2012
 * @author Yuriy Movchan Date: 04/07/2014
 */
@Scope(ScopeType.CONVERSATION)
@Name("updateClientAction")
@Restrict("#{identity.loggedIn}")
public class UpdateClientAction implements Serializable {

	private static final long serialVersionUID = -5756470620039988876L;

	@Logger
	private Log log;

	@In
	private ClientService clientService;

	@In
	private ScopeService scopeService;

	@In
	private GroupService groupService;

	@In
	private LookupService lookupService;

	@In
	private FacesMessages facesMessages;

	private String inum;

	private boolean update;

	private OxAuthClient client;

	private List<String> loginUris;
	private List<String> logoutUris;

	private List<DisplayNameEntry> scopes;
	private List<DisplayNameEntry> groups;
	private List<ResponseType> responseTypes;

	// @NotNull
	// @Size(min = 2, max = 30, message =
	// "Length of search string should be between 2 and 30")
	private String searchAvailableScopePattern;
	private String oldSearchAvailableScopePattern;

	private String searchAvailableGroupPattern;
	private String oldSearchAvailableGroupPattern;

	private String availableLoginUri = "http://";
	private String availableLogoutUri = "http://";

	private List<OxAuthScope> availableScopes;
	private List<GluuGroup> availableGroups;
	private List<SelectableEntity<ResponseType>> availableResponseTypes;

	@Restrict("#{s:hasPermission('client', 'access')}")
	public String add() throws Exception {
		if (this.client != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.update = false;
		this.client = new OxAuthClient();

		try {
			this.loginUris = getNonEmptyStringList(client.getOxAuthRedirectURIs());
			this.logoutUris = getNonEmptyStringList(client.getOxAuthPostLogoutRedirectURIs());

			this.scopes = getInitialScopeDisplayNameEntiries();
			this.groups = getInitialGroupDisplayNameEntiries();
			this.responseTypes = getInitialResponseTypes();
		} catch (LdapMappingException ex) {
			log.error("Failed to prepare lists", ex);

			return OxTrustConstants.RESULT_FAILURE;
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	@Restrict("#{s:hasPermission('client', 'access')}")
	public String update() throws Exception {
		if (this.client != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.update = true;
		log.info("this.update : " + this.update);
		try {
			log.info("inum : " + inum);
			this.client = clientService.getClientByInum(inum);
		} catch (LdapMappingException ex) {
			log.error("Failed to find client {0}", ex, inum);
		}

		if (this.client == null) {
			log.error("Failed to load client {0}", inum);
			return OxTrustConstants.RESULT_FAILURE;
		}

		try {
			this.loginUris = getNonEmptyStringList(client.getOxAuthRedirectURIs());
			this.logoutUris = getNonEmptyStringList(client.getOxAuthPostLogoutRedirectURIs());

			this.scopes = getInitialScopeDisplayNameEntiries();
			this.groups = getInitialGroupDisplayNameEntiries();
			this.responseTypes = getInitialResponseTypes();
		} catch (LdapMappingException ex) {
			log.error("Failed to prepare lists", ex);
			return OxTrustConstants.RESULT_FAILURE;
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private List<String> getNonEmptyStringList(List<String> currentList) {
		if (currentList != null && currentList.size() > 0) {
			return new ArrayList<String>(currentList);
		} else {
			return new ArrayList<String>();
		}
	}

	@Restrict("#{s:hasPermission('client', 'access')}")
	public void cancel() {
	}

	@Restrict("#{s:hasPermission('client', 'access')}")
	public String save() throws Exception {
		updateLoginURIs();
		updateLogoutURIs();
		updateScopes();
		updateGroups();
		updateResponseTypes();

		if (update) {
			// Update client
			try {
				clientService.updateClient(this.client);
			} catch (LdapMappingException ex) {

				log.error("Failed to update client {0}", ex, this.inum);

				facesMessages.add(Severity.ERROR, "Failed to update client");
				return OxTrustConstants.RESULT_FAILURE;
			}
		} else {
			this.inum = clientService.generateInumForNewClient();
			String dn = clientService.getDnForClient(this.inum);

			// Save client
			this.client.setDn(dn);
			this.client.setInum(this.inum);
			try {
				clientService.addClient(this.client);
			} catch (LdapMappingException ex) {
				log.info("error saving client ");
				log.error("Failed to add new client {0}", ex, this.inum);

				facesMessages.add(Severity.ERROR, "Failed to add new client");
				return OxTrustConstants.RESULT_FAILURE;
			}

			this.update = true;
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	@Restrict("#{s:hasPermission('client', 'access')}")
	public String delete() throws Exception {
		if (update) {
			// Remove client
			try {
				clientService.removeClient(this.client);
				return OxTrustConstants.RESULT_SUCCESS;
			} catch (LdapMappingException ex) {
				log.error("Failed to remove client {0}", ex, this.inum);
			}
		}

		return OxTrustConstants.RESULT_FAILURE;
	}

	@Restrict("#{s:hasPermission('client', 'access')}")
	public void removeLoginURI(String uri) {
		removeFromList(this.loginUris, uri);
	}

	@Restrict("#{s:hasPermission('client', 'access')}")
	public void removeLogoutURI(String uri) {
		removeFromList(this.logoutUris, uri);
	}

	private void removeFromList(List<String> uriList, String uri) {
		if (StringUtils.isEmpty(uri)) {
			return;
		}

		for (Iterator<String> iterator = uriList.iterator(); iterator.hasNext();) {
			String tmpUri = iterator.next();
			if (uri.equals(tmpUri)) {
				iterator.remove();
				break;
			}
		}
	}

	private void addGroup(GluuGroup group) {
		DisplayNameEntry oneGroup = new DisplayNameEntry(group.getDn(), group.getInum(), group.getDisplayName());
		this.groups.add(oneGroup);
	}

	public void removeGroup(String inum) throws Exception {
		if (StringHelper.isEmpty(inum)) {
			return;
		}

		String removeGroupInum = groupService.getDnForGroup(inum);

		for (Iterator<DisplayNameEntry> iterator = this.groups.iterator(); iterator.hasNext();) {
			DisplayNameEntry oneGroup = iterator.next();
			if (removeGroupInum.equals(oneGroup.getDn())) {
				iterator.remove();
				break;
			}
		}
	}

	private void addScope(OxAuthScope scope) {
		DisplayNameEntry oneScope = new DisplayNameEntry(scope.getDn(), scope.getInum(), scope.getDisplayName());
		this.scopes.add(oneScope);
	}

	public void removeScope(String inum) throws Exception {
		if (StringHelper.isEmpty(inum)) {
			return;
		}

		String removeScopeInum = scopeService.getDnForScope(inum);

		for (Iterator<DisplayNameEntry> iterator = this.scopes.iterator(); iterator.hasNext();) {
			DisplayNameEntry oneScope = iterator.next();
			if (removeScopeInum.equals(oneScope.getDn())) {
				iterator.remove();
				break;
			}
		}
	}

	public void acceptSelectLoginUri() {
		if (StringHelper.isEmpty(this.availableLoginUri)) {
			return;
		}

		if (!this.loginUris.contains(this.availableLoginUri)) {
			this.loginUris.add(this.availableLoginUri);
		}

		this.availableLoginUri = "http://";
	}

	public void acceptSelectLogoutUri() {
		if (StringHelper.isEmpty(this.availableLogoutUri)) {
			return;
		}

		if (!this.logoutUris.contains(this.availableLogoutUri)) {
			this.logoutUris.add(this.availableLogoutUri);
		}

		this.availableLogoutUri = "http://";
	}

	public void acceptSelectScopes() {
		if (this.availableScopes == null) {
			return;
		}

		Set<String> addedScopeInums = new HashSet<String>();
		for (DisplayNameEntry scope : scopes) {
			addedScopeInums.add(scope.getInum());
		}

		for (OxAuthScope aScope : this.availableScopes) {
			if (aScope.isSelected() && !addedScopeInums.contains(aScope.getInum())) {
				addScope(aScope);
			}
		}
	}

	public void acceptSelectGroups() {
		if (this.availableGroups == null) {
			return;
		}

		Set<String> addedGroupInums = new HashSet<String>();
		for (DisplayNameEntry group : groups) {
			addedGroupInums.add(group.getInum());
		}

		for (GluuGroup aGroup : this.availableGroups) {
			if (aGroup.isSelected() && !addedGroupInums.contains(aGroup.getInum())) {
				addGroup(aGroup);
			}
		}
	}

	public void cancelSelectScopes() {
	}

	public void cancelSelectGroups() {
	}

	public void cancelSelectLoginUri() {
		this.availableLoginUri = "http://";
	}

	public void cancelSelectLogoutUri() {
		this.availableLogoutUri = "http://";
	}


	private void updateLoginURIs() {
		if (this.loginUris == null || this.loginUris.size() == 0) {
			this.client.setOxAuthRedirectURIs(null);
			return;
		}

		List<String> tmpUris = new ArrayList<String>();
		for (String uri : this.loginUris) {
			tmpUris.add(uri);
		}

		this.client.setOxAuthRedirectURIs(tmpUris);
	}

	private void updateLogoutURIs() {
		if (this.logoutUris == null || this.logoutUris.size() == 0) {
			this.client.setOxAuthPostLogoutRedirectURIs(null);
			return;
		}

		List<String> tmpUris = new ArrayList<String>();
		for (String uri : this.logoutUris) {
			tmpUris.add(uri);
		}

		this.client.setOxAuthPostLogoutRedirectURIs(tmpUris);

	}

	private void updateScopes() {
		if (this.scopes == null || this.scopes.size() == 0) {
			this.client.setOxAuthScopes(null);
			return;
		}

		List<String> tmpScopes = new ArrayList<String>();
		for (DisplayNameEntry scope : this.scopes) {
			tmpScopes.add(scope.getDn());
		}

		this.client.setOxAuthScopes(tmpScopes);
	}

	private void updateGroups() {
		if (this.groups == null || this.groups.size() == 0) {
			this.client.setOxAuthClientUserGroups(null);
			return;
		}

		List<String> tmpGroups = new ArrayList<String>();
		for (DisplayNameEntry group : this.groups) {
			tmpGroups.add(group.getDn());
		}

		this.client.setOxAuthClientUserGroups(tmpGroups);
	}

	private void updateResponseTypes() {
		List<ResponseType> currentResponseTypes = this.responseTypes;

		if (currentResponseTypes == null || currentResponseTypes.size() == 0) {
			this.client.setResponseTypes(null);
			return;
		}

		this.client.setResponseTypes(currentResponseTypes.toArray(new ResponseType[currentResponseTypes.size()]));
	}

	public void selectAddedScopes() {
		if (this.availableScopes == null) {
			return;
		}

		Set<String> addedScopeInums = new HashSet<String>();
		for (DisplayNameEntry scope : this.scopes) {
			addedScopeInums.add(scope.getInum());
		}

		for (OxAuthScope aScope : this.availableScopes) {
			aScope.setSelected(addedScopeInums.contains(aScope.getInum()));
		}
	}

	public void selectAddedGroups() {
		if (this.availableGroups == null) {
			return;
		}

		Set<String> addedGroupInums = new HashSet<String>();
		for (DisplayNameEntry group : this.groups) {
			addedGroupInums.add(group.getInum());
		}

		for (GluuGroup availableGroup : this.availableGroups) {
			availableGroup.setSelected(addedGroupInums.contains(availableGroup.getInum()));
		}
	}

	public void searchAvailableScopes() {
		if (Util.equals(this.oldSearchAvailableScopePattern, this.searchAvailableScopePattern)) {
			return;
		}

		try {

			this.availableScopes = scopeService.searchScopes(this.searchAvailableScopePattern, OxTrustConstants.searchPersonsSizeLimit);
			this.oldSearchAvailableScopePattern = this.searchAvailableScopePattern;
			selectAddedScopes();
		} catch (Exception ex) {
			log.error("Failed to find attributes", ex);
		}
	}

	public void searchAvailableGroups() {
		if (Util.equals(this.oldSearchAvailableGroupPattern, this.searchAvailableGroupPattern)) {
			return;
		}

		try {

			this.availableGroups = groupService.searchGroups(this.searchAvailableGroupPattern, OxTrustConstants.searchPersonsSizeLimit);
			this.oldSearchAvailableGroupPattern = this.searchAvailableGroupPattern;
			selectAddedGroups();
		} catch (Exception ex) {
			log.error("Failed to find groups", ex);
		}
	}

	private List<DisplayNameEntry> getInitialScopeDisplayNameEntiries() throws Exception {
		List<DisplayNameEntry> result = new ArrayList<DisplayNameEntry>();
		if ((client.getOxAuthScopes() == null) || (client.getOxAuthScopes().size() == 0)) {
			return result;
		}

		List<DisplayNameEntry> tmp = lookupService.getDisplayNameEntries(scopeService.getDnForScope(null), this.client.getOxAuthScopes());
		if (tmp != null) {
			result.addAll(tmp);
		}

		return result;
	}

	private List<DisplayNameEntry> getInitialGroupDisplayNameEntiries() throws Exception {
		List<DisplayNameEntry> result = new ArrayList<DisplayNameEntry>();
		if ((client.getOxAuthClientUserGroups() == null) || (client.getOxAuthClientUserGroups().size() == 0)) {
			return result;
		}

		List<DisplayNameEntry> tmp = lookupService.getDisplayNameEntries(groupService.getDnForGroup(null),
				this.client.getOxAuthClientUserGroups());
		if (tmp != null) {
			result.addAll(tmp);
		}

		return result;
	}

	private List<ResponseType> getInitialResponseTypes() {
		List<ResponseType> result = new ArrayList<ResponseType>();
		
		ResponseType[] currentResponseTypes = this.client.getResponseTypes();
		if ((currentResponseTypes == null) || (currentResponseTypes.length == 0)) {
			return result;
		}

		result.addAll(Arrays.asList(currentResponseTypes));

		return result;
	}

	@Restrict("#{s:hasPermission('client', 'access')}")
	public void acceptSelectResponseTypes() {
		List<ResponseType> addedResponseTypes = getResponseTypes();

		for (SelectableEntity<ResponseType> availableResponseType : this.availableResponseTypes) {
			ResponseType responseType = availableResponseType.getEntity();
			if (availableResponseType.isSelected() && !addedResponseTypes.contains(responseType)) {
				addResponseType(responseType.getValue());
			}

			if (!availableResponseType.isSelected() && addedResponseTypes.contains(responseType)) {
				removeResponseType(responseType.getValue());
			}
		}
	}

	@Restrict("#{s:hasPermission('client', 'access')}")
	public void cancelSelectResponseTypes() {
	}

	@Restrict("#{s:hasPermission('client', 'access')}")
	public void addResponseType(String value) {
		if (StringHelper.isEmpty(value)) {
			return;
		}

		ResponseType addResponseType = ResponseType.getByValue(value);
		if (addResponseType != null) {
			this.responseTypes.add(addResponseType);
		}
	}

	@Restrict("#{s:hasPermission('client', 'access')}")
	public void removeResponseType(String value) {
		if (StringHelper.isEmpty(value)) {
			return;
		}

		ResponseType removeResponseType = ResponseType.getByValue(value);
		if (removeResponseType != null) {
			this.responseTypes.remove(removeResponseType);
		}
	}
	
	@Restrict("#{s:hasPermission('client', 'access')}")
	public void searchAvailableResponseTypes() {
		if (this.availableResponseTypes != null) {
			selectAddedResponseTypes();
			return;
		}

		List<SelectableEntity<ResponseType>> tmpAvailableResponseTypes = new ArrayList<SelectableEntity<ResponseType>>();
		
		for (ResponseType responseType : ResponseType.values()) {
			tmpAvailableResponseTypes.add(new SelectableEntity<ResponseType>(responseType));
		}
		
		this.availableResponseTypes = tmpAvailableResponseTypes;
		selectAddedResponseTypes();
	}

	private void selectAddedResponseTypes() {
		List<ResponseType> addedResponseTypes = getResponseTypes();

		for (SelectableEntity<ResponseType> availableResponseType : this.availableResponseTypes) {
			availableResponseType.setSelected(addedResponseTypes.contains(availableResponseType.getEntity()));
		}
	}

	public String getInum() {
		return inum;
	}

	public void setInum(String inum) {
		this.inum = inum;
	}

	public OxAuthClient getClient() {
		return client;
	}

	public boolean isUpdate() {
		return update;
	}

	public String getAvailableLoginUri() {
		return availableLoginUri;
	}

	public void setAvailableLoginUri(String availableLoginUri) {
		this.availableLoginUri = availableLoginUri;
	}

	public String getAvailableLogoutUri() {
		return availableLogoutUri;
	}

	public void setAvailableLogoutUri(String availableLogoutUri) {
		this.availableLogoutUri = availableLogoutUri;
	}

	public List<OxAuthScope> getAvailableScopes() {
		return this.availableScopes;
	}

	public List<GluuGroup> getAvailableGroups() {
		return this.availableGroups;
	}

	public List<SelectableEntity<ResponseType>> getAvailableResponseTypes() {
		return this.availableResponseTypes;
	}

	public List<String> getLoginUris() {
		return loginUris;
	}

	public List<String> getLogoutUris() {
		return logoutUris;
	}

	public List<DisplayNameEntry> getScopes() {
		return this.scopes;
	}

	public List<DisplayNameEntry> getGroups() {
		return this.groups;
	}

	public List<ResponseType> getResponseTypes() {
		return responseTypes;
	}

	public String getSearchAvailableScopePattern() {
		return this.searchAvailableScopePattern;
	}

	public void setSearchAvailableScopePattern(String searchAvailableScopePattern) {
		this.searchAvailableScopePattern = searchAvailableScopePattern;
	}

	public String getSearchAvailableGroupPattern() {
		return this.searchAvailableGroupPattern;
	}

	public void setSearchAvailableGroupPattern(String searchAvailableGroupPattern) {
		this.searchAvailableGroupPattern = searchAvailableGroupPattern;
	}

}
