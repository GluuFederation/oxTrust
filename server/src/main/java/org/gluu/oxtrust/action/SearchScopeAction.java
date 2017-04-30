/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.oxtrust.ldap.service.ScopeService;
import org.gluu.oxtrust.model.OxAuthScope;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.ScopeType;
import javax.inject.Inject;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;
import javax.enterprise.context.ConversationScoped;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;
import org.xdi.util.Util;

/**
 * Action class for search scopes
 * 
 * @author Reda Zerrad Date: 06.18.2012
 */
@Named("searchScopeAction")
@ConversationScoped
@Restrict("#{identity.loggedIn}")
public class SearchScopeAction implements Serializable {

	private static final long serialVersionUID = -6633178742652918098L;

	@Logger
	private Log log;

	@NotNull
	@Size(min = 0, max = 30, message = "Length of search string should be less than 30")
	private String searchPattern=" ";

	private String oldSearchPattern;

	private List<OxAuthScope> scopeList;

	@Inject
	private ScopeService scopeService;

	@Restrict("#{s:hasPermission('scope', 'access')}")
	public String start() {
		return search();
	}

	@Restrict("#{s:hasPermission('scope', 'access')}")
	public String search() {
		if (Util.equals(this.oldSearchPattern, this.searchPattern)) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		try {
			this.scopeList = scopeService.searchScopes(this.searchPattern, 100);
			this.oldSearchPattern = this.searchPattern;

			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception ex) {
			log.error("Failed to find scopes", ex);
		}

		return OxTrustConstants.RESULT_FAILURE;
	}

	public String getSearchPattern() {
		return searchPattern;
	}

	public void setSearchPattern(String searchPattern) {
		this.searchPattern = searchPattern;
	}

	public List<OxAuthScope> getScopeList() {
		return scopeList;
	}

}
