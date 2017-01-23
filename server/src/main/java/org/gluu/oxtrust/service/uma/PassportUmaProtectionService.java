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
 * Provides service to protect Passport Passport Rest service endpoints
 * 
 * @author Yuriy Movchan Date: 012/06/2016
 */
@Scope(ScopeType.APPLICATION)
@Name("pasportUmaProtectionService")
@AutoCreate
public class PassportUmaProtectionService extends BaseUmaProtectionService implements Serializable {

	private static final long serialVersionUID = -5547131971095468865L;

	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	@In
	private ApplianceService applianceService;

	protected String getClientId() {
		return applicationConfiguration.getPassportUmaClientId();
	}

	protected String getClientKeyStorePassword() {
		return applicationConfiguration.getPassportUmaClientKeyStorePassword();
	}

	protected String getClientKeyStoreFile() {
		return applicationConfiguration.getPassportUmaClientKeyStoreFile();
	}

	protected String getClientKeyId() {
		return applicationConfiguration.getPassportUmaClientKeyId();
	}

	public String getUmaResourceId() {
		return applicationConfiguration.getPassportUmaResourceId();
	}

	public String getUmaScope() {
		return applicationConfiguration.getPassportUmaScope();
	}

	public boolean isEnabled() {
		return isPassportEnabled() && isEnabledUmaAuthentication();
	}

	private boolean isPassportEnabled() {
		GluuAppliance appliance = applianceService.getAppliance();
		GluuBoolean passportEnbaled = appliance.getPassportEnabled();
		
		return GluuBoolean.ENABLED.equals(passportEnbaled) || GluuBoolean.TRUE.equals(passportEnbaled);
	}

}
