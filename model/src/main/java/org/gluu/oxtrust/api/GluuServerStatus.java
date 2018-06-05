package org.gluu.oxtrust.api;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "gluuperson")
public class GluuServerStatus {

	private String hostname;
	private String ipAddress;
	private String uptime;
	private Date lastUpdate;
	private String pollingInterval;
	private String personCount;
	private String groupCount;
	private String freeMemory;
	private String freeDiskSpace;

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getUptime() {
		return uptime;
	}

	public void setUptime(String uptime) {
		this.uptime = uptime;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date date) {
		this.lastUpdate = date;
	}

	public String getPollingInterval() {
		return pollingInterval;
	}

	public void setPollingInterval(String pollingInterval) {
		this.pollingInterval = pollingInterval;
	}

	public String getPersonCount() {
		return personCount;
	}

	public void setPersonCount(String personCount) {
		this.personCount = personCount;
	}

	public String getGroupCount() {
		return groupCount;
	}

	public void setGroupCount(String groupCount) {
		this.groupCount = groupCount;
	}

	public String getFreeMemory() {
		return freeMemory;
	}

	public void setFreeMemory(String freeMemory) {
		this.freeMemory = freeMemory;
	}

	public String getFreeDiskSpace() {
		return freeDiskSpace;
	}

	public void setFreeDiskSpace(String freeDiskSpace) {
		this.freeDiskSpace = freeDiskSpace;
	}

	@Override
	public String toString() {
		return "GluuServerStatus [hostname=" + hostname + ", ipAddress=" + ipAddress + ", uptime=" + uptime
				+ ", lastUpdate=" + lastUpdate + ", pollingInterval=" + pollingInterval + ", personCount=" + personCount
				+ ", groupCount=" + groupCount + ", freeMemory=" + freeMemory + ", freeDiskSpace=" + freeDiskSpace
				+ "]";
	}

}
