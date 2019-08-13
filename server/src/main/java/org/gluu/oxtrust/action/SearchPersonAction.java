/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.ldap.service.PersonService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.security.Secure;
import org.gluu.util.Util;
import org.slf4j.Logger;

/**
 * Action class for search persons
 * 
 * @author Yuriy Movchan Date: 10.22.2010
 */
@Named
@ConversationScoped
@Secure("#{permissionService.hasPermission('person', 'access')}")
public class SearchPersonAction implements Serializable {

	private static final long serialVersionUID = -4672682869487324438L;

	@Inject
	private Logger log;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	private String searchPattern;

	private String oldSearchPattern;

	private boolean firstLaunch = false;

	private List<GluuCustomPerson> personList;

	@Inject
	private PersonService personService;

	public String start() {
		firstLaunch = true;
		return search();
	}

	public String search() {
		if (!firstLaunch && (this.searchPattern.isEmpty() || this.searchPattern.length() < 2)) {
			firstLaunch = false;
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Length of search string should be between 2 and 30");
			return OxTrustConstants.RESULT_SUCCESS;
		}
		if (Util.equals(this.oldSearchPattern, this.searchPattern)) {
			firstLaunch = false;
			return OxTrustConstants.RESULT_SUCCESS;
		}
		try {
			this.personList = personService.searchPersons(this.searchPattern);
			this.oldSearchPattern = this.searchPattern;
			this.searchPattern = "";
			firstLaunch = false;
		} catch (Exception ex) {
			log.error("Failed to find persons", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to find persons");
			conversationService.endConversation();
			firstLaunch = false;
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

	public List<GluuCustomPerson> getPersonList() {
		return personList;
	}

}
