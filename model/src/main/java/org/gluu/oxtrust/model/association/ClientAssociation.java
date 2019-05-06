/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.association;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Client Association Entity
 * 
 * @author Reda Zerrad Date: 07.23.2012
 */

@XmlRootElement(name = "AssociationService")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonPropertyOrder({ "entryAssociation", "userAssociations" })
@XmlType(propOrder = { "entryAssociation", "userAssociations" })
public class ClientAssociation {

	private String entryAssociation;
	private List<String> userAssociations;

	public ClientAssociation() {
		entryAssociation = "";
		userAssociations = new ArrayList<String>();
	}

	@XmlElementWrapper(name = "userAssociations")
	@XmlElement(name = "userAssociation")
	public List<String> getUserAssociations() {
		return this.userAssociations;
	}

	public void setUserAssociations(List<String> userAssociations) {
		this.userAssociations = userAssociations;
	}

	@XmlElement
	public String getEntryAssociation() {
		return this.entryAssociation;
	}

	public void setEntryAssociation(String entryAssociation) {
		this.entryAssociation = entryAssociation;
	}

}
