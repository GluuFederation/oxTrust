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
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.oxtrust.ldap.service.ClientService;
import org.gluu.oxtrust.ldap.service.ImageService;
import org.gluu.oxtrust.ldap.service.uma.ResourceSetService;
import org.gluu.oxtrust.ldap.service.uma.ScopeDescriptionService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;
import org.xdi.model.DisplayNameEntry;
import org.xdi.oxauth.model.uma.persistence.ResourceSet;
import org.xdi.oxauth.model.uma.persistence.ScopeDescription;
import org.xdi.service.LookupService;
import org.xdi.util.StringHelper;
import org.xdi.util.Util;

/**
 * Action class for UMA inventory
 * 
 * @author Yuriy Movchan Date: 04/24/2013
 */
@Named("umaInventoryAction")
@ConversationScoped
//TODO CDI @Restrict("#{identity.loggedIn}")
public class UmaInventoryAction implements Serializable {

	private static final long serialVersionUID = 2261095046179474395L;

	@Inject
	private Logger log;

	@Inject
	private ResourceSetService resourceSetService;

	@Inject
	private ClientService clientService;

	@Inject
	private ScopeDescriptionService scopeDescriptionService;

	@Inject
	protected ImageService imageService;

	@Inject
	private LookupService lookupService;

	@NotNull
	@Size(min = 0, max = 30, message = "Length of search string should be less than 30")
	private String searchPattern;

	private String oldSearchPattern;

	private List<ResourceSet> resourcesList;
	private List<ScopeDescription> scopesList;
	
	private boolean initialized;

	//TODO CDI @Restrict("#{s:hasPermission('uma', 'access')}")
	public String start() {
		try {
			resourceSetService.prepareResourceSetBranch();
		} catch (Exception ex) {
			log.error("Failed to initialize form", ex);
			return OxTrustConstants.RESULT_FAILURE;
		}
		
		this.initialized = true;

		if (StringHelper.isEmpty(this.searchPattern)) {
			searchPattern = "";
		}

		search();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	//TODO CDI @Restrict("#{s:hasPermission('uma', 'access')}")
	public String search() {
		if (Util.equals(this.oldSearchPattern, this.searchPattern)) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		try {
			if(searchPattern == null || searchPattern.isEmpty()){
				this.scopesList = scopeDescriptionService.getAllScopeDescriptions(100);
				this.resourcesList = resourceSetService.getAllResourceSets(100);
			}else{
				this.scopesList = scopeDescriptionService.findScopeDescriptions(this.searchPattern, 100);
				this.resourcesList = resourceSetService.findResourceSets(this.searchPattern, 100);
			}
			
			this.oldSearchPattern = this.searchPattern;
		} catch (Exception ex) {
			log.error("Failed to find resource sets", ex);

			return OxTrustConstants.RESULT_FAILURE;
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public List<DisplayNameEntry> getScopeDisplayNameEntries(List<String> scopeDns) {
		List<DisplayNameEntry> result = new ArrayList<DisplayNameEntry>();
		List<DisplayNameEntry> tmp = lookupService.getDisplayNameEntries(scopeDescriptionService.getDnForScopeDescription(null), scopeDns);
		if (tmp != null) {
			result.addAll(tmp);
		}

		return result;
	}

	public List<DisplayNameEntry> getClientDisplayNameEntries(List<String> clientDns) {
		List<DisplayNameEntry> result = new ArrayList<DisplayNameEntry>();
		List<DisplayNameEntry> tmp = lookupService.getDisplayNameEntries(clientService.getDnForClient(null), clientDns);
		if (tmp != null) {
			result.addAll(tmp);
		}

		return result;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public List<ResourceSet> getResourcesList() {
		return resourcesList;
	}

	public List<ScopeDescription> getScopesList() {
		return scopesList;
	}

	public String getSearchPattern() {
		return searchPattern;
	}

	public void setSearchPattern(String searchPattern) {
		this.searchPattern = searchPattern;
	}

}
