/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.gluu.model.ldap.GluuLdapConfiguration;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * oxIDPAuthConf
 * 
 * @author Reda Zerrad Date: 08.14.2012
 */

@XmlRootElement(name = "oxIDPAuthConf")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "type", "name", "level", "priority", "enabled", "version", "fields", "config" })
@JsonPropertyOrder({ "type", "name", "level", "priority", "enabled", "version", "fields", "config" })
public class OxIDPAuthConf {
	private String type;
	private String name;
	private int level;
	private int priority;

	private boolean enabled;
	private int version;
	private List<CustomAttribute> fields;
	private GluuLdapConfiguration config;

	public OxIDPAuthConf() {
		this.fields = new ArrayList<CustomAttribute>();
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public boolean getEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public List<CustomAttribute> getFields() {
		return this.fields;
	}

	public void setFields(List<CustomAttribute> fields) {
		this.fields = fields;
	}

	public GluuLdapConfiguration getConfig() {
		return config;
	}

	public void setConfig(GluuLdapConfiguration config) {
		this.config = config;
	}

}
