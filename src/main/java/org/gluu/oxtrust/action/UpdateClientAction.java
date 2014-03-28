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
import org.gluu.oxtrust.ldap.service.LookupService;
import org.gluu.oxtrust.ldap.service.ScopeService;
import org.gluu.oxtrust.model.DisplayNameEntry;
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
import org.xdi.model.SelectableEntity;
import org.xdi.oxauth.model.common.ResponseType;
import org.xdi.util.SelectableEntityHelper;
import org.xdi.util.StringHelper;
import org.xdi.util.Util;

/**
 * Action class for viewing and updating clients.
 * 
 * @author Reda Zerrad Date: 06.11.2012
 */
@Scope(ScopeType.CONVERSATION)
@Name("updateClientAction")
@Restrict("#{identity.loggedIn}")
public class UpdateClientAction implements Serializable {

	/**
     *
     */
	private static final long serialVersionUID = -5756470620039988876L;

	@Logger
	private Log log;

	private String inum;

	private boolean update;

	private OxAuthClient client;

	private List<String> uris;

	private List<String> newURLS;

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

	private String availableURI = "http://";

	private List<OxAuthScope> availableScopes;
	private List<GluuGroup> availableGroups;
	private List<SelectableEntity<ResponseType>> availableResponseTypes;

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

	@Restrict("#{s:hasPermission('client', 'access')}")
	public String add() throws Exception {
		if (this.client != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.update = false;
		this.client = new OxAuthClient();

		try {
			if (this.client.getOxAuthRedirectURIs() != null && this.client.getOxAuthRedirectURIs().size() > 0) {
				this.uris = this.client.getOxAuthRedirectURIs();
			} else {
				this.uris = new ArrayList<String>();
			}

			if (this.client.getOxAuthScopes() != null && this.client.getOxAuthScopes().size() > 0) {
				this.scopes = getScopeDisplayNameEntiries();
			} else {
				this.scopes = new ArrayList<DisplayNameEntry>();
			}

			if (this.client.getOxAuthClientUserGroups() != null && this.client.getOxAuthClientUserGroups().size() > 0) {
				this.groups = getGroupDisplayNameEntiries();
			} else {
				this.groups = new ArrayList<DisplayNameEntry>();
			}
			this.responseTypes = new ArrayList<ResponseType>();
		} catch (LdapMappingException ex) {
			log.error("Failed to load uris or scopes", ex);

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
			log.info("client is null ");
			return OxTrustConstants.RESULT_FAILURE;
		}

		try {
			if (client.getOxAuthRedirectURIs() != null && client.getOxAuthRedirectURIs().size() > 0) {
				this.uris = client.getOxAuthRedirectURIs();
			} else {
				this.uris = new ArrayList<String>();
			}
			if (client.getOxAuthScopes() != null && client.getOxAuthScopes().size() > 0) {
				this.scopes = getScopeDisplayNameEntiries();
			} else {
				this.scopes = new ArrayList<DisplayNameEntry>();
			}

			this.responseTypes = getInitialResponseTypes();

			if (client.getOxAuthClientUserGroups() != null && client.getOxAuthClientUserGroups().size() > 0) {
				this.groups = getGroupDisplayNameEntiries();
			} else {
				this.groups = new ArrayList<DisplayNameEntry>();
			}
		} catch (LdapMappingException ex) {
			log.error("Failed to load redirectURIS or scopes", ex);
			return OxTrustConstants.RESULT_FAILURE;
		}
		log.info("returning Success");
		return OxTrustConstants.RESULT_SUCCESS;
	}

	@Restrict("#{s:hasPermission('client', 'access')}")
	public void cancel() {
	}

	@Restrict("#{s:hasPermission('client', 'access')}")
	public String save() throws Exception {
		// List<String> oldURIs = null;
		// List<String> oldScopes = null;
		// List<String> oldGroups = null;
		// try {
		// oldURIs = client.getOxAuthRedirectURIs();
		// oldScopes = client.getOxAuthScopes();
		// oldGroups = client.getOxAuthClientUserGroups();
		// } catch (LdapMappingException ex) {
		// log.info("error getting oldURIs, oldScopes or oldGroups");
		// log.error("Failed to load redirectUris oldScopes or oldGroups", ex);
		// log.error(ex);
		// log.error(ex.getStackTrace());
		// log.error(ex.getCause());
		// facesMessages.add(Severity.ERROR, "Failed to update client");
		// return Configuration.RESULT_FAILURE;
		// }

		updateURIs();
		updateScopes();
		updateResponseTypes();
		updateGroups();
		if (update) {
			// Update client
			try {
				clientService.updateClient(this.client);
			} catch (LdapMappingException ex) {

				log.info("error updating group ", ex);
				log.error("Failed to update group {0}", ex, this.inum);

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
				log.error("Failed to add new client {0}", ex, this.client.getInum());

				facesMessages.add(Severity.ERROR, "Failed to add new client");
				return OxTrustConstants.RESULT_FAILURE;
			}

			this.update = true;
		}
		log.info(" returning success updating or saving client");
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
				log.error("Failed to remove client {0}", ex, this.client.getInum());
			}
		}

