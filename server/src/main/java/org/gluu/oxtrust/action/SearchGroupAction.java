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

import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.log.Log;
import org.xdi.util.Util;

/**
 * Action class for search groups
 * 
 * @author Yuriy Movchan Date: 11.02.2010
 */
@Name("searchGroupAction")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{identity.loggedIn}")
public class SearchGroupAction implements Serializable {

	private static final long serialVersionUID = -5270460481895022468L;

	@Logger
	private Log log;

	@In
	StatusMessages statusMessages;

	@NotNull
	@Size(min = 0, max = 30, message = "Length of search string should be between 0 and 30")
	private String searchPattern;

	private String oldSearchPattern;

	private List<GluuGroup> groupList;

	@In
	private IGroupService groupService;

	@Restrict("#{s:hasPermission('group', 'access')}")
	public String start() {
		return search();
	}

	@Restrict("#{s:hasPermission('group', 'access')}")
	public String search() {
		if ((this.searchPattern != null) && Util.equals(this.oldSearchPattern, this.searchPattern)) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		try {
			if(searchPattern == null || searchPattern.isEmpty()){
				this.groupList = groupService.getAllGroups(OxTrustConstants.searchGroupSizeLimit);
			}else{
				this.groupList = groupService.searchGroups(this.searchPattern, OxTrustConstants.searchGroupSizeLimit);
			}
			
			log.debug("Found \"" + this.groupList.size() + "\" groups.");
			this.oldSearchPattern = this.searchPattern;
		} catch (Exception ex) {
			log.error("Failed to find groups", ex);
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
