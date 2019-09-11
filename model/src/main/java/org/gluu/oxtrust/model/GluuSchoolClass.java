/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

/**
 * User: Dejan Maric
 */
public class GluuSchoolClass {

	private String uid;

	private String inum;

	private String displayName;

	private boolean selected;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getInum() {
		return inum;
	}

	public void setInum(String inum) {
		this.inum = inum;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		GluuSchoolClass that = (GluuSchoolClass) o;

		if (inum != null ? !inum.equals(that.inum) : that.inum != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return inum != null ? inum.hashCode() : 0;
	}
}
