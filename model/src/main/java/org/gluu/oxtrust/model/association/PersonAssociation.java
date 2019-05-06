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
@JsonPropertyOrder({ "userAssociation", "entryAssociations" })
@XmlType(propOrder = { "userAssociation", "entryAssociations" })
public class PersonAssociation {

	private String userAssociation;
	private List<String> entryAssociations;

	public PersonAssociation() {
		userAssociation = "";
		entryAssociations = new ArrayList<String>();
	}

	@XmlElement
	public String getUserAssociation() {
		return this.userAssociation;
	}

	public void setUserAssociation(String userAssociation) {
		this.userAssociation = userAssociation;
	}

	@XmlElementWrapper(name = "entryAssociations")
	@XmlElement(name = "entryAssociation")
	public List<String> getEntryAssociations() {
		return this.entryAssociations;
	}

	public void setEntryAssociations(List<String> entryAssociations) {
		this.entryAssociations = entryAssociations;
	}

}
