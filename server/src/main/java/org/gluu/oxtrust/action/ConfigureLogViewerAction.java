/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import javax.faces.application.FacesMessage;

import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.LogViewerConfig;
import org.gluu.oxtrust.model.SimpleCustomPropertiesListModel;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.jsf2.message.FacesMessages;
import org.slf4j.Logger;
import org.xdi.model.SimpleCustomProperty;
import org.xdi.service.JsonService;
import org.xdi.util.StringHelper;

/**
 * Action class for configuring log viewer
 * 
 * @author Yuriy Movchan Date: 07/08/2013
 */
@Named("configureLogViewerAction")
@ConversationScoped
//TODO CDI @Restrict("#{identity.loggedIn}")
public class ConfigureLogViewerAction implements SimpleCustomPropertiesListModel, Serializable {

	private static final long serialVersionUID = -3310460481895022468L;

	@Inject
	private Logger log;
	
	@Inject
	private ApplianceService applianceService;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private JsonService jsonService;

	private GluuAppliance appliance;

	private LogViewerConfig logViewerConfiguration;

	private boolean initialized;

	//TODO CDI @Restrict("#{s:hasPermission('configuration', 'access')}")
	public String init() {
		if (this.logViewerConfiguration != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.appliance = applianceService.getAppliance();

		initConfigurations();

		this.initialized = true;

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private void initConfigurations() {
		this.logViewerConfiguration = prepareLogViewerConfig();
	}

	//TODO CDI @Restrict("#{s:hasPermission('configuration', 'access')}")
	public String update() {
		if (!validateLists()) {
			return OxTrustConstants.RESULT_FAILURE;
		}

		updateAppliance();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private void updateAppliance() {
		GluuAppliance updateAppliance = applianceService.getAppliance();
		try {
			updateAppliance.setOxLogViewerConfig(jsonService.objectToJson(logViewerConfiguration));
			applianceService.updateAppliance(updateAppliance);
		} catch (Exception ex) {
			log.error("Failed to save log viewer configuration '{0}'", ex);
		}
	}

	private boolean validateLists() {
		boolean result = true;

		return result;
	}

	//TODO CDI @Restrict("#{s:hasPermission('configuration', 'access')}")
	public void cancel() {
	}

	private LogViewerConfig prepareLogViewerConfig() {
		LogViewerConfig logViewerConfig = null;

		String oxLogViewerConfig = appliance.getOxLogViewerConfig();
		if (StringHelper.isNotEmpty(oxLogViewerConfig)) {
			try {
				logViewerConfig = jsonService.jsonToObject(appliance.getOxLogViewerConfig(), LogViewerConfig.class);
			} catch (Exception ex) {
				log.error("Failed to load log viewer configuration '{0}'", ex, oxLogViewerConfig);
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

}
