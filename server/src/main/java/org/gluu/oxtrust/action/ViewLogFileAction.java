/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.action;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.gluu.model.SimpleCustomProperty;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.model.LogViewerConfig;
import org.gluu.oxtrust.service.ConfigurationService;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.security.Secure;
import org.gluu.util.StringHelper;
import org.gluu.util.io.ReverseLineReader;
import org.slf4j.Logger;

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
	private ConfigurationService configurationService;

	private GluuConfiguration configuration;

	private LogViewerConfig logViewerConfiguration;
	private Map<Integer, String> logFiles;

	private boolean initialized;

	private int activeLogFileIndex;
	
	private String currentLogFileName="";

	private int displayLastLinesCount;

	public String init() {
		if (this.logViewerConfiguration != null) {
			return OxTrustConstants.RESULT_SUCCESS;
		}

		this.configuration = configurationService.getConfiguration();

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
		LogViewerConfig logViewerConfig = configuration.getOxLogViewerConfig();

		if (logViewerConfig == null) {
			logViewerConfig = new LogViewerConfig();
		}

		return logViewerConfig;
	}

	private Map<Integer, String> prepareLogFiles() {
		Map<Integer, String> logFiles = new HashMap<Integer, String>();

		int fileIndex = 0;
		for (SimpleCustomProperty logTemplate : this.logViewerConfiguration.getLogTemplates()) {
			String logTemplatePattern = logTemplate.getValue2();
			if (StringHelper.isEmpty(logTemplatePattern)) {
				continue;
			}

			String logTemplatePath = FilenameUtils.getFullPath(logTemplatePattern);
			String logTemplateFile = FilenameUtils.getName(logTemplatePattern);

			File logTemplateBaseDir = new File(logTemplatePath);

			FileFilter fileFilter = new AndFileFilter(FileFileFilter.FILE, new WildcardFileFilter(logTemplateFile));
			File[] files = logTemplateBaseDir.listFiles(fileFilter);
			if (files == null) {
				continue;
			}

			for (int i = 0; i < files.length; i++) {
				logFiles.put(fileIndex++, files[i].getPath());
			}
		}

		return logFiles;
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
			return "No content available";
		}
		setCurrentLogFileName(this.logFiles.get(activeLogFileIndex));
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
		setCurrentLogFileName(this.logFiles.get(this.activeLogFileIndex));
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
