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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.service.GroupService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.security.Secure;
import org.gluu.util.Util;
import org.slf4j.Logger;

/**
 * Action class for search groups
 * 
 * @author Yuriy Movchan Date: 11.02.2010
 */
@Named
@ConversationScoped
@Secure("#{permissionService.hasPermission('group', 'access')}")
public class SearchGroupAction implements Serializable {

	private static final long serialVersionUID = -5270460481895022468L;

	@Inject
	private Logger log;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	@NotNull
	@Size(min = 0, max = 30, message = "Length of search string should be between 0 and 30")
	private String searchPattern;

	private String oldSearchPattern;

	private List<GluuGroup> groupList;

	@Inject
	private GroupService groupService;

	public String start() {
		return search();
	}

	public String search() {
		if ((this.searchPattern != null) && Util.equals(this.oldSearchPattern, this.searchPattern)) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		try {
			if (searchPattern == null || searchPattern.isEmpty()) {
				this.groupList = groupService.getAllGroups(OxTrustConstants.searchGroupSizeLimit);
			} else {
				this.groupList = groupService.searchGroups(this.searchPattern, OxTrustConstants.searchGroupSizeLimit);
			}
			this.groupList.sort(Comparator.comparing(GluuGroup::getDisplayName));
			log.debug("Found '{}' groups.", this.groupList.size());
			this.oldSearchPattern = this.searchPattern;
			this.searchPattern = "";
		} catch (Exception ex) {
			log.error("Failed to find groups", ex);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to find groups");
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

	public List<GluuGroup> getGroupList() {
		return groupList;
	}

}
