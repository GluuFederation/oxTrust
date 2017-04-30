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

import org.gluu.oxtrust.ldap.service.ClientService;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.ScopeType;
import javax.inject.Inject;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;
import javax.enterprise.context.ConversationScoped;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.log.Log;
import org.xdi.util.Util;

/**
 * Action class for search clients
 * 
 * @author Reda Zerrad Date: 06.11.2012
 */
@Named("searchClientAction")
@ConversationScoped
@Restrict("#{identity.loggedIn}")
public class SearchClientAction implements Serializable {

	private static final long serialVersionUID = 8361095046179474395L;

	@Logger
	private Log log;

	@Inject
	StatusMessages statusMessages;

	@NotNull
	@Size(min = 0, max = 30, message = "Length of search string should be less than 30")
	private String searchPattern="";

	private String oldSearchPattern;

	private List<OxAuthClient> clientList;

	@Inject
	private ClientService clientService;

	@Restrict("#{s:hasPermission('client', 'access')}")
	public String start() {
		return search();
	}

	@Restrict("#{s:hasPermission('client', 'access')}")
	public String search() {
		if (Util.equals(this.oldSearchPattern, this.searchPattern)) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		try {
			if(searchPattern == null || searchPattern.isEmpty()){
				this.clientList = clientService.getAllClients(100);
			}else{
				this.clientList = clientService.searchClients(this.searchPattern, 100);
			}
			
			this.oldSearchPattern = this.searchPattern;
		} catch (Exception ex) {
			log.error("Failed to find clients", ex);
			return OxTrustConstants.RESULT_FAILURE;
		}

		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String getSearchPattern() {
		return searchPattern;
	}

	public void setSearchPattern(String searchPattern) {
		this.searchPattern = searchPattern;
	}

	public List<OxAuthClient> getClientList() {
		return clientList;
	}

}
