/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.IOUtils;
import org.gluu.oxtrust.config.OxTrustConfiguration;
import org.gluu.oxtrust.model.GluuAppliance;
import org.gluu.oxtrust.util.NumberHelper;
import org.gluu.oxtrust.util.OxTrustConstants;
import org.gluu.site.ldap.persistence.exception.LdapMappingException;
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
import org.xdi.config.oxtrust.ApplicationConfiguration;
import org.xdi.util.ArrayHelper;
import org.xdi.util.StringHelper;
import org.xdi.util.process.ProcessHelper;

/**
 * Gather periodically site and server status
 * 
 * @author Yuriy Movchan Date: 11.22.2010
 */
@AutoCreate
@Scope(ScopeType.APPLICATION)
@Name("statusCheckerTimer")
public class StatusCheckerTimer {

	@Logger
	private Log log;

	@In
	private ApplianceService applianceService;

	@In
	private GroupService groupService;

	@In
	private PersonService personService;

	@In
	private CentralLdapService centralLdapService;

	@In
	private OxTrustConfiguration oxTrustConfiguration;

	private NumberFormat numberFormat;

	@Create
	public void create() {
		this.numberFormat = NumberFormat.getNumberInstance();
	}

	@Asynchronous
	public QuartzTriggerHandle scheduleStatusChecking(@Expiration Date when, @IntervalDuration Long interval) {
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
		log.debug("Starting update of appliance status");
		ApplicationConfiguration applicationConfiguration = oxTrustConfiguration.getApplicationConfiguration();
		if (!applicationConfiguration.isUpdateApplianceStatus()) {
			return;
		}

		GluuAppliance appliance;
		try {
			appliance = applianceService.getAppliance();
		} catch (LdapMappingException ex) {
			log.error("Failed to load current appliance", ex);
			return;
		}

		// Execute facter and update appliance attributes
		setFactorAttributes(appliance);

		// Execute df and update appliance attributes
		setDfAttributes(appliance);

		// Set HTTPD attributes
		setHttpdAttributes(appliance);

		setCertificateExpiryAttributes(appliance);

//		setVDSAttributes(appliance);

		appliance.setLastUpdate(toIntString(System.currentTimeMillis() / 1000));

		try {
			applianceService.updateAppliance(appliance);
		} catch (LdapMappingException ex) {
			log.error("Failed to update current appliance", ex);
			return;
		}

		if (centralLdapService.isUseCentralServer()) {
			try {
				GluuAppliance tmpAppliance = new GluuAppliance();
				tmpAppliance.setDn(appliance.getDn());
				boolean existAppliance = centralLdapService.containsAppliance(tmpAppliance);
	
				if (existAppliance) {
					centralLdapService.updateAppliance(appliance);
				} else {
					centralLdapService.addAppliance(appliance);
				}
			} catch (LdapMappingException ex) {
				log.error("Failed to update appliance at central server", ex);
				return;
			}
		}

		log.debug("Appliance status update finished");
	}

	private void setCertificateExpiryAttributes(GluuAppliance appliance) {
		if (isLinux()) {
			String programPath = OxTrustConstants.PROGRAM_CHECK_SSL;

			String[] outputLines = runCheck(programPath);
			int expiresAfter = -1;
			if (ArrayHelper.isEmpty(outputLines) || (outputLines.length < 1) || StringHelper.isEmpty(outputLines[1]) || outputLines[1].startsWith("SSL_CERT CRITICAL")) {
				String message = String.format("%s retuned an unexpected value", programPath);
				log.error(message);
			} else {
				try {
					if (outputLines.length == 1) {
						expiresAfter = Integer.parseInt(outputLines[0]);
					} else {
						String message = String.format("%s retuned an unexpected " + "number of lines", programPath);
						log.error(message);
					}
				} catch (NumberFormatException e) {
					String message = String.format("%s retuned an unexpected value", programPath);
					log.error(message);
				}
			}
			appliance.setSslExpiry(toIntString(expiresAfter));
		}
	}

