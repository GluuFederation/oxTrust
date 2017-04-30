/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;

import org.gluu.oxtrust.model.AuthenticationChartDto;
import org.gluu.oxtrust.service.MetricService;
import org.jboss.seam.ScopeType;
import javax.inject.Inject;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;
import javax.enterprise.context.ConversationScoped;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.xdi.service.JsonService;

/**
 * Action class for home page
 * 
 * @author Yuriy Movchan Date: 08/28/2015
 */
@Named("homeAction")
@ConversationScoped
@Restrict("#{identity.loggedIn}")
public class HomeAction implements Serializable {

	private static final long serialVersionUID = 5130372165991117114L;

	@Logger
	private Log log;

	@Inject
	private MetricService metricService;

	@Inject
	private JsonService jsonService;

	@Inject
	private Identity identity;

	private AuthenticationChartDto authenticationChartDto;

	private String authenticationChartJson;

	public void init() {
		boolean hasConfigurationAccess = identity.hasPermission("configuration", "access");
		if (hasConfigurationAccess) {
			generateAuthenticationChart();
		}
	}

	private boolean generateAuthenticationChart() {
		try {
			this.authenticationChartDto = metricService.genereateAuthenticationChartDto(7);
			this.authenticationChartJson = jsonService.objectToJson(authenticationChartDto);
		} catch (Exception ex) {
			log.error("Failed to prepare authentication chart", ex);
			return false;
		}

		return true;
	}

	public String getAuthenticationChartJson() {
		return authenticationChartJson;
	}

	public AuthenticationChartDto getAuthenticationChartDto() {
		return authenticationChartDto;
	}

}
