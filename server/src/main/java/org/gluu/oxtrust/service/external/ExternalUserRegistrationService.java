/*
 * oxAuth is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
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
 * Provides factory methods needed to create external dynamic client registration extension
 *
 * @author Yuriy Movchan Date: 01/08/2015
 */
@Scope(ScopeType.APPLICATION)
@Name("externalUserRegistrationService")
@AutoCreate
@Startup
public class ExternalUserRegistrationService extends ExternalScriptService {

	private static final long serialVersionUID = 1416361273036208685L;

	public ExternalUserRegistrationService() {
		super(CustomScriptType.USER_REGISTRATION);
	}

	public boolean executeExternalUserRegistrationUpdateMethod(CustomScriptConfiguration customScriptConfiguration, GluuCustomPerson user, boolean persisted) {
		try {
			log.debug("Executing python 'updateUser' method");
			UserRegistrationType externalType = (UserRegistrationType) customScriptConfiguration.getExternalType();
			Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();
			return externalType.updateUser(user, persisted, configurationAttributes);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
		return false;
	}

	public boolean executeExternalDefaultUserRegistrationUpdateMethod(GluuCustomPerson user, boolean persisted) {
		return executeExternalUserRegistrationUpdateMethod(this.defaultExternalCustomScript, user, persisted);
	}

	public boolean executeExternalAddPersonMethods(GluuCustomPerson user) {
		boolean result = true;
		for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
			result &= executeExternalUserRegistrationUpdateMethod(customScriptConfiguration, user, false);
			if (!result) {
				return result;
			}
		}

		return result;
	}

	public boolean executeExternalUpdatePersonMethods(GluuCustomPerson user) {
		boolean result = true;
		for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
			result &= executeExternalUserRegistrationUpdateMethod(customScriptConfiguration, user, true);
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