	private String[] runCheck(String programPath) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
		boolean result = false;
		try {
			result 
				= ProcessHelper.executeProgram(programPath, false, 0, bos);
		} catch (Exception e) {
			String message = String.format("Failed to run %s : %s", 
											programPath, 
											e.getMessage());
			log.error(message);
		} finally {
			IOUtils.closeQuietly(bos);
		}
		
		String[] outputLines = null;
		if (result) {
			String resultOutput = null;
			try {
				resultOutput = new String(bos.toByteArray(), "UTF-8");
			} catch (UnsupportedEncodingException ex) {
				String message 
					= String.format("Failed to parse program %s output", 
									programPath);
				log.error(message, ex);
			}

			if(resultOutput!=null){
				outputLines = resultOutput.split("\\r?\\n");
			}
		}else{
			String message 
				= String.format("There was an error executing command %s", 
							programPath);
			log.error(message);			
		}
		return outputLines;
		
	}

	private void setHttpdAttributes(GluuAppliance appliance) {
		log.debug("Setting httpd attributes");
		ApplicationConfiguration applicationConfiguration = oxTrustConfiguration.getApplicationConfiguration();
		String page = getHttpdPage(applicationConfiguration.getIdpUrl(), OxTrustConstants.HTTPD_TEST_PAGE_NAME);
		appliance.setGluuHttpStatus(Boolean.toString(OxTrustConstants.HTTPD_TEST_PAGE_CONTENT.equals(page)));

	}
