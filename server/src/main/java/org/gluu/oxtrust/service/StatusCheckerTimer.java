/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
import org.gluu.config.oxtrust.AppConfiguration;
import org.gluu.oxtrust.model.FacterData;
import org.gluu.oxtrust.model.GluuConfiguration;
import org.gluu.oxtrust.model.GluuOxTrustStat;
import org.gluu.oxtrust.model.status.ConfigurationStatus;
import org.gluu.oxtrust.model.status.OxtrustStat;
import org.gluu.oxtrust.service.cdi.event.StatusCheckerTimerEvent;
import org.gluu.oxtrust.service.config.ConfigurationFactory;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.service.cdi.async.Asynchronous;
import org.gluu.service.cdi.event.Scheduled;
import org.gluu.service.timer.event.TimerEvent;
import org.gluu.service.timer.schedule.TimerSchedule;
import org.gluu.util.StringHelper;
import org.gluu.util.process.ProcessHelper;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Gather periodically site and server status
 * 
 * @author Yuriy Movchan Date: 11.22.2010
 */
@ApplicationScoped
@Named
public class StatusCheckerTimer {

	private final static int DEFAULT_INTERVAL =  5 * 60; // 1 minute

	@Inject
	private Logger log;

	@Inject
	private Event<TimerEvent> timerEvent;

	@Inject
	private ConfigurationService configurationService;

	@Inject
	private ConfigurationFactory configurationFactory;

	@Inject
	private AppConfiguration appConfiguration;

	private AtomicBoolean isActive;

	@Inject
	private IGroupService groupService;

	@Inject
	private IPersonService personService;

	@PostConstruct
	public void create() {
	}

	public void initTimer() {
		log.debug("Initializing Daily Status Cheker Timer");
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
		FacterData facterData = getFacterData();
		configurationStatus.setHostname(facterData.getHostname());
		oxtrustStatObject.setIpAddress(facterData.getIpaddress());
		oxtrustStatObject.setLoadAvg(facterData.getLoadAverage());
		oxtrustStatObject.setSystemUptime(facterData.getUptime());
		oxtrustStatObject.setFreeDiskSpace(facterData.getFreeDiskSpace());
		oxtrustStatObject.setFreeMemory(facterData.getMemoryfree());
		setHttpdAttributes(configurationStatus);
		try {
			setCertificateExpiryAttributes(configurationStatus);
		} catch (Exception ex) {
			log.error("Failed to check certificate expiration", ex);
		}
		GluuConfiguration configuration = configurationService.getConfiguration();
		GluuOxTrustStat gluuOxTrustStat = configurationService.getOxtrustStat();
		try {
			BeanUtils.copyProperties(configuration, configurationStatus);
			BeanUtils.copyProperties(gluuOxTrustStat, oxtrustStatObject);
		} catch (Exception ex) {
			log.error("Failed to copy status attributes", ex);
		}
		Date currentDateTime = new Date();
		configuration.setLastUpdate(currentDateTime);
		configurationService.updateConfiguration(configuration);
		configurationService.updateOxtrustStat(gluuOxTrustStat);
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

	private FacterData getFacterData() {
		FacterData facterData = new FacterData();
		ObjectMapper mapper = new ObjectMapper();
		if (!isLinux()) {
			return facterData;
		}
		CommandLine commandLine = new CommandLine(OxTrustConstants.PROGRAM_FACTER);
		commandLine.addArgument("-j");
		String resultOutput;
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);) {
			boolean result = ProcessHelper.executeProgram(commandLine, false, 0, bos);
			if (!result) {
				return facterData;
			}
			resultOutput = new String(bos.toByteArray(), "UTF-8");
			facterData = mapper.readValue(resultOutput, FacterData.class);
		} catch (UnsupportedEncodingException ex) {
			log.error("Failed to parse program {} output", OxTrustConstants.PROGRAM_FACTER, ex);
			return facterData;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return facterData;
	}

	private boolean isLinux() {
		String osName = System.getProperty("os.name");
		return !StringHelper.isEmpty(osName) && osName.toLowerCase().contains("linux");
	}

	private String toIntString(Number number) {
		return (number == null) ? null : String.valueOf(number.intValue());
	}

}
