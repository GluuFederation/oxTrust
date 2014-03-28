package org.gluu.oxtrust.action;

import java.io.Serializable;
import java.util.List;

import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.LogViewerConfig;
import org.gluu.oxtrust.model.SimpleCustomPropertiesListModel;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.xdi.model.SimpleCustomProperty;
import org.xdi.service.JsonService;
import org.xdi.util.StringHelper;

/**
 * Action class for configuring log viewer
 * 
 * @author Yuriy Movchan Date: 07/08/2013
 */
@Name("configureLogViewerAction")
@Scope(ScopeType.CONVERSATION)
@Restrict("#{identity.loggedIn}")
public class ConfigureLogViewerAction implements SimpleCustomPropertiesListModel, Serializable {

	private static final long serialVersionUID = -3310460481895022468L;

	@Logger
	private Log log;

	@In
	private FacesMessages facesMessages;

	@In
	private JsonService jsonService;

	private GluuAppliance appliance;

	private LogViewerConfig logViewerConfiguration;

	private boolean initialized;

	@Restrict("#{s:hasPermission('configuration', 'access')}")
	public String init() {
		if (this.logViewerConfiguration != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.appliance = ApplianceService.instance().getAppliance();

		initConfigurations();

		this.initialized = true;

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private void initConfigurations() {
		this.logViewerConfiguration = prepareLogViewerConfig();
	}

	@Restrict("#{s:hasPermission('configuration', 'access')}")
	public String update() {
		if (!validateLists()) {
			return OxTrustConstants.RESULT_FAILURE;
		}

		updateAppliance();

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private void updateAppliance() {
		GluuAppliance updateAppliance = ApplianceService.instance().getAppliance();
		try {
			updateAppliance.setOxLogViewerConfig(jsonService.objectToJson(logViewerConfiguration));
			ApplianceService.instance().updateAppliance(updateAppliance);
		} catch (Exception ex) {
			log.error("Failed to save log viewer configuration '{0}'", ex);
		}
	}

	private boolean validateLists() {
		boolean result = true;

		return result;
	}

	@Restrict("#{s:hasPermission('configuration', 'access')}")
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
