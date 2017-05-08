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

import javax.faces.application.FacesMessage;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;

/**
 * Action class for configuring SCIM
 * 
 * @author Yuriy Movchan Date: 05.10.2012
 */
@ConversationScoped
@Named
//TODO CDI @Restrict("#{identity.loggedIn}")
public class ScimConfigureAction implements Serializable {

	private static final long serialVersionUID = -1290460481895022469L;

	@Inject
	private Logger log;

	@Inject
	private ApplianceService applianceService;

	private boolean isInitialized = false;

	//TODO CDI @Restrict("#{s:hasPermission('configuration', 'access')}")
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

	//TODO CDI @Restrict("#{s:hasPermission('configuration', 'access')}")
	public String update() {
		return OxTrustConstants.RESULT_SUCCESS;
	}

	//TODO CDI @Restrict("#{s:hasPermission('configuration', 'access')}")
	public void cancel() {
	}

}
