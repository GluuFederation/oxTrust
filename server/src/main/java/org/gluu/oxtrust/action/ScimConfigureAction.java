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

import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;
import org.xdi.service.security.Secure;

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
	private Logger log;

	@Inject
	private ApplianceService applianceService;

	private boolean isInitialized = false;

	public String init() {
		if (isInitialized) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		GluuAppliance appliance = applianceService.getAppliance();
		if ((appliance.getScimEnabled() == null) || !appliance.getScimEnabled().isBooleanValue()) {
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
