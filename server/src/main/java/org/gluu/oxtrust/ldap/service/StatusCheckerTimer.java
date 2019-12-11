/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.IOUtils;
import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.oxtrust.config.ConfigurationFactory;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.model.GluuOxTrustStat;
import org.gluu.oxtrust.model.status.ConfigurationStatus;
import org.gluu.oxtrust.model.status.OxtrustStat;
import org.gluu.oxtrust.service.cdi.event.StatusCheckerTimerEvent;
import org.gluu.oxtrust.util.NumberHelper;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.persist.exception.BasePersistenceException;
import org.gluu.service.cdi.async.Asynchronous;
import org.gluu.service.cdi.event.Scheduled;
import org.gluu.service.timer.event.TimerEvent;
import org.gluu.service.timer.schedule.TimerSchedule;
import org.gluu.util.StringHelper;
import org.gluu.util.process.ProcessHelper;
import org.slf4j.Logger;

/**
 * Gather periodically site and server status
 * 
 * @author Yuriy Movchan Date: 11.22.2010
 */
@ApplicationScoped
@Named
public class StatusCheckerTimer {

	private final static int DEFAULT_INTERVAL = 60; // 1 minute

	@Inject
	private Logger log;

	@Inject
	private Event<TimerEvent> timerEvent;

	@Inject
	private ConfigurationService configurationService;

	@Inject
	private CentralLdapService centralLdapService;

	@Inject
	private ConfigurationFactory configurationFactory;

	@Inject
	private AppConfiguration appConfiguration;

	private NumberFormat numberFormat;

	private AtomicBoolean isActive;

	@Inject
	private IGroupService groupService;

	@Inject
	private IPersonService personService;

	@PostConstruct
	public void create() {
		this.numberFormat = NumberFormat.getNumberInstance(Locale.US);
	}

	public void initTimer() {
		log.info("Initializing Daily Status Cheker Timer");
		this.isActive = new AtomicBoolean(false);

		final int delay = 1 * 60;
		final int interval = DEFAULT_INTERVAL;

		timerEvent.fire(new TimerEvent(new TimerSchedule(delay, interval), new StatusCheckerTimerEvent(),
				Scheduled.Literal.INSTANCE));
	}

