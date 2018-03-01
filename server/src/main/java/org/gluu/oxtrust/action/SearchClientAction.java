/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.ldap.service.ClientService;
import org.gluu.oxtrust.model.OxAuthClient;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;
import org.xdi.service.security.Secure;
import org.xdi.util.Util;

/**
 * Action class for search clients
 * 
 * @author Reda Zerrad Date: 06.11.2012
 */
@Named
@ConversationScoped
@Secure("#{permissionService.hasPermission('client', 'access')}")
public class SearchClientAction implements Serializable {

	private static final long serialVersionUID = 8361095046179474395L;

	@Inject
	private Logger log;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	@NotNull
	@Size(min = 0, max = 30, message = "Length of search string should be less than 30")
	private String searchPattern="";

	private String oldSearchPattern;

	private List<OxAuthClient> clientList;
	
	private Map<OxAuthClient,Boolean> checkMap = new HashMap<OxAuthClient,Boolean>();
	public Map<OxAuthClient, Boolean> getCheckMap() {
		return checkMap;
	}

	@Inject
	private ClientService clientService;

	public String start() {
		return search();
	}

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

			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to find clients");
			conversationService.endConversation();

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

	public String deleteClients() {
		for(Entry<OxAuthClient,Boolean> entry : checkMap.entrySet()){
			if (entry.getValue()) {
				log.info( "result +  "+ entry.getKey());
				clientService.removeClient(entry.getKey());
				clientList.remove(entry.getKey());
			}			
		}
		checkMap.clear();
		
		return OxTrustConstants.RESULT_SUCCESS;
	}
}