		return OxTrustConstants.RESULT_FAILURE;
	}

	public void addURI(String URI) {
		this.uris.add(URI);
	}

	public void removeURI(String URI) {

		List<String> tmpUris = new ArrayList<String>();
		for (String tmpUri : uris) {
			if (StringUtils.isNotBlank(tmpUri)) {
				tmpUris.add(tmpUri);
			}
		}
		for (Iterator<String> iterator = tmpUris.iterator(); iterator.hasNext();) {
			String oneURI = iterator.next();
			if (URI.equals(oneURI)) {
				iterator.remove();
				break;
			}
		}
		this.uris = tmpUris;
	}

	public void addGroup(GluuGroup group) {
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

	public void addScope(OxAuthScope scope) {
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

	public String getSearchAvailableScopePattern() {
		return this.searchAvailableScopePattern;
	}

	public void setSearchAvailableScopePattern(String searchAvailableScopePattern) {
		this.searchAvailableScopePattern = searchAvailableScopePattern;
	}

	public void acceptSelectScopes() {
		log.trace("Checking if availableScope == null");
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
		/*
		 * log.info("adding scopes to addesScopesInums"); Set<String>
		 * addedScopesInums = new HashSet<String>(); for (String scope : scopes)
		 * { if(scope != null && scope.length()>0 && scope !=
		 * ""){addedScopesInums.add(scope);} }
		 * 
		 * 
		 * 
		 * for (oxAuthScope oneScope : this.availableScopes) { if
		 * (oneScope.isSelected() &&
		 * !addedScopesInums.contains(oneScope.getDisplayName())) {
		 * log.info("adding scopes :", oneScope.getDisplayName()); if(oneScope
		 * != null && oneScope.getDisplayName().length()>0 &&
		 * oneScope.getDisplayName() !=
		 * ""){addScope(oneScope.getDisplayName());} } }
		 */
	}

	public void acceptSelectGroups() {
		log.trace("Checking if availableGroups == null");
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

	public void acceptSelectURI() {
		try {
			log.info("checking if availableURI == null");
			if (this.availableURI == null) {
				return;
			}

			log.info("availableURL != null == ", this.availableURI);
			log.info("adding availableURI");
			List<String> tmpURIS = new ArrayList<String>();
			for (String URI : this.uris) {
				if (StringUtils.isNotBlank(URI)) {
					tmpURIS.add(URI);
				}
			}
			tmpURIS.add(this.availableURI);
			this.uris = tmpURIS;
			log.info("availableURI added");
		} catch (Exception ex) {
			log.error(ex);

			// return;
		} finally {
			this.availableURI = "http://";
		}
	}

	public void cancelSelectURI() {
	}

	public void cancelSelectScopes() {
	}

	public void cancelSelectGroups() {
	}

	private void updateURIs() {

		if (this.uris == null || this.uris.size() < 1) {
			this.client.setOxAuthRedirectURIs(null);
			return;
		}
		List<String> TMPuris = new ArrayList<String>();
		this.client.setOxAuthRedirectURIs(TMPuris);

		for (String uri : this.uris) {
			TMPuris.add(uri);
		}
		this.client.setOxAuthRedirectURIs(TMPuris);

	}

	private void updateScopes() {

		if (this.scopes == null || this.scopes.size() < 1) {
			this.client.setOxAuthScopes(null);
			return;
		}

		List<String> TMPscopes = new ArrayList<String>();
		this.client.setOxAuthScopes(TMPscopes);

		for (DisplayNameEntry scope : this.scopes) {
			TMPscopes.add(scope.getDn());
		}
		this.client.setOxAuthScopes(TMPscopes);

	}

	private void updateGroups() {

		if (this.groups == null || this.groups.size() < 1) {
			this.client.setOxAuthClientUserGroups(null);
			return;
		}

		List<String> TMPgroups = new ArrayList<String>();
		this.client.setOxAuthClientUserGroups(TMPgroups);

		for (DisplayNameEntry group : this.groups) {
			TMPgroups.add(group.getDn());
		}
		this.client.setOxAuthClientUserGroups(TMPgroups);

	}

	public void selectAddedURIs() {
		// if (this.availableURI == null){
		// return;
		// }
	}

	public void selectAddedScopes() {
		if (this.availableScopes == null) {
			return;
		}

		Set<String> addedScopeInums = new HashSet<String>();

		for (DisplayNameEntry scope : scopes) {
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

		for (DisplayNameEntry group : groups) {
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

	private List<DisplayNameEntry> getScopeDisplayNameEntiries() throws Exception {
		List<DisplayNameEntry> result = new ArrayList<DisplayNameEntry>();
		List<DisplayNameEntry> tmp = lookupService.getDisplayNameEntries(scopeService.getDnForScope(null), this.client.getOxAuthScopes());
		if (tmp != null) {
			result.addAll(tmp);
		}

		return result;
	}

	private List<DisplayNameEntry> getGroupDisplayNameEntiries() throws Exception {
		List<DisplayNameEntry> result = new ArrayList<DisplayNameEntry>();
		List<DisplayNameEntry> tmp = lookupService.getDisplayNameEntries(groupService.getDnForGroup(null),
				this.client.getOxAuthClientUserGroups());
		if (tmp != null) {
			result.addAll(tmp);
		}

		return result;
	}

	public List<OxAuthScope> getAvailableScopes() {
		return this.availableScopes;
	}

	public void setAvailableScopes(List<OxAuthScope> availableScopes) {
		this.availableScopes = availableScopes;
	}

	public String getAvailableURI() {
		return this.availableURI;
	}

	public void setAvailableURI(String availableURI) {
		this.availableURI = availableURI;
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

	public void setClient(OxAuthClient client) {
		this.client = client;
	}

	public List<String> getUris() {
		return uris;
	}

	public void getUris(List<String> uris) {
		this.uris = uris;
	}

	public boolean isUpdate() {
		return update;
	}

	public List<String> getNewURLS() {
		return this.newURLS;
	}

	public void setNewURLS(List<String> newURLS) {
		this.newURLS = newURLS;
	}

	public List<DisplayNameEntry> getScopes() {
		return this.scopes;
	}

	public void setScopes(List<DisplayNameEntry> scopes) {
		this.scopes = scopes;
	}

	public List<DisplayNameEntry> getGroups() {
		return this.groups;
	}

	public void setGroups(List<DisplayNameEntry> groups) {
		this.groups = groups;
	}

	public List<GluuGroup> getAvailableGroups() {
		return this.availableGroups;
	}

	public void setAvailableGroup(List<GluuGroup> availableGroups) {
		this.availableGroups = availableGroups;
	}

	public String getSearchAvailableGroupPattern() {
		return this.searchAvailableGroupPattern;
	}

	public void setSearchAvailableGroupPattern(String searchAvailableGroupPattern) {
		this.searchAvailableGroupPattern = searchAvailableGroupPattern;
	}

	public String getOldSearchAvailableGroupPattern() {
		return this.oldSearchAvailableGroupPattern;
	}

	public void setOldSearchAvailableGroupPattern(String oldSearchAvailableGroupPattern) {
		this.oldSearchAvailableGroupPattern = oldSearchAvailableGroupPattern;
	}


	@Restrict("#{s:hasPermission('client', 'access')}")
	public void selectAddedResponseTypes() {
		List<ResponseType> addedResponseTypes = getAddedResponseTypes();

		for (SelectableEntity<ResponseType> availableResponseType : this.availableResponseTypes) {
			availableResponseType.setSelected(addedResponseTypes.contains(availableResponseType.getEntity()));
		}
	}

	@Restrict("#{s:hasPermission('client', 'access')}")
	public void acceptSelectResponseTypes() {
		List<ResponseType> addedResponseTypes = getAddedResponseTypes();

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

	private List<ResponseType> getAddedResponseTypes() {
		return this.responseTypes;
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

	private void updateResponseTypes() {
		List<ResponseType> currentResponseTypes = getAddedResponseTypes();

		if (currentResponseTypes.isEmpty()) {
			this.client.setResponseTypes(null);
			return;
		}

		this.client.setResponseTypes(currentResponseTypes.toArray(new ResponseType[currentResponseTypes.size()]));
	}

	private List<ResponseType> getInitialResponseTypes() {
		List<ResponseType> result = new ArrayList<ResponseType>();
		
		ResponseType[] currentResponseTypes = this.client.getResponseTypes();
		if ((currentResponseTypes != null) && (currentResponseTypes.length > 0)) {
			result.addAll(Arrays.asList(currentResponseTypes));
		}

		return result;
	}
	
	public void searchAvailableResponseTypes() {
		if (this.availableResponseTypes != null) {
			return;
		}

		List<SelectableEntity<ResponseType>> tmpAvailableResponseTypes = new ArrayList<SelectableEntity<ResponseType>>();
		
		for (ResponseType responseType : ResponseType.values()) {
			tmpAvailableResponseTypes.add(new SelectableEntity<ResponseType>(responseType));
		}
		
		this.availableResponseTypes = tmpAvailableResponseTypes;
		selectAddedResponseTypes();
	}

	public List<SelectableEntity<ResponseType>> getAvailableResponseTypes() {
		return this.availableResponseTypes;
	}

	public List<ResponseType> getResponseTypes() {
		return responseTypes;
	}

}
