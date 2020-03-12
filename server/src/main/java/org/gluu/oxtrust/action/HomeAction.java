/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.model.AuthenticationChartDto;
import org.gluu.oxtrust.service.MetricService;
import org.gluu.oxtrust.service.PermissionService;
import org.gluu.oxtrust.service.UpdateChecker;
import org.gluu.service.JsonService;
import org.gluu.service.security.Secure;
import org.slf4j.Logger;

/**
 * Action class for home page
 * 
 * @author Yuriy Movchan Date: 08/28/2015
 */
@Named("homeAction")
@ConversationScoped
@Secure("#{identity.loggedIn}")
public class HomeAction implements Serializable {

	private static final long serialVersionUID = 5130372165991117114L;

	@Inject
	private Logger log;

	@Inject
	private MetricService metricService;

	@Inject
	private JsonService jsonService;
	
	@Inject
	private UpdateChecker updateChecker;

	@Inject
	private PermissionService permissionService;

	private AuthenticationChartDto authenticationChartDto;

	private String authenticationChartJson;

	public void init() {
		boolean hasConfigurationAccess = permissionService.hasPermission("configuration", "access");
		if (hasConfigurationAccess) {
			generateAuthenticationChart();
		}
	}

	private boolean generateAuthenticationChart() {
		if (metricService.isMetricReporterEnabled()) {
			try {
				this.authenticationChartDto = metricService.genereateAuthenticationChartDto(6);
				this.authenticationChartJson = jsonService.objectToJson(authenticationChartDto);
			} catch (Exception ex) {
				log.error("Failed to prepare authentication chart", ex);
				return false;
			}
		}
		return true;
	}

	public String getAuthenticationChartJson() {
		return authenticationChartJson;
	}

	public AuthenticationChartDto getAuthenticationChartDto() {
		return authenticationChartDto;
	}
	
	public UpdateChecker getUpdateChecker() {
		return updateChecker;
	}

	public void setUpdateChecker(UpdateChecker updateChecker) {
		this.updateChecker = updateChecker;
	}

}
