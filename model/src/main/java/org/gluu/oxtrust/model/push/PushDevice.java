/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.push;

import java.io.Serializable;

import org.gluu.persist.model.base.Entry;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapJsonObject;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;

/**
 * Push device
 * 
 * @author Yuriy Movchan Date: 01/10/2014
 */
@LdapEntry(sortBy = { "userId" })
@LdapObjectClass(values = { "top", "oxPushDevice" })
public class PushDevice extends Entry implements Serializable {

	private static final long serialVersionUID = 1332826784937052508L;

	@LdapAttribute(ignoreDuringUpdate = true, name = "oxId")
	private String id;

	@LdapAttribute(name = "oxType")
	private String type;

	@LdapAttribute(name = "oxPushApplication")
	private String application;

	@LdapAttribute(name = "oxAuthUserId")
	private String userId;

	@LdapAttribute(name = "oxPushDeviceConf")
	@LdapJsonObject
	private PushDeviceConfiguration deviceConfiguration;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public PushDeviceConfiguration getDeviceConfiguration() {
		return deviceConfiguration;
	}

	public void setDeviceConfiguration(PushDeviceConfiguration deviceConfiguration) {
		this.deviceConfiguration = deviceConfiguration;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PushDevice [id=").append(id).append(", type=").append(type).append(", application=").append(application)
				.append(", userId=").append(userId).append(", deviceConfiguration=").append(deviceConfiguration).append("]");
		return builder.toString();
	}

}
