/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.external;

import java.util.Map;

import org.gluu.oxtrust.model.GluuCustomPerson;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.xdi.model.SimpleCustomProperty;
import org.xdi.model.custom.script.CustomScriptType;
import org.xdi.model.custom.script.conf.CustomScriptConfiguration;
import org.xdi.model.custom.script.type.user.UserRegistrationType;
import org.xdi.service.custom.script.ExternalScriptService;

/**
 * Provides factory methods needed to create external user registration extension
 * 
 * @author Yuriy Movchan Date: 01/16/2015
 */
@Scope(ScopeType.APPLICATION)
@Name("externalUserRegistrationService")
@AutoCreate
@Startup
public class ExternalUserRegistrationService extends ExternalScriptService {

	private static final long serialVersionUID = 1767751544454591273L;

	public ExternalUserRegistrationService() {
		super(CustomScriptType.USER_REGISTRATION);
	}

	public boolean executeExternalInitRegistrationMethod(CustomScriptConfiguration customScriptConfiguration, GluuCustomPerson user, Map<String, String[]> requestParameters) {
		try {
			log.debug("Executing python 'initRegistration' method");
			UserRegistrationType externalType = (UserRegistrationType) customScriptConfiguration.getExternalType();
			Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();
			return externalType.initRegistration(user, requestParameters, configurationAttributes);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}

		return false;
	}

	public boolean executeExternalPreRegistrationMethod(CustomScriptConfiguration customScriptConfiguration, GluuCustomPerson user, Map<String, String[]> requestParameters) {
		try {
			log.debug("Executing python 'preRegistration' method");
			UserRegistrationType externalType = (UserRegistrationType) customScriptConfiguration.getExternalType();
			Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();
			return externalType.preRegistration(user, requestParameters, configurationAttributes);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}

		return false;
	}

	public boolean executeExternalPostRegistrationMethod(CustomScriptConfiguration customScriptConfiguration, GluuCustomPerson user, Map<String, String[]> requestParameters) {
		try {
			log.debug("Executing python 'postRegistration' method");
			UserRegistrationType externalType = (UserRegistrationType) customScriptConfiguration.getExternalType();
			Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();
			return externalType.postRegistration(user, requestParameters, configurationAttributes);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}

		return false;
	}

	public boolean executeExternalInitRegistrationMethods(GluuCustomPerson user, Map<String, String[]> requestParameters) {
		boolean result = true;
		for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
			result &= executeExternalInitRegistrationMethod(customScriptConfiguration, user, requestParameters);
			if (!result) {
				return result;
			}
		}

		return result;
	}

	public boolean executeExternalPreRegistrationMethods(GluuCustomPerson user, Map<String, String[]> requestParameters) {
		boolean result = true;
		for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
			result &= executeExternalPreRegistrationMethod(customScriptConfiguration, user, requestParameters);
			if (!result) {
				return result;
			}
		}

		return result;
	}

	public boolean executeExternalPostRegistrationMethods(GluuCustomPerson user, Map<String, String[]> requestParameters) {
		boolean result = true;
		for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
			result &= executeExternalPostRegistrationMethod(customScriptConfiguration, user, requestParameters);
			if (!result) {
				return result;
			}
		}

		return result;
	}

	public static ExternalUserRegistrationService instance() {
		return (ExternalUserRegistrationService) Component.getInstance(ExternalUserRegistrationService.class);
	}

}
