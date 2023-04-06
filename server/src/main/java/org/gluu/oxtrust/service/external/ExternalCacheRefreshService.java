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
import org.gluu.model.custom.script.model.bind.BindCredentials;
import org.gluu.model.custom.script.type.user.CacheRefreshType;
import org.gluu.oxtrust.ldap.cache.model.GluuSimplePerson;
import org.gluu.oxtrust.model.GluuCustomPerson;
import org.gluu.service.custom.script.ExternalScriptService;
import org.gluu.util.StringHelper;

/**
 * Provides factory methods needed to create external cache refresh extension
 * 
 * @author Yuriy Movchan Date: 01/12/2015
 */
@ApplicationScoped
@Named("externalCacheRefreshService")
public class ExternalCacheRefreshService extends ExternalScriptService {

	private static final long serialVersionUID = 1707751544454591273L;

	public ExternalCacheRefreshService() {
		super(CustomScriptType.CACHE_REFRESH);
	}

	public boolean executeExternalUpdateUserMethod(CustomScriptConfiguration customScriptConfiguration, GluuCustomPerson user) {
		try {
			log.debug("Executing python 'updateUser' method");
			CacheRefreshType externalType = (CacheRefreshType) customScriptConfiguration.getExternalType();
			Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();
			return externalType.updateUser(user, configurationAttributes);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
            saveScriptError(customScriptConfiguration.getCustomScript(), ex);
		}

		return false;
	}

	public boolean executeExternalUpdateSourceUserMethod(CustomScriptConfiguration customScriptConfiguration, GluuSimplePerson user) {
		try {
            CacheRefreshType externalType = (CacheRefreshType) customScriptConfiguration.getExternalType();
            Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();
            
            // Execute only if API > 3
            if (externalType.getApiVersion() > 3) {
                log.debug("Executing python 'updateSourceUser' method");

				return externalType.updateSourceUser(user, configurationAttributes);
            }
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
            saveScriptError(customScriptConfiguration.getCustomScript(), ex);
		}

		return false;
	}

    public BindCredentials executeExternalGetBindCredentialsMethod(CustomScriptConfiguration customScriptConfiguration, String configId) {
        try {
            log.debug("Executing python 'getBindCredentialsMethod' method");
            CacheRefreshType externalType = (CacheRefreshType) customScriptConfiguration.getExternalType();
            Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();
            
            // Execute only if API > 1
            if (externalType.getApiVersion() > 1) {
                return externalType.getBindCredentials(configId, configurationAttributes);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            saveScriptError(customScriptConfiguration.getCustomScript(), ex);
        }

        return null;
    }

    public boolean executeExternalIsStartProcessMethod(CustomScriptConfiguration customScriptConfiguration) {
        try {
            log.debug("Executing python 'executeExternalIsStartProcessMethod' method");
            CacheRefreshType externalType = (CacheRefreshType) customScriptConfiguration.getExternalType();
            Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();
            
            // Execute only if API > 2
            if (externalType.getApiVersion() > 2) {
                return externalType.isStartProcess(configurationAttributes);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            saveScriptError(customScriptConfiguration.getCustomScript(), ex);
        }

        return false;
    }

	public boolean executeExternalUpdateUserMethods(GluuCustomPerson user) {
		boolean result = true;
		for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
			result &= executeExternalUpdateUserMethod(customScriptConfiguration, user);
			if (!result) {
				return result;
			}
		}

		return result;
	}

	public boolean executeExternalUpdateSourceUserMethods(GluuSimplePerson user) {
		boolean result = true;
		for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
			result &= executeExternalUpdateSourceUserMethod(customScriptConfiguration, user);
			if (!result) {
				return result;
			}
		}

		return result;
	}

    public BindCredentials executeExternalGetBindCredentialsMethods(String configId) {
        BindCredentials result = null;
        for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
            result = executeExternalGetBindCredentialsMethod(customScriptConfiguration, configId);
            if (result != null) {
                return result;
            }
        }

        return result;
    }

	public boolean executeExternalIsStartProcessMethods() {
		boolean result = this.customScriptConfigurations.size() > 0;

		for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
			result &= executeExternalIsStartProcessMethod(customScriptConfiguration);
			if (!result) {
				return result;
			}
		}

		return result;
	}

}
