/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.Date;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.gluu.oxtrust.ldap.service.ConfigurationService;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.security.Secure;
import org.slf4j.Logger;

/**
 * Action class for health check display
 * 
 * @author Oleksiy Tataryn Date: 11.14.2013
 */
@RequestScoped
@Named("configurationStatusAction")
@Secure("#{permissionService.hasPermission('configuration', 'access')}")
public class ConfigurationStatusAction implements Serializable {

	private static final long serialVersionUID = -7470520478553992898L;

	@Inject
	private Logger log;

	@Inject
	private ConfigurationService configurationService;

	private String health;

	public String init() {
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String checkHealth() {
		GluuConfiguration configuration = configurationService.getConfiguration();
		Date lastUpdateDateTime = configuration.getLastUpdate();
		long lastUpdate = 0;
		if (lastUpdateDateTime != null) {
			lastUpdate = lastUpdateDateTime.getTime();
		}
		long currentTime = System.currentTimeMillis();
		log.debug("lastUpdate: '{}', currentTime: '{}'", lastUpdate, currentTime);
		long timeSinceLastUpdate = (currentTime - lastUpdate) / 1000;
		if (timeSinceLastUpdate >= 0 && timeSinceLastUpdate < 100) {
			this.setHealth("OK");
		} else {
			this.setHealth("FAIL");
		}
		log.debug("Set status '{}'", this.getHealth());
		return OxTrustConstants.RESULT_SUCCESS;
	}

	public String getHealth() {
		return health;
	}

	public void setHealth(String health) {
		this.health = health;
	}

	public String getHostName(String hostName) {
		if (hostName == null || StringUtils.isEmpty(hostName)) {
			ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
			hostName = context.getRequestServerName();
		}
		return hostName;
	}

}
