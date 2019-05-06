/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.push;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Push pairing device configuration for platform
 * 
 * @author Yuriy Movchan Date: 02/03/2014
 */
@JsonPropertyOrder({ "device_uuid", "device_token", "device_type", "device_name", "os_name", "os_version", "approve_time", "approve_ip" })
public class PushDeviceConfiguration implements Serializable {

	private static final long serialVersionUID = 2208826784937052508L;

	@JsonProperty("device_uuid")
	private String deviceUuid;

	@JsonProperty("device_token")
	private String deviceToken;

	@JsonProperty("device_type")
	private String deviceType;

	@JsonProperty("device_name")
	private String deviceName;

	@JsonProperty("os_name")
	private String osName;

	@JsonProperty("os_version")
	private String osVersion;

	@JsonProperty("approve_time")
	private Date approvedTime;

	@JsonProperty("approve_ip")
	private String approvedIp;

	public String getDeviceUuid() {
		return deviceUuid;
	}

	public void setDeviceUuid(String deviceUuid) {
		this.deviceUuid = deviceUuid;
	}

	public String getDeviceToken() {
		return deviceToken;
	}

	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getOsName() {
		return osName;
	}

	public void setOsName(String osName) {
		this.osName = osName;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public Date getApprovedTime() {
		return approvedTime;
	}

	public void setApprovedTime(Date approvedTime) {
		this.approvedTime = approvedTime;
	}

	public String getApprovedIp() {
		return approvedIp;
	}

	public void setApprovedIp(String approvedIp) {
		this.approvedIp = approvedIp;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PushDeviceConfiguration [deviceUuid=").append(deviceUuid).append(", deviceToken=").append(deviceToken)
				.append(", deviceType=").append(deviceType).append(", deviceName=").append(deviceName).append(", osName=").append(osName)
				.append(", osVersion=").append(osVersion).append(", approvedTime=").append(approvedTime).append(", approvedIp=")
				.append(approvedIp).append("]");
		return builder.toString();
	}

}
