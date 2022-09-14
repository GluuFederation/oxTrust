package org.gluu.oxtrust.api.server.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetricConfig implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5272812235464652897L;
	@JsonProperty("metricReporterInterval")
	private Integer metricReporterInterval;
	@JsonProperty("metricReporterKeepDataDays")
	private Integer metricReporterKeepDataDays;
	@JsonProperty("metricReporterEnabled")
	private Boolean metricReporterEnabled;
	
	
	
	@JsonProperty("metricReporterInterval")
	public Integer getMetricReporterInterval() {
		return metricReporterInterval;
	}

	@JsonProperty("metricReporterInterval")
	public void setMetricReporterInterval(Integer metricReporterInterval) {
		this.metricReporterInterval = metricReporterInterval;
	}

	@JsonProperty("metricReporterKeepDataDays")
	public Integer getMetricReporterKeepDataDays() {
		return metricReporterKeepDataDays;
	}

	@JsonProperty("metricReporterKeepDataDays")
	public void setMetricReporterKeepDataDays(Integer metricReporterKeepDataDays) {
		this.metricReporterKeepDataDays = metricReporterKeepDataDays;
	}

	@JsonProperty("metricReporterEnabled")
	public Boolean getMetricReporterEnabled() {
		return metricReporterEnabled;
	}

	@JsonProperty("metricReporterEnabled")
	public void setMetricReporterEnabled(Boolean metricReporterEnabled) {
		this.metricReporterEnabled = metricReporterEnabled;
	}


}
