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
 * oxChooser response
 * 
 * @author Reda Zerrad Date: 07.04.2012
 */
@XmlRootElement(name = "OxChooserResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonPropertyOrder({ "firstname", "lastname", "email", "language", "country", "nickname", "fullname", "gender", "timezone", "image" })
@XmlType(propOrder = { "firstname", "lastname", "email", "language", "country", "nickname", "fullname", "gender", "timezone", "image" })
public class IdentityResponse {

	private String firstname;
	private String lastname;
	private String email;
	private String language;
	private String country;
	private String nickname;
	private String fullname;
	private String gender;
	private String timezone;
	private String image;

	public IdentityResponse() {
		this.firstname = "";
		this.lastname = "";
		this.email = "";
		this.language = "";
		this.country = "";
		this.nickname = "";
		this.fullname = "";
		this.gender = "";
		this.timezone = "";
		this.image = "";
	}

	public String getFirstname() {
		return this.firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return this.lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLanguage() {
		return this.language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getCountry() {
		return this.country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getNickname() {
		return this.nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getFullname() {
		return this.fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getGender() {
		return this.gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getTimezone() {
		return this.timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getImage() {
		return this.image;
	}

	public void setImage(String image) {
		this.image = image;
	}

}
