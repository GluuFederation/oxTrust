/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.web.ServletContexts;
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.util.StringHelper;

/**
 * GluuAppliance service
 * 
 * @author Oleksiy Tataryn Date: 08.07.2014
 */
@Scope(ScopeType.STATELESS)
@Name("oxTrustConfigurationService")
@AutoCreate
public class OxTrustConfigurationService {
	
	@In(value = "#{oxTrustConfiguration.applicationConfiguration}")
	private ApplicationConfiguration applicationConfiguration;

	public String getCssLocation() {
		if (StringHelper.isEmpty(applicationConfiguration.getCssLocation())){
			String contextPath = ServletContexts.instance().getRequest().getContextPath();
			return contextPath + "/stylesheet";
		}else{
			return applicationConfiguration.getCssLocation();
		}
	}
	
	public String getJsLocation() {
		if (StringHelper.isEmpty(applicationConfiguration.getJsLocation())){
			String contextPath = ServletContexts.instance().getRequest().getContextPath();
			return contextPath + "/js";
		}else{
			return applicationConfiguration.getJsLocation();
		}
	}


}
