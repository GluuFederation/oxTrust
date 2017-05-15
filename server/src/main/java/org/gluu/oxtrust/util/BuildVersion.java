/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.util;

import java.io.Serializable;

import javax.inject.Named;
/**
 * Constants with current build info
 * 
 * @author Yuriy Movchan Date: 12.17.2010
 */
@Named("buildVersion")
public class BuildVersion implements Serializable {

	private static final long serialVersionUID = 3790281266924133197L;

	private String revisionVersion;
	private String revisionDate;
	private String buildDate;
	private String buildNumber;

	public String getRevisionVersion() {
		return revisionVersion;
	}

	public void setRevisionVersion(String revisionVersion) {
		this.revisionVersion = revisionVersion;
	}

	public String getRevisionDate() {
		return revisionDate;
	}

	public void setRevisionDate(String revisionDate) {
		this.revisionDate = revisionDate;
	}

	public String getBuildDate() {
		return buildDate;
	}

	public void setBuildDate(String buildDate) {
		this.buildDate = buildDate;
	}

	public String getBuildNumber() {
		return buildNumber;
	}

	public void setBuildNumber(String buildNumber) {
		this.buildNumber = buildNumber;
	}

}
