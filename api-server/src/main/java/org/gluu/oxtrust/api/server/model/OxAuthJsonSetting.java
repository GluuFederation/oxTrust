package org.gluu.oxtrust.api.server.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OxAuthJsonSetting implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8002416610811584319L;

	private boolean disableFido2;
	private boolean userAutoEnrollment;
	private boolean metricReporterEnabled;
	private int authenticationHistoryExpiration;
	private int unfinishedRequestExpiration;
	private int metricReporterInterval;
	private int metricReporterKeepDataDays;
	private int cleanServiceInterval;
	private String loggingLevel;

	public boolean isDisableFido2() {
		return disableFido2;
	}

	public void setDisableFido2(boolean disableFido2) {
		this.disableFido2 = disableFido2;
	}

	public boolean isUserAutoEnrollment() {
		return userAutoEnrollment;
	}

	public void setUserAutoEnrollment(boolean userAutoEnrollment) {
		this.userAutoEnrollment = userAutoEnrollment;
	}

	public boolean isMetricReporterEnabled() {
		return metricReporterEnabled;
	}

	public void setMetricReporterEnabled(boolean metricReporterEnabled) {
		this.metricReporterEnabled = metricReporterEnabled;
	}

	public int getAuthenticationHistoryExpiration() {
		return authenticationHistoryExpiration;
	}

	public void setAuthenticationHistoryExpiration(int authenticationHistoryExpiration) {
		this.authenticationHistoryExpiration = authenticationHistoryExpiration;
	}

	public int getUnfinishedRequestExpiration() {
		return unfinishedRequestExpiration;
	}

	public void setUnfinishedRequestExpiration(int unfinishedRequestExpiration) {
		this.unfinishedRequestExpiration = unfinishedRequestExpiration;
	}

	public int getMetricReporterInterval() {
		return metricReporterInterval;
	}

	public void setMetricReporterInterval(int metricReporterInterval) {
		this.metricReporterInterval = metricReporterInterval;
	}

	public int getMetricReporterKeepDataDays() {
		return metricReporterKeepDataDays;
	}

	public void setMetricReporterKeepDataDays(int metricReporterKeepDataDays) {
		this.metricReporterKeepDataDays = metricReporterKeepDataDays;
	}

	public int getCleanServiceInterval() {
		return cleanServiceInterval;
	}

	public void setCleanServiceInterval(int cleanServiceInterval) {
		this.cleanServiceInterval = cleanServiceInterval;
	}

	public String getLoggingLevel() {
		return loggingLevel;
	}

	public void setLoggingLevel(String loggingLevel) {
		this.loggingLevel = loggingLevel;
	}

	@Override
	public String toString() {
		return "OxAuthJsonSetting [disableFido2=" + disableFido2 + ", userAutoEnrollment=" + userAutoEnrollment
				+ ", metricReporterEnabled=" + metricReporterEnabled + ", authenticationHistoryExpiration="
				+ authenticationHistoryExpiration + ", unfinishedRequestExpiration=" + unfinishedRequestExpiration
				+ ", metricReporterInterval=" + metricReporterInterval + ", metricReporterKeepDataDays="
				+ metricReporterKeepDataDays + ", cleanServiceInterval=" + cleanServiceInterval + ", loggingLevel="
				+ loggingLevel + "]";
	}

}
