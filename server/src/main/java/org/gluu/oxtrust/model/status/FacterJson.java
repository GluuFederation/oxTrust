package org.gluu.oxtrust.model.status;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "memoryfree", "memoryfree_mb", "memorysize_mb", "swapfree", "swapsize", "hostname", "ipaddress",
		"uptime_seconds", "free_disk_space", "load_average" })
public class FacterJson {

	@JsonProperty("memoryfree")
	private String memoryfree;
	@JsonProperty("memoryfree_mb")
	private String memoryfreeMb;
	@JsonProperty("memorysize_mb")
	private String memorysizeMb;
	@JsonProperty("swapfree")
	private String swapfree;
	@JsonProperty("swapsize")
	private String swapsize;
	@JsonProperty("hostname")
	private String hostname;
	@JsonProperty("ipaddress")
	private String ipaddress;
	@JsonProperty("uptime_seconds")
	private Integer uptimeSeconds;
	@JsonProperty("free_disk_space")
	private Double freeDiskSpace;
	@JsonProperty("load_average")
	private String loadAverage;

	@JsonProperty("memoryfree")
	public String getMemoryfree() {
		return memoryfree;
	}

	@JsonProperty("memoryfree")
	public void setMemoryfree(String memoryfree) {
		this.memoryfree = memoryfree;
	}

	@JsonProperty("memoryfree_mb")
	public String getMemoryfreeMb() {
		return memoryfreeMb;
	}

	@JsonProperty("memoryfree_mb")
	public void setMemoryfreeMb(String memoryfreeMb) {
		this.memoryfreeMb = memoryfreeMb;
	}

	@JsonProperty("memorysize_mb")
	public String getMemorysizeMb() {
		return memorysizeMb;
	}

	@JsonProperty("memorysize_mb")
	public void setMemorysizeMb(String memorysizeMb) {
		this.memorysizeMb = memorysizeMb;
	}

	@JsonProperty("swapfree")
	public String getSwapfree() {
		return swapfree;
	}

	@JsonProperty("swapfree")
	public void setSwapfree(String swapfree) {
		this.swapfree = swapfree;
	}

	@JsonProperty("swapsize")
	public String getSwapsize() {
		return swapsize;
	}

	@JsonProperty("swapsize")
	public void setSwapsize(String swapsize) {
		this.swapsize = swapsize;
	}

	@JsonProperty("hostname")
	public String getHostname() {
		return hostname;
	}

	@JsonProperty("hostname")
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	@JsonProperty("ipaddress")
	public String getIpaddress() {
		return ipaddress;
	}

	@JsonProperty("ipaddress")
	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}

	@JsonProperty("uptime_seconds")
	public Integer getUptimeSeconds() {
		return uptimeSeconds;
	}

	@JsonProperty("uptime_seconds")
	public void setUptimeSeconds(Integer uptimeSeconds) {
		this.uptimeSeconds = uptimeSeconds;
	}

	@JsonProperty("free_disk_space")
	public Double getFreeDiskSpace() {
		return freeDiskSpace;
	}

	@JsonProperty("free_disk_space")
	public void setFreeDiskSpace(Double freeDiskSpace) {
		this.freeDiskSpace = freeDiskSpace;
	}

	public String getLoadAverage() {
		return loadAverage;
	}

	public void setLoadAverage(String loadAverage) {
		this.loadAverage = loadAverage;
	}
}