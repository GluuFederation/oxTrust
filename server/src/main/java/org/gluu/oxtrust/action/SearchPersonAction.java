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

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.oxtrust.service.PersonService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.model.PagedResult;
import org.gluu.service.security.Secure;
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

	private List<GluuCustomPerson> persons = new ArrayList<>();

	private PagedResult<GluuCustomPerson> results;

	private int count = 5000;
	private int start = 0;
	private int nbPages = 0;

	private int searchIndex = 1;

	@Inject
	private PersonService personService;

	public String start() {
		return search();
	}

	
	public String search() {
		try {
			start = 0;
			nbPages = 0;
			searchIndex = 1;
			results = personService.findPeople(this.searchPattern, start, count);
			persons = results.getEntries();
			nbPages = (int) Math.ceil(results.getTotalEntriesCount() / (double) count);
		} catch (Exception ex) {
			log.error("Failed to find persons", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to find persons");
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String getNextPage() {
		try {
			searchIndex++;
			start = start + count;
			results = personService.findPeople(this.searchPattern, start, count);
			persons.clear();
			persons = results.getEntries();

		} catch (Exception ex) {
			log.error("Failed to find persons", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to find persons");
			conversationService.endConversation();
			return OxTrustConstants.RESULT_FAILURE;
		}
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String getPreviousPage() {
		try {
			searchIndex--;
			start = start - count;
			results = personService.findPeople(this.searchPattern, start, count);
			persons.clear();
			persons = results.getEntries();

		} catch (Exception ex) {
			log.error("Failed to find persons", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to find persons");
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

	public List<GluuCustomPerson> getPersonList() {
		return persons;
	}

	public int getNbPages() {
		return nbPages;
	}

	public int getSearchIndex() {
		return searchIndex;
	}

	public boolean showNext() {
		return searchIndex < nbPages;
	}

	public boolean showPrev() {
		return searchIndex > 1;
	}

}
