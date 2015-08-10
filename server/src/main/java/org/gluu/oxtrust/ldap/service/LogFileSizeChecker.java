/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.oxtrust.model.FileData;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalDuration;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.log.Log;
import org.xml.sax.InputSource;

@AutoCreate
@Scope(ScopeType.APPLICATION)
@Name("logFileSizeChecker")
public class LogFileSizeChecker {

	@Logger
	Log log;

	@In
	ApplianceService applianceService;

	@Create
	public void create() {
		// Initialization Code
	}

	@Asynchronous
	public QuartzTriggerHandle scheduleSizeChecking(@Expiration Date when, @IntervalDuration Long interval) {
		process(when, interval);
		return null;
	}

	/**
	 * Gather periodically site and server status
	 * 
	 * @param when
	 *            Date
	 * @param interval
	 *            Interval
	 */
	private void process(Date when, Long interval) {
		GluuAppliance appliance;
		appliance = applianceService.getAppliance();
		String maxLogSize = appliance.getMaxLogSize();
		log.debug("Max Log Size: " + maxLogSize);
		long maxSize = 0;

		try {
			maxSize = Long.parseLong(maxLogSize); // MB
		} catch (Exception ex) {
			log.error("appliance maxLogSize value is invalid: " + maxLogSize);
			log.error("assuming 0");
		}

		if (maxSize > 0) {

			log.debug("Max Log Size: " + maxLogSize);

			long maxSizeInByte = maxSize * 1024 * 1024;
			long currentSize = 0;
			Date today = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String todayStr = sdf.format(today);

			String filePath = "";
			String tomcatHome = System.getProperty("catalina.home");
			if (tomcatHome != null) {
				log.debug("Setting the tomcat home directory");
				filePath = System.getProperty("catalina.home") + File.separator + "conf" + File.separator
						+ OxTrustConfiguration.LOG_ROTATION_CONFIGURATION;
				log.debug("FilePath: " + filePath);
			} else {
				log.error(OxTrustConfiguration.LOG_ROTATION_CONFIGURATION + " file not found");
				return;
			}

			List<LogDir> logDirs = readConfig(filePath);

			List<FileData> fDataList = new ArrayList<FileData>();
			for (LogDir logDir : logDirs) {
				File file = new File(logDir.getLocation());
				File[] files = file.listFiles();
				long totalSize = 0;

				if (files != null && files.length > 0) {
					for (File singleFile : files) {
						if (singleFile.getName().startsWith(logDir.getPrefix()) && singleFile.getName().endsWith(logDir.getExtension())) {
							totalSize += singleFile.length();
							FileData fData = new FileData(singleFile.getName(), logDir.getLocation(), singleFile.lastModified(),
									singleFile.length());
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
					log.debug("--Deleted File Name: " + fileData.getName() + " Date: " + sdf.format(date) + " Size: " + fileData.getSize());
				}
			}
		}
	}

	private List<LogDir> readConfig(String source) {
		List<LogDir> logDirs = new ArrayList<LogDir>();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			org.w3c.dom.Document document = builder.parse(new InputSource(new FileInputStream(new File(source))));
			DOMReader reader = new DOMReader();
			Document doc = reader.read(document);
			Element element = doc.getRootElement();
			Iterator itr = element.elementIterator();
			while (itr.hasNext()) {
				element = (Element) itr.next();
				String prefix = element.element("prefix").getText();
				String location = element.element("location").getText();

				String extension = "";
				if (element.element("extension") != null) {
					extension = element.element("extension").getText();
				}
				if (extension == null || extension.trim().equals("")) {
					extension = "log";
				}

				LogDir logDir = new LogDir(prefix, location, extension);
				logDirs.add(logDir);
				log.debug("Prefix: " + prefix + " Location: " + location);
			}
			log.info("OutXML: " + doc.asXML());
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
