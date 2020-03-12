/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.service.ConfigurationService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.security.Secure;

/**
 * Action class for configuring SCIM
 * 
 * @author Yuriy Movchan Date: 05.10.2012
 */
@ConversationScoped
@Named
@Secure("#{permissionService.hasPermission('configuration', 'access')}")
public class ScimConfigureAction implements Serializable {

	private static final long serialVersionUID = -1290460481895022469L;
	
	@Inject
	private ConfigurationService configurationService;

	private boolean isInitialized = false;

	public String init() {
		if (isInitialized) {
			return OxTrustConstants.RESULT_SUCCESS;
		}
		if (configurationService.getConfiguration().isScimEnabled()) {
			return OxTrustConstants.RESULT_DISABLED;
		}
		this.isInitialized = true;
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String update() {
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public void cancel() {
	}

}
