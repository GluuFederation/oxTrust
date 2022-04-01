/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action.uma;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.model.DisplayNameEntry;
import org.gluu.oxauth.model.uma.UmaMetadata;
import org.gluu.oxauth.model.uma.persistence.UmaResource;
import org.gluu.oxtrust.service.ClientService;
import org.gluu.oxtrust.service.ImageService;
import org.gluu.oxtrust.service.uma.ResourceSetService;
import org.gluu.oxtrust.service.uma.UmaScopeService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.annotation.ObjectClass;
import org.gluu.service.LookupService;
import org.gluu.service.security.Secure;
import org.gluu.util.StringHelper;
import org.gluu.util.Util;
import org.oxauth.persistence.model.Scope;
import org.slf4j.Logger;

/**
 * Action class for UMA inventory
 * 
 * @author Yuriy Movchan Date: 04/24/2013
 */
@ConversationScoped
@Named
@Secure("#{permissionService.hasPermission('uma', 'access')}")
public class UmaInventoryAction implements Serializable {

	private static final long serialVersionUID = 2261095046179474395L;

	@Inject
	private Logger log;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	@Inject
	private ResourceSetService umaResourcesService;

	@Inject
	private ClientService clientService;

	@Inject
	private UmaScopeService umaScopeService;

	@Inject
	protected ImageService imageService;

	@Inject
	private LookupService lookupService;

	@Inject
	private UmaMetadata umaMetadata;

	@NotNull
	@Size(min = 0, max = 30, message = "Length of search string should be less than 30")
	private String searchPattern;

	private String oldSearchPattern;

	private List<UmaResource> resourcesList;
	private List<Scope> scopesList;

	private boolean initialized;

	public String start() {
		try {
			umaResourcesService.prepareResourceBranch();
		} catch (Exception ex) {
			log.error("Failed to initialize form", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to initialize UMA inventory");
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}
		this.initialized = true;
		if (StringHelper.isEmpty(this.searchPattern)) {
			searchPattern = "";
		}
		search();
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String search() {
		if (Util.equals(this.oldSearchPattern, this.searchPattern)) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		try {
			if (searchPattern == null || searchPattern.isEmpty()) {
				this.scopesList = umaScopeService.getAllUmaScopes(1000);
				this.resourcesList = umaResourcesService.getAllResources(1000);
			} else {
				this.scopesList = umaScopeService.findUmaScopes(this.searchPattern, 1000);
				this.resourcesList = umaResourcesService.findResources(this.searchPattern, 1000);
			}
			this.oldSearchPattern = this.searchPattern;
		} catch (Exception ex) {
			log.error("Failed to find resource sets", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR,
					"Failed to filter UMA inventory by '#{umaInventoryAction.searchPattern}'");
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public List<DisplayNameEntry> getScopeDisplayNameEntries(UmaResource resource) {
		List<String> scopeDns = resource.getScopes();
		List<DisplayNameEntry> result = new ArrayList<DisplayNameEntry>();
		List<ScopeDisplayNameEntry> tmp = lookupService
				.getDisplayNameEntries(umaScopeService.getDnForScope(null),
						ScopeDisplayNameEntry.class, scopeDns);
		if (tmp != null) {
			result.addAll(tmp);
		}
		return result;
	}

	public List<String> getScopes(UmaResource resource) {
		List<String> result = new ArrayList<>();
		List<String> scopeDns = resource.getScopes();
		if (scopeDns != null) {
			for (String dn : scopeDns) {
				Scope res = umaScopeService.getScopeByDn(dn);
				if (res != null) {
					result.add(res.getDisplayName());
				}
			}
		}
		return result;
	}

	public List<DisplayNameEntry> getClientDisplayNameEntries(List<String> clientDns) {
		List<DisplayNameEntry> result = new ArrayList<DisplayNameEntry>();
		List<ClientDisplayNameEntry> tmp = lookupService.getDisplayNameEntries(clientService.getDnForClient(null),
				ClientDisplayNameEntry.class, clientDns);
		if (tmp != null) {
			result.addAll(tmp);
		}

		return result;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public List<UmaResource> getResourcesList() {
		return resourcesList;
	}

	public List<Scope> getScopesList() {
		return scopesList;
	}

	public String getSearchPattern() {
		return searchPattern;
	}

	public void setSearchPattern(String searchPattern) {
		this.searchPattern = searchPattern;
	}

	public UmaMetadata getUmaMetadata() {
		return umaMetadata;
	}

	@ObjectClass(value = "oxAuthCustomScope")
	class ScopeDisplayNameEntry extends DisplayNameEntry {
		public ScopeDisplayNameEntry() {}
	}

	@ObjectClass(value = "oxAuthClient")
	class ClientDisplayNameEntry extends DisplayNameEntry {
		public ClientDisplayNameEntry() {}
	}

}
