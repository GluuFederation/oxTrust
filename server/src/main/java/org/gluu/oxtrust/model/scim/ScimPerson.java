/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

/**
 * SCIM person Entity
 * 
 * @author Reda Zerrad Date: 04.17.2012
 */
@XmlRootElement(name = "User")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonPropertyOrder({ "schemas", "id", "externalId", "userName", "name", "displayName", "nickName", "profileUrl", "emails", "addresses",
		"phoneNumbers", "ims", "photos", "userType", "title", "preferredLanguage", "locale", "timezone", "active", "password", "groups",
		"roles", "entitlements", "x509Certificates", "meta", "customAttributes" })
@XmlType(propOrder = { "id", "externalId", "userName", "name", "displayName", "nickName", "profileUrl", "emails", "addresses",
		"phoneNumbers", "ims", "photos", "userType", "title", "preferredLanguage", "locale", "timezone", "active", "password", "groups",
		"roles", "entitlements", "x509Certificates", "meta", "customAttributes" })
public class ScimPerson implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4855047769008483029L;
	private List<String> schemas;
	private String externalId;
	private String userName;
	private String id;
	private ScimName name;
	private String displayName;
	private String nickName;
	private String profileUrl;
	private String title;
	private String userType;
	private String password;
	private List<ScimPersonEmails> emails;
	private List<ScimPersonPhones> phoneNumbers;
	private List<ScimPersonIms> ims;
	private List<ScimPersonPhotos> photos;
	private List<Scimx509Certificates> x509Certificates;
	private List<ScimPersonAddresses> addresses;
	private String timezone;
	private String locale;
	private String preferredLanguage;
	private List<ScimPersonGroups> groups;
	private String active;
	private List<ScimRoles> roles;
	private PersonMeta meta;
	private List<ScimEntitlements> entitlements;
	private List<ScimCustomAttributes> customAttributes;

	@XmlTransient
	public List<String> getSchemas() {
		return schemas;
	}

	@XmlElement
	public String getExternalId() {
		return this.externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@XmlElement
	public String getLocale() {
		return this.locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public void setSchemas(List<String> schemas) {
		this.schemas = schemas;
	}

	@XmlElement
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlElement
	public ScimName getName() {
		return name;
	}

	public void setName(ScimName name) {
		this.name = name;
	}

	@XmlElement
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@XmlElement
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@XmlElementWrapper(name = "emails")
	@XmlElement(name = "email")
	public List<ScimPersonEmails> getEmails() {
		return this.emails;
	}

	public void setEmails(List<ScimPersonEmails> emails) {
		this.emails = emails;
	}

	@XmlElement
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@XmlElement
	public String getNickName() {
		return this.nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	@XmlElement
	public String getProfileUrl() {
		return this.profileUrl;
	}

	public void setProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}

	@XmlElement
	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@XmlElement
	public String getUserType() {
		return this.userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	@XmlElement
	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	@XmlElement
	public String getPreferredLanguage() {
		return preferredLanguage;
	}

	public void setPreferredLanguage(String preferredLanguage) {
		this.preferredLanguage = preferredLanguage;
	}

	@XmlElementWrapper(name = "addresses")
	@XmlElement(name = "address")
	public List<ScimPersonAddresses> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<ScimPersonAddresses> addresses) {
		this.addresses = addresses;
	}

	@XmlElementWrapper(name = "PhoneNumbers")
	@XmlElement(name = "PhoneNumber")
	public List<ScimPersonPhones> getPhoneNumbers() {
		return phoneNumbers;
	}

	public void setPhoneNumbers(List<ScimPersonPhones> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}

	@XmlElementWrapper(name = "ims")
	@XmlElement(name = "im")
	public List<ScimPersonIms> getIms() {
		return this.ims;
	}

	public void setIms(List<ScimPersonIms> ims) {

		this.ims = ims;

	}

	@XmlElementWrapper(name = "photos")
	@XmlElement(name = "photo")
	public List<ScimPersonPhotos> getPhotos() {
		return this.photos;
	}

	public void setPhotos(List<ScimPersonPhotos> photos) {
		this.photos = photos;
	}

	@XmlElementWrapper(name = "x509Certificates")
	@XmlElement(name = "x509Certificate")
	public List<Scimx509Certificates> getX509Certificates() {
		return this.x509Certificates;
	}

	public void setX509Certificates(List<Scimx509Certificates> x509Certificates) {
		this.x509Certificates = x509Certificates;
	}

	@XmlElementWrapper(name = "groups")
	@XmlElement(name = "group")
	public List<ScimPersonGroups> getGroups() {
		return groups;
	}

	public void setGroups(List<ScimPersonGroups> groups) {
		this.groups = groups;
	}

	@XmlElement
	public String getActive() {
		return this.active;
	}

	public void setActive(String active) {
		this.active = active;
	}

	@XmlElement
	public PersonMeta getMeta() {
		return this.meta;
	}

	public void setMeta(PersonMeta meta) {
		this.meta = meta;
	}

	@XmlElementWrapper(name = "roles")
	@XmlElement(name = "role")
	public List<ScimRoles> getRoles() {
		return this.roles;
	}

	public void setRoles(List<ScimRoles> roles) {
		this.roles = roles;
	}

	@XmlElementWrapper(name = "entitlements")
	@XmlElement(name = "entitlement")
	public List<ScimEntitlements> getEntitlements() {
		return this.entitlements;

	}

	public void setEntitlements(List<ScimEntitlements> entitlements) {
		this.entitlements = entitlements;
	}

	public List<ScimCustomAttributes> getCustomAttributes() {
		return this.customAttributes;
	}

	public void setCustomAttributes(List<ScimCustomAttributes> customAttributes) {
		this.customAttributes = customAttributes;
	}

	public ScimPerson() {
		schemas = new ArrayList<String>();
		externalId = "";
		locale = "";
		userName = "";
		id = "";
		name = new ScimName();
		displayName = "";
		nickName = "";
		profileUrl = "";
		title = "";
		userType = "";
		password = "";
		emails = new ArrayList<ScimPersonEmails>();
		phoneNumbers = new ArrayList<ScimPersonPhones>();
		ims = new ArrayList<ScimPersonIms>();
		photos = new ArrayList<ScimPersonPhotos>();
		x509Certificates = new ArrayList<Scimx509Certificates>();
		addresses = new ArrayList<ScimPersonAddresses>();
		groups = new ArrayList<ScimPersonGroups>();
		meta = new PersonMeta();
		entitlements = new ArrayList<ScimEntitlements>();
		roles = new ArrayList<ScimRoles>();
		customAttributes = new ArrayList<ScimCustomAttributes>();
	}
}
