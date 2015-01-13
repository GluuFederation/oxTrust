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
import org.xdi.model.custom.script.type.user.CacheRefreshType;
import org.xdi.service.custom.script.ExternalScriptService;

/**
 * Provides factory methods needed to create external cache refresh extension
 * 
 * @author Yuriy Movchan Date: 01/12/2015
 */
@Scope(ScopeType.APPLICATION)
@Name("externalCacheRefreshService")
@AutoCreate
@Startup
public class ExternalCacheRefreshService extends ExternalScriptService {

	private static final long serialVersionUID = 1707751544454591273L;

	public ExternalCacheRefreshService() {
		super(CustomScriptType.CACHE_REFRESH);
	}

	public boolean executeExternalCacheRefreshUpdateMethod(CustomScriptConfiguration customScriptConfiguration, GluuCustomPerson user) {
		try {
			log.debug("Executing python 'updateUser' method");
			CacheRefreshType externalType = (CacheRefreshType) customScriptConfiguration.getExternalType();
			Map<String, SimpleCustomProperty> configurationAttributes = customScriptConfiguration.getConfigurationAttributes();
			return externalType.updateUser(user, configurationAttributes);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}

		return false;
	}

	public boolean executeExternalDefaultCacheRefreshUpdateMethod(GluuCustomPerson user) {
		return executeExternalCacheRefreshUpdateMethod(this.defaultExternalCustomScript, user);
	}

	public boolean executeExternalCacheRefreshUpdateMethods(GluuCustomPerson user) {
		boolean result = true;
		for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
			result &= executeExternalCacheRefreshUpdateMethod(customScriptConfiguration, user);
			if (!result) {
				return result;
			}
		}

		return result;
	}

	public static ExternalCacheRefreshService instance() {
		return (ExternalCacheRefreshService) Component.getInstance(ExternalCacheRefreshService.class);
	}

}