	@Asynchronous
	public void process(@Observes @Scheduled StatusCheckerTimerEvent statusCheckerTimerEvent) {
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
	 * 
	 * @param when
	 *            Date
	 * @param interval
	 *            Interval
	 */
	private void processInt() {
		log.debug("Starting update of configuration status");
		log.debug("=================================GETTING=======================================");
		AppConfiguration appConfiguration = configurationFactory.getAppConfiguration();
		if (!appConfiguration.isUpdateStatus()) {
			log.debug("isUpdateStatus");
			return;
		}
		ConfigurationStatus configurationStatus = new ConfigurationStatus();
		OxtrustStat oxtrustStatObject = new OxtrustStat();
		oxtrustStatObject.setGroupCount(String.valueOf(groupService.countGroups()));
		oxtrustStatObject.setPersonCount(String.valueOf(personService.countPersons()));
		log.debug("Setting FactorAttributes");
		setFactorAttributes(configurationStatus, oxtrustStatObject);
		// Execute df and update configuration attributes
		setDfAttributes(configurationStatus, oxtrustStatObject);
		// Set HTTPD attributes
		setHttpdAttributes(configurationStatus);
		try {
			setCertificateExpiryAttributes(configurationStatus);
		} catch (Exception ex) {
			log.error("Failed to check certificate expiration", ex);
		}

		GluuConfiguration configuration = configurationService.getConfiguration();
		GluuOxTrustStat gluuOxTrustStat = configurationService.getOxtrustStat();
		try {
			// Copy gathered values
			BeanUtils.copyProperties(configuration, configurationStatus);
			BeanUtils.copyProperties(gluuOxTrustStat, oxtrustStatObject);
		} catch (Exception ex) {
			log.error("Failed to copy status attributes", ex);
		}

		Date currentDateTime = new Date();
		configuration.setLastUpdate(currentDateTime);
		configurationService.updateConfiguration(configuration);
		configurationService.updateOxtrustStat(gluuOxTrustStat);
		if (centralLdapService.isUseCentralServer()) {
			try {
				boolean existConfiguration = centralLdapService.containsConfiguration(configuration.getDn());
				if (existConfiguration) {
					centralLdapService.updateConfiguration(configuration);
				} else {
					centralLdapService.addConfiguration(configuration);
				}
			} catch (BasePersistenceException ex) {
				log.error("Failed to update configuration at central server", ex);
				return;
			}
			try {
				boolean existConfiguration = centralLdapService.containsOxtrustStatForToday(gluuOxTrustStat.getDn());
				if (existConfiguration) {
					centralLdapService.updateOxtrustStat(gluuOxTrustStat);
				} else {
					centralLdapService.addOxtrustStat(gluuOxTrustStat);
				}
			} catch (BasePersistenceException ex) {
				log.error("Failed to update configuration at central server", ex);
				return;
			}
		}

		log.debug("Configuration status update finished");
	}

	private void setCertificateExpiryAttributes(ConfigurationStatus configuration) {
		try {
			URL destinationURL = new URL(appConfiguration.getApplicationUrl());
			HttpsURLConnection conn = (HttpsURLConnection) destinationURL.openConnection();
			conn.connect();
			Certificate[] certs = conn.getServerCertificates();
			if (certs.length > 0) {
				if (certs[0] instanceof X509Certificate) {
					X509Certificate x509Certificate = (X509Certificate) certs[0];
					Date expirationDate = x509Certificate.getNotAfter();
					long expiresAfter = TimeUnit.MILLISECONDS.toDays(expirationDate.getTime() - new Date().getTime());
					configuration.setSslExpiry(toIntString(expiresAfter));
				}
			}
		} catch (IOException e) {
			log.error("Can not download ssl certificate", e);
		}
	}

	private void setHttpdAttributes(ConfigurationStatus configuration) {
		log.debug("Setting httpd attributes");
		AppConfiguration appConfiguration = configurationFactory.getAppConfiguration();
		String page = getHttpdPage(appConfiguration.getIdpUrl(), OxTrustConstants.HTTPD_TEST_PAGE_NAME);
		configuration.setGluuHttpStatus(Boolean.toString(OxTrustConstants.HTTPD_TEST_PAGE_CONTENT.equals(page)));

	}

	private String getHttpdPage(String idpUrl, String httpdTestPageName) {
		String[] urlParts = idpUrl.split("://");
		if ("https".equals(urlParts[0])) {
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}
			} };

