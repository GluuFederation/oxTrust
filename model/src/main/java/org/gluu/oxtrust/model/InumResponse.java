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
 * InumReponse
 * 
 * @author Reda Zerrad Date: 08.22.2012
 */
@XmlRootElement(name = "InumReponse")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonPropertyOrder({ "generatedInum" })
@XmlType(propOrder = { "generatedInum" })
public class InumResponse {

	private String generatedInum;

	public InumResponse() {
		generatedInum = "";
	}

	public String getGeneratedInum() {
		return this.generatedInum;
	}

	public void setGeneratedInum(String generatedInum) {
		this.generatedInum = generatedInum;
	}

}
