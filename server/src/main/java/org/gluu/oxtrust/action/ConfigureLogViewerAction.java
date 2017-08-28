/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.jsf2.service.ConversationService;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.ldap.service.JsonConfigurationService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.LogViewerConfig;
import org.gluu.oxtrust.model.SimpleCustomPropertiesListModel;
import org.gluu.oxtrust.service.logger.LoggerService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;
import org.xdi.model.SimpleCustomProperty;
import org.xdi.oxauth.model.configuration.AppConfiguration;
import org.xdi.service.JsonService;
import org.xdi.service.security.Secure;
import org.xdi.util.StringHelper;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Action class for configuring log viewer
 *
 * @author Yuriy Movchan Date: 07/08/2013
 */
@Named("configureLogViewerAction")
@ConversationScoped
@Secure("#{permissionService.hasPermission('configuration', 'access')}")
public class ConfigureLogViewerAction implements SimpleCustomPropertiesListModel, Serializable {

	private static final long serialVersionUID = -3310460481895022468L;

	@Inject
	private Logger log;

	@Inject
	private ApplianceService applianceService;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private ConversationService conversationService;

	@Inject
	private JsonService jsonService;

	@Inject
	private LoggerService loggerService;

	@Inject
	private JsonConfigurationService jsonConfigurationService;

	private GluuAppliance appliance;

	private String oxTrustLogConfigLocation;
	private String oxAuthLogConfigLocation;

	private LogViewerConfig logViewerConfiguration;

	private boolean initialized;

	public String init() {
		if (this.logViewerConfiguration != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.appliance = applianceService.getAppliance();
		this.oxTrustLogConfigLocation = appliance.getOxLogConfigLocation();
		
		try {
			this.oxAuthLogConfigLocation = jsonConfigurationService.getOxauthAppConfiguration().getExternalLoggerConfiguration();
		} catch (Exception e) {
			log.error("Failed to retrieve oxauth configuration", e);
		}

		initConfigurations();

		this.initialized = true;

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private void initConfigurations() {
		this.logViewerConfiguration = prepareLogViewerConfig();
	}

	public String update() {
		if (!validateLists()) {
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update log viewer configuration");
			return OxTrustConstants.RESULT_FAILURE;
		}

		updateAppliance();
		updateOxAuthConfiguration();

    facesMessages.add(FacesMessage.SEVERITY_INFO, "Log viewer configuration updated");

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private void updateAppliance() {
		GluuAppliance updateAppliance = applianceService.getAppliance();
		try {
			updateAppliance.setOxLogViewerConfig(jsonService.objectToJson(logViewerConfiguration));
			updateAppliance.setOxLogConfigLocation(oxTrustLogConfigLocation);

			applianceService.updateAppliance(updateAppliance);
			loggerService.updateLoggerConfigLocation();
		} catch (Exception ex) {
			log.error("Failed to save log viewer configuration '{}'", ex);
		}
	}

	private void updateOxAuthConfiguration() {
		try {
			AppConfiguration appConfiguration = jsonConfigurationService.getOxauthAppConfiguration();
			appConfiguration.setExternalLoggerConfiguration(oxAuthLogConfigLocation);
			jsonConfigurationService.saveOxAuthAppConfiguration(appConfiguration);
		} catch (IOException e) {
			log.error("Failed to update oxauth-config.json", e);
			facesMessages.add(FacesMessage.SEVERITY_ERROR, "Failed to update oxAuth configuration in LDAP");
		}
	}

	private boolean validateLists() {
		boolean result = true;

		return result;
	}

	public String cancel() {
		facesMessages.add(FacesMessage.SEVERITY_INFO, "Log viewer configuration update were canceled");
		conversationService.endConversation();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private LogViewerConfig prepareLogViewerConfig() {
		LogViewerConfig logViewerConfig = null;

		String oxLogViewerConfig = appliance.getOxLogViewerConfig();
		if (StringHelper.isNotEmpty(oxLogViewerConfig)) {
			try {
				logViewerConfig = jsonService.jsonToObject(appliance.getOxLogViewerConfig(), LogViewerConfig.class);
			} catch (Exception ex) {
				log.error("Failed to load log viewer configuration '{}'", oxLogViewerConfig, ex);
			}
		}

		if (logViewerConfig == null) {
			logViewerConfig = new LogViewerConfig();
		}

		return logViewerConfig;
	}

	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public void addItemToSimpleCustomProperties(List<SimpleCustomProperty> simpleCustomProperties) {
		if (simpleCustomProperties != null) {
			simpleCustomProperties.add(new SimpleCustomProperty("description", ""));
		}
	}

	@Override
	public void removeItemFromSimpleCustomProperties(List<SimpleCustomProperty> simpleCustomProperties,
			SimpleCustomProperty simpleCustomProperty) {
		if (simpleCustomProperties != null) {
			simpleCustomProperties.remove(simpleCustomProperty);
		}
	}

	public LogViewerConfig getLogViewerConfiguration() {
		return logViewerConfiguration;
	}

	public String getOxTrustLogConfigLocation() {
		return oxTrustLogConfigLocation;
	}

	public void setOxTrustLogConfigLocation(String oxTrustLogConfigLocation) {
		this.oxTrustLogConfigLocation = oxTrustLogConfigLocation;
	}

	public String getOxAuthLogConfigLocation() {
		return oxAuthLogConfigLocation;
	}

	public void setOxAuthLogConfigLocation(String oxAuthLogConfigLocation) {
		this.oxAuthLogConfigLocation = oxAuthLogConfigLocation;
	}
}
