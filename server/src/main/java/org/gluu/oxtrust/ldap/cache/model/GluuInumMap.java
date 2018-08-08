/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.ldap.cache.model;

import java.io.Serializable;
import java.util.Arrays;

import org.gluu.persist.model.base.Entry;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;
import org.xdi.model.GluuStatus;

/**
 * GluuInumMap
 * 
 * @author Yuriy Movchan Date: 07.13.2011
 */
@LdapEntry(sortBy = { "inum" })
@LdapObjectClass(values = { "top", "gluuInumMap" })
public class GluuInumMap extends Entry implements Serializable {

	private static final long serialVersionUID = -2190480357430436503L;

	@LdapAttribute(ignoreDuringUpdate = true)
	private String inum;

	@LdapAttribute
	private String primaryKeyAttrName;

	@LdapAttribute(name = "primaryKeyValue")
	private String[] primaryKeyValues;

	@LdapAttribute
	private String secondaryKeyAttrName;

	@LdapAttribute(name = "secondaryKeyValue")
	private String[] secondaryKeyValues;

	@LdapAttribute
	private String tertiaryKeyAttrName;

	@LdapAttribute(name = "tertiaryKeyValue")
	private String[] tertiaryKeyValues;

	@LdapAttribute(name = "gluuStatus")
	private GluuStatus status;

	public String getInum() {
		return inum;
	}

	public void setInum(String inum) {
		this.inum = inum;
	}

	public String getPrimaryKeyAttrName() {
		return primaryKeyAttrName;
	}

	public void setPrimaryKeyAttrName(String primaryKeyAttrName) {
		this.primaryKeyAttrName = primaryKeyAttrName;
	}

	public String[] getPrimaryKeyValues() {
		return primaryKeyValues;
	}

	public void setPrimaryKeyValues(String[] primaryKeyValues) {
		this.primaryKeyValues = primaryKeyValues;
	}

	public String getSecondaryKeyAttrName() {
		return secondaryKeyAttrName;
	}

	public void setSecondaryKeyAttrName(String secondaryKeyAttrName) {
		this.secondaryKeyAttrName = secondaryKeyAttrName;
	}

	public String[] getSecondaryKeyValues() {
		return secondaryKeyValues;
	}

	public void setSecondaryKeyValues(String[] secondaryKeyValues) {
		this.secondaryKeyValues = secondaryKeyValues;
	}

	public String getTertiaryKeyAttrName() {
		return tertiaryKeyAttrName;
	}

	public void setTertiaryKeyAttrName(String tertiaryKeyAttrName) {
		this.tertiaryKeyAttrName = tertiaryKeyAttrName;
	}

	public String[] getTertiaryKeyValues() {
		return tertiaryKeyValues;
	}

	public void setTertiaryKeyValues(String[] tertiaryKeyValues) {
		this.tertiaryKeyValues = tertiaryKeyValues;
	}

	public GluuStatus getStatus() {
		return status;
	}

	public void setStatus(GluuStatus status) {
		this.status = status;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GluuInumMap [inum=").append(inum).append(", primaryKeyAttrName=").append(primaryKeyAttrName)
				.append(", primaryKeyValues=").append(Arrays.toString(primaryKeyValues)).append(", secondaryKeyAttrName=")
				.append(secondaryKeyAttrName).append(", secondaryKeyValues=").append(Arrays.toString(secondaryKeyValues))
				.append(", tertiaryKeyAttrName=").append(tertiaryKeyAttrName).append(", tertiaryKeyValues=")
				.append(Arrays.toString(tertiaryKeyValues)).append(", status=").append(status).append("]");
		return builder.toString();
	}

}
