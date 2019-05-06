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
 * oxChooser errors
 * 
 * @author Reda Zerrad Date: 07.04.2012
 */
@XmlRootElement(name = "Error")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonPropertyOrder({ "description" })
@XmlType(propOrder = { "description" })
public class OxChooserError {

	private String description;

	public OxChooserError() {
		description = "";
	}

	public OxChooserError(String p_description) {
		description = p_description;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
