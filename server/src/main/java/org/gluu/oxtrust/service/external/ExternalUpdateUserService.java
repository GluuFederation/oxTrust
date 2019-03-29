/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service.external;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.gluu.model.SimpleCustomProperty;
import org.gluu.model.custom.script.CustomScriptType;
import org.gluu.model.custom.script.conf.CustomScriptConfiguration;
import org.gluu.model.custom.script.type.user.UpdateUserType;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.service.custom.script.ExternalScriptService;

/**
 * Provides factory methods needed to create external dynamic client registration extension
 *
 * @author Yuriy Movchan Date: 01/08/2015
 */
@ApplicationScoped
@Named
public class ExternalUpdateUserService extends ExternalScriptService {

	private static final long serialVersionUID = 1416361273036208685L;

	public ExternalUpdateUserService() {
		super(CustomScriptType.UPDATE_USER);
	}
	
	public boolean executeExternalAddUserMethod(CustomScriptConfiguration customScriptConfiguration, GluuCustomPerson user, boolean persisted) {
		try {
			log.debug("Executing python 'addUser' method");
			UpdateUserType externalType = (UpdateUserType) customScriptConfiguration.getExternalType();
			Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();
			return externalType.addUser(user, persisted, configurationAttributes);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
            saveScriptError(customScriptConfiguration.getCustomScript(), ex);
		}
		
		return false;
	}

	public boolean executeExternalAddUserMethods(GluuCustomPerson user) {
		boolean result = true;
		for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
			result &= executeExternalAddUserMethod(customScriptConfiguration, user, true);
			if (!result) {
				return result;
			}
		}

		return result;
	}
	
	public boolean executeExternalPostAddUserMethod(CustomScriptConfiguration customScriptConfiguration, GluuCustomPerson user) {
        int apiVersion = executeExternalGetApiVersion(customScriptConfiguration);
        if (apiVersion < 2) {
        	return true;
        }

		try {
			log.debug("Executing python 'postAddUser' method");
			UpdateUserType externalType = (UpdateUserType) customScriptConfiguration.getExternalType();
			Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();
			return externalType.postAddUser(user, configurationAttributes);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
            saveScriptError(customScriptConfiguration.getCustomScript(), ex);
		}
		
		return false;
	}

	public boolean executeExternalPostAddUserMethods(GluuCustomPerson user) {
		boolean result = true;
		for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
			result &= executeExternalPostAddUserMethod(customScriptConfiguration, user);
			if (!result) {
				return result;
			}
		}

		return result;
	}

	public boolean executeExternalUpdateUserMethod(CustomScriptConfiguration customScriptConfiguration, GluuCustomPerson user, boolean persisted) {
		try {
			log.debug("Executing python 'updateUser' method");
			UpdateUserType externalType = (UpdateUserType) customScriptConfiguration.getExternalType();
			Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();
			return externalType.updateUser(user, persisted, configurationAttributes);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
            saveScriptError(customScriptConfiguration.getCustomScript(), ex);
		}
		
		return false;
	}

	public boolean executeExternalUpdateUserMethods(GluuCustomPerson user) {
		boolean result = true;
		for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
			result &= executeExternalUpdateUserMethod(customScriptConfiguration, user, true);
			if (!result) {
				return result;
			}
		}

		return result;
	}

	public boolean executeExternalPostUpdateUserMethod(CustomScriptConfiguration customScriptConfiguration, GluuCustomPerson user) {
        int apiVersion = executeExternalGetApiVersion(customScriptConfiguration);
        if (apiVersion < 2) {
        	return true;
        }

		try {
			log.debug("Executing python 'postUpdateUser' method");
			UpdateUserType externalType = (UpdateUserType) customScriptConfiguration.getExternalType();
			Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();
			return externalType.postUpdateUser(user, configurationAttributes);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
            saveScriptError(customScriptConfiguration.getCustomScript(), ex);
		}
		
		return false;
	}

	public boolean executeExternalPostUpdateUserMethods(GluuCustomPerson user) {
		boolean result = true;
		for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
			result &= executeExternalPostUpdateUserMethod(customScriptConfiguration, user);
			if (!result) {
				return result;
			}
		}

		return result;
	}
	
	public boolean executeExternalNewUserMethod(CustomScriptConfiguration customScriptConfiguration, GluuCustomPerson user) {
		try {
			log.debug("Executing python 'newUser' method");
			UpdateUserType externalType = (UpdateUserType) customScriptConfiguration.getExternalType();
			Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();
			return externalType.newUser(user, configurationAttributes);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
            saveScriptError(customScriptConfiguration.getCustomScript(), ex);
		}
		
		return false;
	}

	public boolean executeExternalNewUserMethods(GluuCustomPerson user) {
		boolean result = true;
		for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
			result &= executeExternalNewUserMethod(customScriptConfiguration, user);
			if (!result) {
				return result;
			}
		}

		return result;
	}

	public boolean executeExternalDeleteUserMethod(CustomScriptConfiguration customScriptConfiguration, GluuCustomPerson user, boolean persisted) {
		try {
			log.debug("Executing python 'addUser' method");
			UpdateUserType externalType = (UpdateUserType) customScriptConfiguration.getExternalType();
			Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();
			return externalType.deleteUser(user, persisted, configurationAttributes);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
            saveScriptError(customScriptConfiguration.getCustomScript(), ex);
		}
		
		return false;
	}

	public boolean executeExternalDeleteUserMethods(GluuCustomPerson user) {
		boolean result = true;
		for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
			result &= executeExternalDeleteUserMethod(customScriptConfiguration, user, true);
			if (!result) {
				return result;
			}
		}

		return result;
	}

	public boolean executeExternalPostDeleteUserMethod(CustomScriptConfiguration customScriptConfiguration, GluuCustomPerson user) {
        int apiVersion = executeExternalGetApiVersion(customScriptConfiguration);
        if (apiVersion < 2) {
        	return true;
        }

        try {
			log.debug("Executing python 'postAddUser' method");
			UpdateUserType externalType = (UpdateUserType) customScriptConfiguration.getExternalType();
			Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();
			return externalType.postDeleteUser(user, configurationAttributes);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
            saveScriptError(customScriptConfiguration.getCustomScript(), ex);
		}
		
		return false;
	}

	public boolean executeExternalPostDeleteUserMethods(GluuCustomPerson user) {
		boolean result = true;
		for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
			result &= executeExternalPostDeleteUserMethod(customScriptConfiguration, user);
			if (!result) {
				return result;
			}
		}

		return result;
	}

	public int executeExternalGetApiVersion(CustomScriptConfiguration customScriptConfiguration) {
		try {
			log.debug("Executing python 'getApiVersion' method");
			UpdateUserType externalAuthenticator = (UpdateUserType) customScriptConfiguration.getExternalType();
			return externalAuthenticator.getApiVersion();
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
            saveScriptError(customScriptConfiguration.getCustomScript(), ex);
		}

		return -1;
	}

}
