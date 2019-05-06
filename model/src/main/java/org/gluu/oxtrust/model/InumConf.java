/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * InumConfig
 * 
 * @author Reda Zerrad Date: 08.22.2012
 */
@XmlRootElement(name = "InumConfig")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonPropertyOrder({ "personPrefix", "groupPrefix", "clientPrefix", "scopePrefix", "scriptName" })
@XmlType(propOrder = { "personPrefix", "groupPrefix", "clientPrefix", "scopePrefix", "scriptName" })
public class InumConf {

	private String personPrefix;
	private String groupPrefix;
	private String clientPrefix;
	private String scopePrefix;
	private String scriptName;

	public InumConf() {
		personPrefix = "";
		groupPrefix = "";
		clientPrefix = "";
		scopePrefix = "";
		scriptName = "";
	}

	public String getPersonPrefix() {
		return this.personPrefix;
	}

	public void setPersonPrefix(String personPrefix) {
		this.personPrefix = personPrefix;
	}

	public String getGroupPrefix() {
		return this.groupPrefix;
	}

	public void setGroupPrefix(String groupPrefix) {
		this.groupPrefix = groupPrefix;
	}

	public String getClientPrefix() {
		return this.clientPrefix;
	}

	public void setClientPrefix(String clientPrefix) {
		this.clientPrefix = clientPrefix;
	}

	public String getScopePrefix() {
		return this.scopePrefix;
	}

	public void setScopePrefix(String scopePrefix) {
		this.scopePrefix = scopePrefix;
	}

	public String getScriptName() {
		return this.scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}
}
