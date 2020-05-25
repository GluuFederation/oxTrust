/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.gluu.oxtrust.service.config.ConfigurationFactory;
import org.gluu.oxtrust.model.FileData;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.service.cdi.event.LogFileSizeChekerEvent;
import org.gluu.service.XmlService;
import org.gluu.service.cdi.async.Asynchronous;
import org.gluu.service.cdi.event.Scheduled;
import org.gluu.service.timer.event.TimerEvent;
import org.gluu.service.timer.schedule.TimerSchedule;
import org.gluu.util.StringHelper;
import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@ApplicationScoped
@Named("logFileSizeChecker")
public class LogFileSizeChecker {

	private static final int DEFAULT_INTERVAL = 60 * 60 * 24; // 24 hours

	@Inject
	private Logger log;

	@Inject
	private Event<TimerEvent> timerEvent;

	@Inject
	ConfigurationService configurationService;

	@Inject
	private XmlService xmlService;

	private AtomicBoolean isActive;

	public void initTimer() {
		log.info("Initializing Log File Size Checker Timer");
		this.isActive = new AtomicBoolean(false);

		final int delay = 2 * 60;
		final int interval = DEFAULT_INTERVAL;

		timerEvent.fire(new TimerEvent(new TimerSchedule(delay, interval), new LogFileSizeChekerEvent(),
				Scheduled.Literal.INSTANCE));
	}

	@Asynchronous
	public void process(@Observes @Scheduled LogFileSizeChekerEvent logFileSizeChekerEvent) {
		if (this.isActive.get()) {
			return;
		}

		if (!this.isActive.compareAndSet(false, true)) {
			return;
		}

		try {
			processInt();
		} finally {
			this.isActive.set(false);
		}
	}

	/**
	 * Gather periodically site and server status
	 */
	private void processInt() {
		GluuConfiguration configuration = configurationService.getConfiguration();
		long maxSize = configuration.getMaxLogSize();
		;
		log.debug("Max Log Size: " + maxSize);

		if (maxSize > 0) {
			log.debug("Max Log Size: " + maxSize);

			long maxSizeInByte = maxSize * 1024 * 1024;
			long currentSize = 0;
			Date today = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String todayStr = sdf.format(today);

			log.debug("Getting the tomcat home directory");
			String filePath = ConfigurationFactory.DIR + ConfigurationFactory.LOG_ROTATION_CONFIGURATION;
			log.debug("FilePath: " + filePath);

			List<LogDir> logDirs = readConfig(filePath);

			List<FileData> fDataList = new ArrayList<FileData>();
			for (LogDir logDir : logDirs) {
				File file = new File(logDir.getLocation());
				File[] files = file.listFiles();
				long totalSize = 0;

				if (files != null && files.length > 0) {
					for (File singleFile : files) {
						if (singleFile.getName().startsWith(logDir.getPrefix())
								&& singleFile.getName().endsWith(logDir.getExtension())) {
							totalSize += singleFile.length();
							FileData fData = new FileData(singleFile.getName(), logDir.getLocation(),
									singleFile.lastModified(), singleFile.length());
							fDataList.add(fData);
						}
					}
				}

				currentSize += totalSize;
				logDir.setLength(totalSize);
			}

			Collections.sort(fDataList);
			if (currentSize > maxSizeInByte) {
				maxSizeInByte -= (maxSizeInByte * 15) / 100; // empty 15% space
																// less of the
																// maximum
																// allocated
			}
			for (FileData fileData : fDataList) {
				if (currentSize < maxSizeInByte) {
					break;
				}
				Date date = new Date(fileData.getLastModified());
				String dateStr = sdf.format(date);
				if (todayStr.equals(dateStr)) {
					log.debug("--Skipped Active File: " + fileData.getName() + " Date: " + sdf.format(date) + " Size: "
							+ fileData.getSize());
					continue;
				}

				File singleFile = new File(fileData.getFilePath() + "/" + fileData.getName());
				if (!singleFile.delete()) {
					log.error("Failed to delete the file.");
				} else {
					currentSize -= fileData.getSize();
					log.debug("--Deleted File Name: " + fileData.getName() + " Date: " + sdf.format(date) + " Size: "
							+ fileData.getSize());
				}
			}
		}
	}

	private List<LogDir> readConfig(String source) {
		List<LogDir> logDirs = new ArrayList<LogDir>();
		try {
			org.w3c.dom.Document document = xmlService.getXmlDocument(FileUtils.readFileToByteArray(new File(source)));
			XPath xPath = XPathFactory.newInstance().newXPath();
			XPathExpression entriesXPath = xPath.compile("/entries/entry");

			NodeList list = (NodeList) entriesXPath.evaluate(document, XPathConstants.NODESET);
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);

				String prefix = null;
				String location = null;
				String extension = null;

				NodeList subList = node.getChildNodes();
				for (int j = 0; j < subList.getLength(); j++) {
					Node subNode = subList.item(j);
					String subNodeName = subNode.getNodeName();
					String subNodeValue = subNode.getTextContent();

					if (StringHelper.equalsIgnoreCase(subNodeName, "prefix")) {
						prefix = subNodeValue;
					} else if (StringHelper.equalsIgnoreCase(subNodeName, "location")) {
						location = subNodeValue;
					} else if (StringHelper.equalsIgnoreCase(subNodeName, "extension")) {
						extension = subNodeValue;
					}
				}

				if (extension == null || extension.trim().equals("")) {
					extension = "log";
				}

				LogDir logDir = new LogDir(prefix, location, extension);
				logDirs.add(logDir);
				log.debug("Prefix: " + prefix + " Location: " + location);
			}
		} catch (Exception ex) {
			log.debug("Exception while reading configuration file: " + ex);
		}
		return logDirs;
	}
}

class LogDir {

	private String name;
	private String prefix;
	private String pattern;
	private String location;
	private String extension;
	private long length;

	public LogDir(String name, String prefix, String pattern, String location, String extension) {
		this.name = name;
		this.prefix = prefix;
		this.pattern = pattern;
		this.location = location;
		this.extension = extension;
	}

	public LogDir(String prefix, String location, String extension) {
		this.prefix = prefix;
		this.location = location;
		this.extension = extension;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}
}
