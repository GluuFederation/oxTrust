package org.gluu.oxtrust.api.server.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RptConfig implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3895525764368865825L;
	
	@JsonProperty("rptConnectionPoolUseConnectionPooling")
	private Boolean rptConnectionPoolUseConnectionPooling;
	@JsonProperty("rptConnectionPoolMaxTotal")
	private Integer rptConnectionPoolMaxTotal;
	@JsonProperty("rptConnectionPoolDefaultMaxPerRoute")
	private Integer rptConnectionPoolDefaultMaxPerRoute;
	@JsonProperty("rptConnectionPoolValidateAfterInactivity")
	private Integer rptConnectionPoolValidateAfterInactivity;
	@JsonProperty("rptConnectionPoolCustomKeepAliveTimeout")
	private Integer rptConnectionPoolCustomKeepAliveTimeout;
	
	@JsonProperty("rptConnectionPoolMaxTotal")
	public void setRptConnectionPoolMaxTotal(Integer rptConnectionPoolMaxTotal) {
		this.rptConnectionPoolMaxTotal = rptConnectionPoolMaxTotal;
	}

	@JsonProperty("rptConnectionPoolDefaultMaxPerRoute")
	public Integer getRptConnectionPoolDefaultMaxPerRoute() {
		return rptConnectionPoolDefaultMaxPerRoute;
	}

	@JsonProperty("rptConnectionPoolDefaultMaxPerRoute")
	public void setRptConnectionPoolDefaultMaxPerRoute(Integer rptConnectionPoolDefaultMaxPerRoute) {
		this.rptConnectionPoolDefaultMaxPerRoute = rptConnectionPoolDefaultMaxPerRoute;
	}

	@JsonProperty("rptConnectionPoolValidateAfterInactivity")
	public Integer getRptConnectionPoolValidateAfterInactivity() {
		return rptConnectionPoolValidateAfterInactivity;
	}

	@JsonProperty("rptConnectionPoolValidateAfterInactivity")
	public void setRptConnectionPoolValidateAfterInactivity(Integer rptConnectionPoolValidateAfterInactivity) {
		this.rptConnectionPoolValidateAfterInactivity = rptConnectionPoolValidateAfterInactivity;
	}

	@JsonProperty("rptConnectionPoolCustomKeepAliveTimeout")
	public Integer getRptConnectionPoolCustomKeepAliveTimeout() {
		return rptConnectionPoolCustomKeepAliveTimeout;
	}

	@JsonProperty("rptConnectionPoolCustomKeepAliveTimeout")
	public void setRptConnectionPoolCustomKeepAliveTimeout(Integer rptConnectionPoolCustomKeepAliveTimeout) {
		this.rptConnectionPoolCustomKeepAliveTimeout = rptConnectionPoolCustomKeepAliveTimeout;
	}
	@JsonProperty("rptConnectionPoolUseConnectionPooling")
	public Boolean getRptConnectionPoolUseConnectionPooling() {
		return rptConnectionPoolUseConnectionPooling;
	}

	@JsonProperty("rptConnectionPoolUseConnectionPooling")
	public void setRptConnectionPoolUseConnectionPooling(Boolean rptConnectionPoolUseConnectionPooling) {
		this.rptConnectionPoolUseConnectionPooling = rptConnectionPoolUseConnectionPooling;
	}

	@JsonProperty("rptConnectionPoolMaxTotal")
	public Integer getRptConnectionPoolMaxTotal() {
		return rptConnectionPoolMaxTotal;
	}

}
