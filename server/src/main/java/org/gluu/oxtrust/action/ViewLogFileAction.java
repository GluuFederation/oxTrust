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
import org.gluu.oxtrust.model.log.LogFiles;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.slf4j.Logger;
import org.xdi.service.JsonService;
import org.xdi.service.security.Secure;
import org.xdi.util.io.ReverseLineReader;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.List;
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

	private LogViewerConfig logViewerConfiguration;
	private Map<Integer, String> logFiles;

	private boolean initialized;

	private int activeLogFileIndex;

	private int displayLastLinesCount;

	public String init() {
		if (this.logViewerConfiguration != null) {
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
		this.logViewerConfiguration = prepareLogViewerConfig();

		this.logFiles = prepareLogFiles();
	}

    private LogViewerConfig prepareLogViewerConfig() {
        LogViewerConfig logViewerConfig = null;

        try {
            logViewerConfig = new LogFiles().config(appliance, jsonService);
        } catch (Exception ex) {
            log.error("Failed to load log viewer configuration '{}'", appliance.getOxLogViewerConfig(), ex);
        }

        if (logViewerConfig == null) {
            logViewerConfig = new LogViewerConfig();
        }

        return logViewerConfig;
    }

    private Map<Integer, String> prepareLogFiles() {
        return new LogFiles().filesIndexedById(logViewerConfiguration);
    }

	public boolean isInitialized() {
		return initialized;
	}

	public LogViewerConfig getLogViewerConfiguration() {
		return logViewerConfiguration;
	}

	public Map<Integer, String> getLogFiles() {
		return logFiles;
	}

	public String getTailOfLogFile() {
		if (this.activeLogFileIndex == -1) {
			return "";
		}

		File activeLogFile = new File(this.logFiles.get(activeLogFileIndex));
		ReverseLineReader reverseLineReader = new ReverseLineReader(activeLogFile, Charset.defaultCharset().name());
		try {
			List<String> logFileLastLines = reverseLineReader.readLastLines(this.displayLastLinesCount);

			StringBuilder sb = new StringBuilder();
			for (String logFileLastLine : logFileLastLines) {
				sb.append(logFileLastLine);
				sb.append('\n');
			}

			return sb.toString();
		} catch (IOException ex) {
			log.error("Failed to read log file '{}'", this.logFiles.get(activeLogFileIndex), ex);
			String result = String.format("Failed to read log file '%s'", this.logFiles.get(activeLogFileIndex));

			return result;
		} finally {
			try {
				reverseLineReader.close();
			} catch (IOException ex) {
				log.error("Failed to destory ReverseLineReader", ex);
			}
		}
	}

	public int getActiveLogFileIndex() {
		return activeLogFileIndex;
	}

	public void setActiveLogFileIndex(int activeLogFileIndex) {
		this.activeLogFileIndex = activeLogFileIndex;
	}


	public int getDisplayLastLinesCount() {
		return displayLastLinesCount;
	}

	public void setDisplayLastLinesCount(int displayLinesCount) {
		this.displayLastLinesCount = displayLinesCount;
	}

}
