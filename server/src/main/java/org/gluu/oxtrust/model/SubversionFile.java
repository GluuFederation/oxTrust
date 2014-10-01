/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

/**
 * @author Yuriy Movchan Date: 11.25.2010
 */
public class SubversionFile {

	String svnFolder;
	String localFile;

	public SubversionFile(String svnFolder, String localFile) {
		this.svnFolder = svnFolder;
		this.localFile = localFile;
	}

	public String getSvnFolder() {
		return svnFolder;
	}

	public void setSvnFolder(String svnFolder) {
		this.svnFolder = svnFolder;
	}

	public String getLocalFile() {
		return localFile;
	}

	public void setLocalFile(String localFile) {
		this.localFile = localFile;
	}

	@Override
	public String toString() {
		return String.format("SubversionFile [svnFolder=%s, localFile=%s]", svnFolder, localFile);
	}

}
