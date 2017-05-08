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

import javax.faces.application.FacesMessage;import javax.enterprise.context.ConversationScoped;
import org.xdi.config.oxtrust.AppConfiguration;
import org.xdi.ldap.model.GluuBoolean;

/**
 * Provides service to protect Passport Passport Rest service endpoints
 * 
 * @author Yuriy Movchan Date: 012/06/2016
 */
@ApplicationScoped
@Named("pasportUmaProtectionService")
public class PassportUmaProtectionService extends BaseUmaProtectionService implements Serializable {

	private static final long serialVersionUID = -5547131971095468865L;

	@Inject
	private AppConfiguration appConfiguration;

	@Inject
	private ApplianceService applianceService;

	protected String getClientId() {
		return appConfiguration.getPassportUmaClientId();
	}

	protected String getClientKeyStorePassword() {
		return appConfiguration.getPassportUmaClientKeyStorePassword();
	}

	protected String getClientKeyStoreFile() {
		return appConfiguration.getPassportUmaClientKeyStoreFile();
	}

	protected String getClientKeyId() {
		return appConfiguration.getPassportUmaClientKeyId();
	}

	public String getUmaResourceId() {
		return appConfiguration.getPassportUmaResourceId();
	}

	public String getUmaScope() {
		return appConfiguration.getPassportUmaScope();
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