/*
	private void setVDSAttributes(GluuAppliance appliance) {
		log.debug("Setting VDS attributes");
		ApplicationConfiguration applicationConfiguration = oxTrustConfiguration.getApplicationConfiguration();
		// Run vds check only on if vds.test.filter is set
		if (applicationConfiguration.getVdsFilter() == null) {
			return;
		}
		String serverURL = applicationConfiguration.getVdsLdapServer().split(":")[0];
		int serverPort = Integer.parseInt(applicationConfiguration.getVdsLdapServer().split(":")[1]);
		String bindDN = applicationConfiguration.getVdsBindDn();

		String bindPassword = null;
		try {
			bindPassword = StringEncrypter.defaultInstance().decrypt(applicationConfiguration.getVdsBindPassword());
		} catch (EncryptionException e1) {
			log.error("Failed to decrypt VDS bind password: %s", e1.getMessage());
		}

		String vdsFilter = applicationConfiguration.getVdsFilter();
		String baseDN = applicationConfiguration.getBaseDN();

		LDAPConnectionPool connectionPool = null;
		String[] objectclasses = null;
		try {
			ServerSet vdsServerSet;
			boolean useSSL = "ldaps".equals(applicationConfiguration.getVdsLdapProtocol());
			if (useSSL) {
				SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
				vdsServerSet = new SingleServerSet(serverURL, serverPort, sslUtil.createSSLSocketFactory());
			} else {
				vdsServerSet = new SingleServerSet(serverURL, serverPort);
			}
			SimpleBindRequest bindRequest = new SimpleBindRequest(bindDN, bindPassword);
			connectionPool = new LDAPConnectionPool(vdsServerSet, bindRequest, 10);
			SearchResult entry = connectionPool.search(baseDN, SearchScope.BASE, vdsFilter);
			objectclasses = entry.getSearchEntries().get(0).getAttribute("objectclass").getValues();

		} catch (Exception e) {
			appliance.setGluuVDSStatus(Boolean.toString(false));
			log.error(String.format("Failed to get objectclass values from VDS. error: %s", e.getMessage()));
		} finally {
			if (connectionPool != null) {
				connectionPool.close();
			} else {
				log.error(String.format("Unable to connect to VDS server. Is it running on %s:%s ?", serverURL, serverPort));
			}

		}
		boolean topPresent = false;
		boolean vdapcontainerPresent = false;
		boolean vdlabelPresent = false;
		boolean vdDirectoryViewPresent = false;
		if (objectclasses != null && objectclasses.length > 0) {
			Arrays.sort(objectclasses);
			topPresent = Arrays.binarySearch(objectclasses, "top") >= 0;
			vdapcontainerPresent = Arrays.binarySearch(objectclasses, "vdapcontainer") >= 0;
			vdlabelPresent = Arrays.binarySearch(objectclasses, "vdlabel") >= 0;
			vdDirectoryViewPresent = Arrays.binarySearch(objectclasses, "vdDirectoryView") >= 0;
		}
		appliance.setGluuVDSStatus(Boolean.toString(topPresent && vdapcontainerPresent && vdlabelPresent && vdDirectoryViewPresent));
	}
*/
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

	private void setFactorAttributes(GluuAppliance appliance) {
		log.debug("Setting facter attributes");
		// Run facter only on linux
		if (!isLinux()) {
			return;
		}

		String programPath = OxTrustConstants.PROGRAM_FACTER;

		ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);
		try {
			boolean result = ProcessHelper.executeProgram(programPath, false, 0, bos);
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
			log.error("Failed to parse program {0} output", ex, programPath);
			return;
		}

		String[] outputLines = resultOutput.split("\\r?\\n");

		// Update appliance attributes
		appliance.setFreeMemory(toIntString(getFacterPercentResult(outputLines, OxTrustConstants.FACTER_FREE_MEMORY,
				OxTrustConstants.FACTER_FREE_MEMORY_TOTAL)));
		appliance.setFreeSwap(toIntString(getFacterPercentResult(outputLines, OxTrustConstants.FACTER_FREE_SWAP,
				OxTrustConstants.FACTER_FREE_SWAP_TOTAL)));
		appliance.setHostname(getFacterResult(outputLines, OxTrustConstants.FACTER_HOST_NAME));
		appliance.setIpAddress(getFacterResult(outputLines, OxTrustConstants.FACTER_IP_ADDRESS));

		appliance.setLoadAvg(getFacterResult(outputLines, OxTrustConstants.FACTER_LOAD_AVERAGE));

		getFacterBandwidth(getFacterResult(outputLines, OxTrustConstants.FACTER_BANDWIDTH_USAGE), appliance);
		appliance.setSystemUptime(getFacterResult(outputLines, OxTrustConstants.FACTER_SYSTEM_UP_TIME));
	}

	private void getFacterBandwidth(String facterResult, GluuAppliance appliance) {
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
					appliance.setGluuBandwidthRX(values[0].replaceAll("^\\s*", "").replaceAll("\\s*$", ""));
					appliance.setGluuBandwidthTX(values[1].replaceAll("^\\s*", "").replaceAll("\\s*$", ""));
				}
			}
		} else {
			appliance.setGluuBandwidthRX("-1");
			appliance.setGluuBandwidthTX("-1");
		}

	}

	private void setDfAttributes(GluuAppliance appliance) {
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
			log.error("Failed to parse program {0} output", ex, programPath);
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

			// Update appliance attributes
			appliance.setFreeDiskSpace(toIntString(freeDiskSpace));
		}
	}

	private boolean isLinux() {
		String osName = System.getProperty("os.name");
		return !StringHelper.isEmpty(osName) && osName.toLowerCase().contains("linux");
	}

	private String getFacterResult(String[] lines, String param) {
		log.debug("Setting facter param: " + param);
		boolean valueStarted = false;
		String value = "";
		List<String> facterLines = Arrays.asList(lines);
		Iterator<String> lineIterator = facterLines.iterator();
		while (lineIterator.hasNext()) {
			String line = lineIterator.next();

			if (!valueStarted) { // start searching for the line with param
				if (line.startsWith(param)) {
					valueStarted = true;
					int index = line.indexOf(OxTrustConstants.FACTER_PARAM_VALUE_DIVIDER);
					if (index > -1) {
						value = line.substring(index + OxTrustConstants.FACTER_PARAM_VALUE_DIVIDER.length());
					}
				} else {
					continue;
				}

				log.debug(line);

			} else { //check if there are any additional lines
				int index = line.indexOf(OxTrustConstants.FACTER_PARAM_VALUE_DIVIDER);
				if (index == -1) { // this line has no value name, so it must be continuation of the previous value.
					value += "\n" + line;
				}else{ // this line has it's own value, so the value we were looking for has ended. 
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

	private Number getNumber(String value) {
		try {
			return numberFormat.parse(value);
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
