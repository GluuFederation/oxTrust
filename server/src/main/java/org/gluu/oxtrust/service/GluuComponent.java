package org.gluu.oxtrust.service;

public class GluuComponent {
	private String file;
	private String title;
	private String version;
	private String buildDate;
	private String build;
	private String branch;

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getBuildDate() {
		return buildDate;
	}

	public void setBuildDate(String buildDate) {
		this.buildDate = buildDate;
	}

	public String getBuild() {
		return build;
	}

	public void setBuild(String build) {
		this.build = build;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getKey() {
		String[] values = file.split("/");
		return values[values.length - 1];
	}

	@Override
	public String toString() {
		return "GluuComponent [file=" + file + ", title=" + title + ", version=" + version + ", buildDate=" + buildDate
				+ ", build=" + build + ", branch=" + branch + "]";
	}

}