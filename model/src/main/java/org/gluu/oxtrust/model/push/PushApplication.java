/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.push;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.gluu.persist.model.base.Entry;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapJsonObject;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;

/**
 * Push application
 * 
 * @author Yuriy Movchan Date: 01/10/2014
 */
@LdapEntry(sortBy = { "displayName" })
@LdapObjectClass(values = { "top", "oxPushApplication" })
public class PushApplication extends Entry implements Serializable {

	private static final long serialVersionUID = 3308826784937052508L;

	@LdapAttribute(ignoreDuringUpdate = true, name = "oxId")
	private String id;

	@Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Name should contain only letters, digits and underscores")
	@Size(min = 1, max = 30, message = "Length of the Name should be between 1 and 30")
	@LdapAttribute(name = "oxName")
	private String name;

	@NotNull
	@Size(min = 0, max = 60, message = "Length of the Display Name should not exceed 60")
	@LdapAttribute
	private String displayName;

	@LdapAttribute(name = "oxPushApplicationConf")
	@LdapJsonObject
	private PushApplicationConfiguration appConfiguration;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public PushApplicationConfiguration getappConfiguration() {
		return appConfiguration;
	}

	public void setappConfiguration(PushApplicationConfiguration appConfiguration) {
		this.appConfiguration = appConfiguration;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PushApplication [id=").append(id).append(", name=").append(name).append(", displayName=").append(displayName)
				.append(", appConfiguration=").append(appConfiguration).append("]");
		return builder.toString();
	}

}
