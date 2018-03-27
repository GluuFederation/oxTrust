package org.gluu.oxtrust.api;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.gluu.oxtrust.model.GluuCustomPerson;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "gluuperson")
public class GluuPersonApi implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4216836182127315394L;
	private String inum;
	private String iname;
	private String displayName;
	private String surName;
	private String givenName;
	private Date creationDate;

	public GluuPersonApi() {
	}

	public GluuPersonApi(GluuCustomPerson person) {
		this.inum = person.getInum();
		this.iname = person.getIname();
		this.displayName = person.getDisplayName();
		this.creationDate = person.getCreationDate();
		this.givenName = person.getGivenName();
		this.surName = person.getSurname();
	}

	public String getInum() {
		return inum;
	}

	public void setInum(String inum) {
		this.inum = inum;
	}

	public String getIname() {
		return iname;
	}

	public void setIname(String iname) {
		this.iname = iname;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getSurName() {
		return surName;
	}

	public void setSurName(String surName) {
		this.surName = surName;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
}
