/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import javax.ejb.Stateless;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;

import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.util.StringHelper;

/**
 * GluuConfiguration service
 * 
 * @author Oleksiy Tataryn Date: 08.07.2014
 */
@Stateless
@Named
@Deprecated //TODO: We don't need this class
public class OxTrustConfigurationService {
	
	@Inject
	private AppConfiguration appConfiguration;

	@Inject
	private ServletContext context;

	public String getCssLocation() {
		if (StringHelper.isEmpty(appConfiguration.getCssLocation())) {
			FacesContext ctx = FacesContext.getCurrentInstance();
			if (ctx == null) {
				return "";
			}
			String contextPath = ctx.getExternalContext().getRequestContextPath();
			return contextPath + "/stylesheet";
		} else {
			return appConfiguration.getCssLocation();
		}
	}

	public String getJsLocation() {
		if (StringHelper.isEmpty(appConfiguration.getJsLocation())) {
			String contextPath = context.getContextPath();
			return contextPath + "/js";
		} else {
			return appConfiguration.getJsLocation();
		}
	}

}
