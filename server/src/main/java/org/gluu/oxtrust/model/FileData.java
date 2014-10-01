/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

public class FileData implements Comparable<FileData> {
	String name;
	String filePath;
	long lastModified;
	long size;

	public FileData(String name, String filePath, long lastModified, long size) {
		this.name = name;
		this.filePath = filePath;
		this.lastModified = lastModified;
		this.size = size;
	}

	public int compareTo(FileData fileData) {
		if (this.lastModified > fileData.getLastModified())
			return 1;
		return -1;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
}
