/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action.push;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.model.push.PushDevice;
import org.gluu.oxtrust.service.push.PushDeviceService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.security.Secure;
import org.gluu.util.Util;
import org.slf4j.Logger;

/**
 * Action class for Push Device Inventory
 * 
 * @author Yuriy Movchan Date: 02/03/2014
 */
@Named("pushDeviceInventoryAction")
@ConversationScoped
@Secure("#{permissionService.hasPermission('super-gluu', 'access')}")
public class PushDeviceInventoryAction implements Serializable {

	private static final long serialVersionUID = 6613070802638642079L;

	@Inject
	private Logger log;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	@NotNull
	@Size(min = 0, max = 30, message = "Length of search string should be less than 30")
	private String searchPattern;

	private String oldSearchPattern;

	private List<PushDevice> pushDeviceList;

	@Inject
	private PushDeviceService pushDeviceService;
	
	public String start() {
		return search();
	}

	public String search() {
		if (Util.equals(this.oldSearchPattern, this.searchPattern)) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		try {
			this.pushDeviceList = pushDeviceService.findPushDevices(this.searchPattern, 0);
			this.oldSearchPattern = this.searchPattern;

			return OxTrustConstants.RESULT_SUCCESS;
		} catch (Exception ex) {
			log.error("Failed to find scopes", ex);
		}

		facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to find Super-Gluu devices");
		conversationService.endConversation();

		return OxTrustConstants.RESULT_FAILURE;
	}

	public String getSearchPattern() {
		return searchPattern;
	}

	public void setSearchPattern(String searchPattern) {
		this.searchPattern = searchPattern;
	}

	public List<PushDevice> getPushDeviceList() {
		return pushDeviceList;
	}

}
