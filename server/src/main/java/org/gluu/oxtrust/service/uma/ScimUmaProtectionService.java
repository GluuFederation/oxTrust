/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.uma;

import java.io.Serializable;

import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.GluuAppliance;
import javax.enterprise.context.ApplicationScoped;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.enterprise.context.ConversationScoped;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.ldap.model.GluuBoolean;

/**
 * Provides service to protect SCIM UMA Rest service endpoints
 * 
 * @author Yuriy Movchan Date: 12/06/2016
 */
@ApplicationScoped
@Named("scimUmaProtectionService")
public class ScimUmaProtectionService extends BaseUmaProtectionService implements Serializable {

	private static final long serialVersionUID = -5447131971095468865L;

	@Inject
	private AppConfiguration appConfiguration;

	@Inject
	private ApplianceService applianceService;

	protected String getClientId() {
		return appConfiguration.getScimUmaClientId();
	}

	protected String getClientKeyStorePassword() {
		return appConfiguration.getScimUmaClientKeyStorePassword();
	}

	protected String getClientKeyStoreFile() {
		return appConfiguration.getScimUmaClientKeyStoreFile();
	}

	protected String getClientKeyId() {
		return appConfiguration.getScimUmaClientKeyId();
	}

	public String getUmaResourceId() {
		return appConfiguration.getScimUmaResourceId();
	}

	public String getUmaScope() {
		return appConfiguration.getScimUmaScope();
	}

	public boolean isEnabled() {
		return isScimEnabled() && isEnabledUmaAuthentication();
	}

	private boolean isScimEnabled() {
		GluuAppliance appliance = applianceService.getAppliance();
		GluuBoolean scimEnbaled = appliance.getScimEnabled();
		
		return GluuBoolean.ENABLED.equals(scimEnbaled) || GluuBoolean.TRUE.equals(scimEnbaled);
	}

}
