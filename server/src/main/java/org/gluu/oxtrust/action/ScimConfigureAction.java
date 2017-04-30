/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;

import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.ScopeType;
import javax.inject.Inject;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;
import javax.enterprise.context.ConversationScoped;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

/**
 * Action class for configuring SCIM
 * 
 * @author Yuriy Movchan Date: 05.10.2012
 */
@Named("scimConfigureAction")
@ConversationScoped
@Restrict("#{identity.loggedIn}")
public class ScimConfigureAction implements Serializable {

	private static final long serialVersionUID = -1290460481895022469L;

	@Logger
	private Log log;

	@Inject
	private ApplianceService applianceService;

	private boolean isInitialized = false;

	@Restrict("#{s:hasPermission('configuration', 'access')}")
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

	@Restrict("#{s:hasPermission('configuration', 'access')}")
	public String update() {
		return OxTrustConstants.RESULT_SUCCESS;
	}

	@Restrict("#{s:hasPermission('configuration', 'access')}")
	public void cancel() {
	}

}
