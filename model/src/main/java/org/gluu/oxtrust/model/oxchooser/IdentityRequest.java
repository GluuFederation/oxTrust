/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.oxchooser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * oxChooser request
 * 
 * @author Reda Zerrad Date: 07.04.2012
 */
@XmlRootElement(name = "OxChooserRequest")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonPropertyOrder({ "identifier", "returnToUrl", "axschema", "realm" })
@XmlType(propOrder = { "identifier", "returnToUrl", "axschema", "realm" })
public class IdentityRequest {

	private String identifier;
	private String returnToUrl;
	private String axschema;
	private String realm;

	public IdentityRequest() {
		this.identifier = "";
		this.returnToUrl = "";
		this.axschema = "";
		this.realm = "";
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getReturnToUrl() {
		return this.returnToUrl;
	}

	public void setReturnToUrl(String returnToUrl) {
		this.returnToUrl = returnToUrl;
	}

	public String getAxschema() {
		return this.axschema;
	}

	public void setAxschema(String axschema) {
		this.axschema = axschema;
	}

	public String getRealm() {
		return this.realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

}
