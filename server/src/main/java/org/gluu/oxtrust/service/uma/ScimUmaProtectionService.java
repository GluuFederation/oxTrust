/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.uma;

import java.io.Serializable;

import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.ldap.model.GluuBoolean;

/**
 * Provides service to protect SCIM UMA Rest service endpoints
 * 
 * @author Yuriy Movchan Date: 12/06/2016
 */
@Scope(ScopeType.APPLICATION)
@Name("scimUmaProtectionService")
@AutoCreate
public class ScimUmaProtectionService extends BaseUmaProtectionService implements Serializable {

	private static final long serialVersionUID = -5447131971095468865L;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	@In
	private ApplianceService applianceService;

	protected String getClientId() {
		return applicationConfiguration.getScimUmaClientId();
	}

	protected String getClientKeyStorePassword() {
		return applicationConfiguration.getScimUmaClientKeyStorePassword();
	}

	protected String getClientKeyStoreFile() {
		return applicationConfiguration.getScimUmaClientKeyStoreFile();
	}

	protected String getClientKeyId() {
		return applicationConfiguration.getScimUmaClientKeyId();
	}

	public String getUmaResourceId() {
		return applicationConfiguration.getScimUmaResourceId();
	}

	public String getUmaScope() {
		return applicationConfiguration.getScimUmaScope();
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
