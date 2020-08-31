/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

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
import org.gluu.oxtrust.service.ScopeService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.security.Secure;
import org.oxauth.persistence.model.Scope;
import org.slf4j.Logger;

/**
 * Action class for search scopes
 * 
 * @author Reda Zerrad Date: 06.18.2012
 */
@Named
@ConversationScoped
@Secure("#{permissionService.hasPermission('scope', 'access')}")
public class SearchScopeAction implements Serializable {

	private static final long serialVersionUID = -6633178742652918098L;

	@Inject
	private Logger log;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	@Inject
	private ScopeService scopeService;

	@NotNull
	@Size(min = 0, max = 30, message = "Length of search string should be less than 30")
	private String searchPattern = "";

	private List<Scope> scopeList = new ArrayList<>();

	public String start() {
		return search();
	}

	public String search() {
		try {
			this.scopeList = scopeService.searchScopes(this.searchPattern, 1000);
			this.searchPattern = "";
			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception ex) {
			log.error("Failed to find scopes", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to find scopes");
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}
	}

	public String getSearchPattern() {
		return searchPattern;
	}

	public void setSearchPattern(String searchPattern) {
		this.searchPattern = searchPattern;
	}

	public List<Scope> getScopeList() {
		return scopeList;
	}

}
