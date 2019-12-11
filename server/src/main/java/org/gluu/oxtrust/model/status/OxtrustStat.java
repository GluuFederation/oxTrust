package org.gluu.oxtrust.model.status;

import java.io.Serializable;

public class OxtrustStat implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4325309258488921779L;
	private String freeDiskSpace;
	private String freeMemory;
	private String freeSwap;
	private String ipAddress;
	private String systemUptime;
	private String loadAvg;
	private String groupCount;
	private String personCount;

	public String getFreeDiskSpace() {
		return freeDiskSpace;
	}

	public void setFreeDiskSpace(String freeDiskSpace) {
		this.freeDiskSpace = freeDiskSpace;
	}

	public String getFreeMemory() {
		return freeMemory;
	}

	public void setFreeMemory(String freeMemory) {
		this.freeMemory = freeMemory;
	}

	public String getFreeSwap() {
		return freeSwap;
	}

	public void setFreeSwap(String freeSwap) {
		this.freeSwap = freeSwap;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getSystemUptime() {
		return systemUptime;
	}

	public void setSystemUptime(String systemUptime) {
		this.systemUptime = systemUptime;
	}

	public String getLoadAvg() {
		return loadAvg;
	}

	public void setLoadAvg(String loadAvg) {
		this.loadAvg = loadAvg;
	}

	public String getGroupCount() {
		return groupCount;
	}

	public void setGroupCount(String groupCount) {
		this.groupCount = groupCount;
	}

	public String getPersonCount() {
		return personCount;
	}

	public void setPersonCount(String personCount) {
		this.personCount = personCount;
	}

}
