/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action.push;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.oxtrust.model.push.PushApplication;
import org.gluu.oxtrust.service.push.PushApplicationConfigurationService;
import org.gluu.oxtrust.service.push.PushApplicationService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;
import org.xdi.util.Util;

/**
 * Action class for Push Application Inventory
 * 
 * @author Yuriy Movchan Date: 10/01/2014
 */
@Named("pushApplicationInventoryAction")
@ConversationScoped
//TODO CDI @Restrict("#{identity.loggedIn}")
public class PushApplicationInventoryAction implements Serializable {

	private static final long serialVersionUID = -2233178742652918022L;

	@Inject
	private Logger log;

	@NotNull
	@Size(min = 0, max = 30, message = "Length of search string should be less than 30")
	private String searchPattern;

	private String oldSearchPattern;

	private List<PushApplication> pushApplicationList;

	@Inject
	private PushApplicationService pushApplicationService;
	
	@Inject
	private PushApplicationConfigurationService pushApplicationConfigurationService;

	//TODO CDI @Restrict("#{s:hasPermission('oxpush', 'access')}")
	public String start() {
		return search();
	}

	//TODO CDI @Restrict("#{s:hasPermission('oxpush', 'access')}")
	public String search() {
		if (Util.equals(this.oldSearchPattern, this.searchPattern)) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		try {
			this.pushApplicationList = pushApplicationService.findPushApplications(this.searchPattern, 0);
			this.oldSearchPattern = this.searchPattern;

			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception ex) {
			log.error("Failed to find scopes", ex);
		}

		return OxTrustConstants.RESULT_FAILURE;
	}

	//TODO CDI @Restrict("#{s:hasPermission('oxpush', 'access')}")
	public List<String> getPlatforms(PushApplication pushApplication) {
		List<String> platforms = pushApplicationConfigurationService.getPlatformDescriptionList(pushApplication);
		
		return platforms;
	}

	public String getSearchPattern() {
		return searchPattern;
	}

	public void setSearchPattern(String searchPattern) {
		this.searchPattern = searchPattern;
	}

	public List<PushApplication> getPushApplicationList() {
		return pushApplicationList;
	}

}
