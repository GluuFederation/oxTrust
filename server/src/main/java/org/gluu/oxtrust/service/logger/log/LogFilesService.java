package org.gluu.oxtrust.service.logger.log;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.model.LogViewerConfig;
import org.xdi.model.SimpleCustomProperty;
import org.xdi.service.JsonService;
import org.xdi.util.StringHelper;
import org.xdi.util.io.ReverseLineReader;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogFilesService {

	private static final Logger LOG = Logger.getLogger(LogFilesService.class);

	private final GluuAppliance appliance;
	private final JsonService jsonService;

	private LogViewerConfig config;
	private Map<Integer, String> logFiles;

	public LogFilesService(GluuAppliance appliance, JsonService jsonService) {
		this.appliance = appliance;
		this.jsonService = jsonService;
		this.config = null;
		this.logFiles = null;
	}

	public Map<Integer, String> filesIndexedById() {
		if (logFiles != null) {
			return logFiles;
		}

		this.config = config();

		logFiles = new HashMap<Integer, String>();
		for (SimpleCustomProperty logTemplate : config.getLogTemplates()) {
			String logTemplatePattern = logTemplate.getValue2();
			if (StringHelper.isEmpty(logTemplatePattern)) {
				continue;
			}

			File[] files = listFiles(logTemplatePattern);
			if (files == null) {
				continue;
			}

			for (File file : files) {
				logFiles.put(logFiles.size(), file.getPath());
			}
		}

		return logFiles;
	}

	private File[] listFiles(String logTemplatePattern) {
		String logTemplatePath = FilenameUtils.getFullPath(logTemplatePattern);
		String logTemplateFile = FilenameUtils.getName(logTemplatePattern);

		File logTemplateBaseDir = new File(logTemplatePath);

		FileFilter fileFilter = new AndFileFilter(FileFileFilter.FILE, new WildcardFileFilter(logTemplateFile));
		return logTemplateBaseDir.listFiles(fileFilter);
	}

	public LogViewerConfig config() {
		if (this.config != null) {
			return this.config;
		}

		String oxLogViewerConfig = appliance.getOxLogViewerConfig();
		if (StringHelper.isNotEmpty(oxLogViewerConfig)) {
			try {
				return jsonService.jsonToObject(appliance.getOxLogViewerConfig(), LogViewerConfig.class);
			} catch (IOException e) {
				LOG.error("Error loading log files", e);
				return new LogViewerConfig();
			}
		}

		return new LogViewerConfig();
	}

	public String logTailById(int id, int numberOfLines) throws IOException {
		Map<Integer, String> logFiles = filesIndexedById();

		if (!logFiles.containsKey(id)) {
			throw new FileNotFoundException();
		}

		File logFile = new File(logFiles.get(id));
		return parseFile(numberOfLines, logFile);
	}

	private String parseFile(int numberOfLines, File logFile) throws IOException {
		ReverseLineReader reverseLineReader = new ReverseLineReader(logFile, Charset.defaultCharset().name());
		try {
			List<String> logFileLastLines = reverseLineReader.readLastLines(numberOfLines);

			StringBuilder sb = new StringBuilder();
			for (String logFileLastLine : logFileLastLines) {
				sb.append(logFileLastLine);
				sb.append('\n');
			}

			return sb.toString();
		} finally {
			reverseLineReader.close();
		}
	}

	public String getLogName(int index) {
		return this.logFiles.get(index);
	}

}