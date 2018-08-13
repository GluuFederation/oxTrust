/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import org.gluu.jsf2.message.FacesMessages;
import org.gluu.oxtrust.ldap.service.ApplianceService;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.LogViewerConfig;
import org.gluu.oxtrust.service.logger.log.LogFilesService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;
import org.xdi.service.JsonService;
import org.xdi.service.security.Secure;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * Action class for configuring log viewer
 *
 * @author Yuriy Movchan Date: 07/08/2013
 */
@ConversationScoped
@Named
@Secure("#{permissionService.hasPermission('log', 'access')}")
public class ViewLogFileAction implements Serializable {

	private static final long serialVersionUID = -3310340481895022468L;

	@Inject
	private Logger log;

	@Inject
	private ApplianceService applianceService;

	@Inject
	private FacesMessages facesMessages;

	@Inject
	private JsonService jsonService;

	private GluuAppliance appliance;

	private LogFilesService logFilesService;

	private boolean initialized;

	private int activeLogFileIndex;

	private String currentLogFileName = "";

	private int displayLastLinesCount;

	public String init() {
		if (this.logFilesService != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.appliance = applianceService.getAppliance();

		initConfigurations();

		this.activeLogFileIndex = -1;

		this.displayLastLinesCount = 400;
		this.initialized = true;

		return OxTrustConstants.RESULT_SUCCESS;
	}

	private void initConfigurations() {
		this.logFilesService = new LogFilesService(appliance, jsonService);
	}

	public boolean isInitialized() {
		return initialized;
	}

	public LogViewerConfig getLogViewerConfiguration() {
		return logFilesService.config();
	}

	public Map<Integer, String> getLogFiles() {
		return logFilesService.filesIndexedById();
	}

	public String getTailOfLogFile() {
		if (this.activeLogFileIndex == -1) {
			return "No Content available";
		}

		try {
			return this.logFilesService.logTailById(activeLogFileIndex, displayLastLinesCount);
		} catch (IOException ex) {
			log.error("Failed to read log file '{}'", logFilesService.filesIndexedById().get(activeLogFileIndex), ex);
			return String.format("Failed to read log file '%s'",
					logFilesService.filesIndexedById().get(activeLogFileIndex));
		}
	}

	public int getActiveLogFileIndex() {
		return activeLogFileIndex;
	}

	public void setActiveLogFileIndex(int activeLogFileIndex) {
		this.activeLogFileIndex = activeLogFileIndex;
		setCurrentLogFileName(this.logFilesService.getLogName(activeLogFileIndex));
	}

	public int getDisplayLastLinesCount() {
		return displayLastLinesCount;
	}

	public void setDisplayLastLinesCount(int displayLinesCount) {
		this.displayLastLinesCount = displayLinesCount;
	}

	public String getCurrentLogFileName() {
		return currentLogFileName;
	}

	public void setCurrentLogFileName(String currentLogFileName) {
		this.currentLogFileName = currentLogFileName;
	}

}
