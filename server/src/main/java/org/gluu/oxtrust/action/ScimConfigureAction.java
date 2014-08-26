package org.gluu.oxtrust.action;

import java.io.Serializable;

import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

/**
 * Action class for configuring SCIM
 * 
 * @author Yuriy Movchan Date: 05.10.2012
 */
@Name("scimConfigureAction")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{identity.loggedIn}")
public class ScimConfigureAction implements Serializable {

	private static final long serialVersionUID = -1290460481895022469L;

	@Logger
	private Log log;

	@In
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