			// Install the all-trusting trust manager
			try {
				SSLContext sc = SSLContext.getInstance("SSL");
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			} catch (Exception e) {
			}
		}

		StringBuilder sb = new StringBuilder();
		// Now you can access an https URL without having the certificate in the
		// truststore
		try {
			String[] hostAndPort = urlParts[1].split(":");
			URL url = null;
			if (hostAndPort.length < 2) {
				url = new URL(urlParts[0], hostAndPort[0], httpdTestPageName);
			} else {
				url = new URL(urlParts[0], hostAndPort[0], Integer.parseInt(hostAndPort[1]), httpdTestPageName);
			}
			InputStream in = url.openConnection().getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			in.close();

		} catch (Exception e) {
			// log.error("Failed to get test page: ", e);
		}
		return sb.toString();
	}

	@SuppressWarnings("deprecation")
	private void setFactorAttributes(ConfigurationStatus configuration, OxtrustStat oxtrustStat) {
		if (!isLinux()) {
			return;
		}
		CommandLine commandLine = new CommandLine(OxTrustConstants.PROGRAM_FACTER);
		String facterVersion = getFacterVersion();
		boolean isOldVersion = false;
		log.debug("Facter version: " + facterVersion);
		String resultOutput;
		if (facterVersion == null) {
			return;
		}
		if (Integer.valueOf(facterVersion.substring(0, 1)) <= 1) {
			isOldVersion = true;
		}
		if (Integer.valueOf(facterVersion.substring(0, 1)) >= 3) {
			log.debug("Running facter in legacy mode");
			commandLine.addArgument("--show-legacy");
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
		try {
			boolean result = ProcessHelper.executeProgram(commandLine, false, 0, bos);
			if (!result) {
				return;
			}
			resultOutput = new String(bos.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			log.error("Failed to parse program {} output", OxTrustConstants.PROGRAM_FACTER, ex);
			return;
		} finally {
			IOUtils.closeQuietly(bos);
		}
		String[] outputLines = resultOutput.split("\\r?\\n");

		if (isOldVersion) {
			oxtrustStat.setFreeMemory(getFreeMemory(outputLines, OxTrustConstants.FACTER_FREE_MEMORY,
					OxTrustConstants.FACTER_MEMORY_SIZE));
		} else {
			oxtrustStat.setFreeMemory(getFreeMemory(outputLines, OxTrustConstants.FACTER_FREE_MEMORY_MB,
					OxTrustConstants.FACTER_MEMORY_SIZE_MB));
		}
		oxtrustStat.setFreeSwap(toIntString(getFacterPercentResult(outputLines, OxTrustConstants.FACTER_FREE_SWAP,
				OxTrustConstants.FACTER_FREE_SWAP_TOTAL)));
		String hostname = "";
		try {
			hostname = Files.readAllLines(Paths.get("/install/community-edition-setup/output/hostname")).get(0);
		} catch (IOException e) {
			log.trace("+++++++++++++++++++++++++++++++++: '{}'", "Error reading hostname from file");
		}
		if (hostname.equalsIgnoreCase("localhost")) {
			hostname = getFacterResult(outputLines, OxTrustConstants.FACTER_HOST_NAME);
		}
		configuration.setHostname(hostname);
		oxtrustStat.setIpAddress(getFacterResult(outputLines, OxTrustConstants.FACTER_IP_ADDRESS));
		oxtrustStat.setLoadAvg(getFacterResult(outputLines, OxTrustConstants.FACTER_LOAD_AVERAGE));
		getFacterBandwidth(getFacterResult(outputLines, OxTrustConstants.FACTER_BANDWIDTH_USAGE), configuration);
		oxtrustStat.setSystemUptime(getFacterResult(outputLines, OxTrustConstants.FACTER_SYSTEM_UP_TIME));
	}

	@SuppressWarnings("deprecation")
	private String getFacterVersion() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
		try {
			CommandLine commandLine = new CommandLine(OxTrustConstants.PROGRAM_FACTER);
			commandLine.addArgument("--version");
			ProcessHelper.executeProgram(commandLine, false, 0, bos);
			return new String(bos.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			log.error("Failed to parse program {} output", OxTrustConstants.PROGRAM_FACTER, ex);
			return null;
		} finally {
			IOUtils.closeQuietly(bos);
		}

	}

	private void getFacterBandwidth(String facterResult, ConfigurationStatus configuration) {
		log.debug("Setting bandwidth attributes");
		if (facterResult != null) {
			String[] lines = facterResult.split("\n");
			SimpleDateFormat monthFormat = new SimpleDateFormat("MMM ''yy");
			String month = monthFormat.format(new Date());
			Pattern curent = Pattern.compile("^\\s*" + month);

			for (String line : lines) {
				Matcher match = curent.matcher(line);
				if (match.find()) {
					line = line.replaceAll("^\\s*" + month, "");
					String[] values = line.split("\\|");
					configuration.setGluuBandwidthRX(values[0].replaceAll("^\\s*", "").replaceAll("\\s*$", ""));
					configuration.setGluuBandwidthTX(values[1].replaceAll("^\\s*", "").replaceAll("\\s*$", ""));
				}
			}
		} else {
			configuration.setGluuBandwidthRX("-1");
			configuration.setGluuBandwidthTX("-1");
		}

	}

	@SuppressWarnings("deprecation")
	private void setDfAttributes(ConfigurationStatus configuration, OxtrustStat oxtrustStat) {
		log.debug("Setting df attributes");
		// Run df only on linux
		if (!isLinux()) {
			return;
		}

		String programPath = OxTrustConstants.PROGRAM_DF;
		CommandLine commandLine = new CommandLine(programPath);
		commandLine.addArgument("/");
		commandLine.addArgument("--human-readable");

		ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
		try {
			boolean result = ProcessHelper.executeProgram(commandLine, false, 0, bos);
			if (!result) {
				return;
			}
		} finally {
			IOUtils.closeQuietly(bos);
		}

		String resultOutput = null;
		try {
			resultOutput = new String(bos.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			log.error("Failed to parse program {} output", programPath, ex);
			return;
		}

		String[] outputLines = resultOutput.split("\\r?\\n");
		String[] outputValues = null;
		if (outputLines.length == 2) {
			outputValues = outputLines[1].split("\\s+");
		} else if (outputLines.length == 3) {
			outputValues = outputLines[2].split("\\s+");
		}

		if (outputValues != null) {
			if (outputValues.length < 6) {
				return;
			}

			Number usedDiskSpace = getNumber(outputValues[4]);
			Number freeDiskSpace = usedDiskSpace == null ? null : 100 - usedDiskSpace.doubleValue();
			oxtrustStat.setFreeDiskSpace(toIntString(freeDiskSpace));
		}
	}

	private boolean isLinux() {
		String osName = System.getProperty("os.name");
		return !StringHelper.isEmpty(osName) && osName.toLowerCase().contains("linux");
	}

	private String getFacterResult(String[] lines, String param) {
		log.debug("Setting facter param: " + param);
		String paramPattern = param + OxTrustConstants.FACTER_PARAM_VALUE_DIVIDER;
		boolean valueStarted = false;
		String value = "";
		List<String> facterLines = Arrays.asList(lines);
		Iterator<String> lineIterator = facterLines.iterator();
		while (lineIterator.hasNext()) {
			String line = lineIterator.next();

			if (!valueStarted) { // start searching for the line with param
				if (line.startsWith(paramPattern)) {
					valueStarted = true;
					int index = line.indexOf(OxTrustConstants.FACTER_PARAM_VALUE_DIVIDER);
					if (index > -1) {
						value = line.substring(index + OxTrustConstants.FACTER_PARAM_VALUE_DIVIDER.length());
					}
				} else {
					continue;
				}

				log.debug(line);

			} else { // check if there are any additional lines
				int index = line.indexOf(OxTrustConstants.FACTER_PARAM_VALUE_DIVIDER);
				if (index == -1) { // this line has no value name, so it must be continuation of the previous
									// value.
					value += "\n" + line;
				} else { // this line has it's own value, so the value we were looking for has ended.
					valueStarted = false;
					continue;
				}
			}
		}

		return value.equals("") ? null : value;
	}

	private Number getFacterNumberResult(String[] lines, String param) {
		String value = getFacterResult(lines, param);
		if (value == null) {
			return null;
		}

		return getNumber(value);
	}

	private Number getFacterPercentResult(String[] lines, String paramValue, String paramTotal) {
		Number value = getFacterNumberResult(lines, paramValue);
		Number total = getFacterNumberResult(lines, paramTotal);
		return getNumber(value, total);
	}

	private String getFreeMemory(String[] lines, String paramValue, String paramTotal) {
		Number value = getFacterNumberResult(lines, paramValue);
		Number total = getFacterNumberResult(lines, paramTotal);
		double result = (value.doubleValue() / total.doubleValue()) * 100;
		return String.format("%.2f", result);

	}

	private Number getNumber(String value) {
		int multiplier = 1;
		if (value.contains("KB")) {
			multiplier = 1024;
		} else if (value.contains("MB")) {
			multiplier = 1024 * 1024;
		} else if (value.contains("GB")) {
			multiplier = 1024 * 1024 * 1024;
		}

		try {
			return multiplier * numberFormat.parse(value).doubleValue();
		} catch (ParseException ex) {
		}

		return null;
	}

	private Number getNumber(Number value, Number total) {
		if ((value == null) || (total == null) || (value.doubleValue() == 0.0d) || (total.doubleValue() == 0.0)) {
			return null;
		}

		return NumberHelper.round(value.doubleValue() / total.doubleValue(), 2) * 100;
	}

	private String toIntString(Number number) {
		return (number == null) ? null : String.valueOf(number.intValue());
	}

}
