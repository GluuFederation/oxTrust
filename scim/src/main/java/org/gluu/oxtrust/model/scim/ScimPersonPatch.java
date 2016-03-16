/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SCIM person Patch request
 * 
 * @author Reda Zerrad Date: 04.25.2012
 */
@XmlRootElement(name = "User")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonPropertyOrder({ "schemas", "id", "externalId", "userName", "name", "displayName", "nickName", "profileUrl", "emails", "addresses",
		"phoneNumbers", "ims", "photos", "userType", "title", "preferredLanguage", "locale", "timezone", "active", "password", "groups",
		"roles", "entitlements", "x509Certificates", "meta" })
@XmlType(propOrder = { "id", "externalId", "userName", "name", "displayName", "nickName", "profileUrl", "emails", "addresses",
		"phoneNumbers", "ims", "photos", "userType", "title", "preferredLanguage", "locale", "timezone", "active", "password", "groups",
		"roles", "entitlements", "x509Certificates", "meta" })
public class ScimPersonPatch {
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
	private List<ScimPersonEmailsPatch> emails;
	private List<ScimPersonPhonesPatch> phoneNumbers;
	private List<ScimPersonImsPatch> ims;
	private List<ScimPersonPhotosPatch> photos;
	private List<Scimx509CertificatesPatch> x509Certificates;
	private List<ScimPersonAddressesPatch> addresses;
	private String timezone;
	private String locale;
	private String preferredLanguage;
	private List<ScimPersonGroupsPatch> groups;
	private String active;
	private List<ScimRolesPatch> roles;
	private PersonMetaPatch meta;
	private List<ScimEntitlementsPatch> entitlements;

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
	public List<ScimPersonEmailsPatch> getEmails() {
		return this.emails;
	}

	public void setEmails(List<ScimPersonEmailsPatch> emails) {
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
	public List<ScimPersonAddressesPatch> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<ScimPersonAddressesPatch> addresses) {
		this.addresses = addresses;
	}

	@XmlElementWrapper(name = "phoneNumbers")
	@XmlElement(name = "PhoneNumber")
	public List<ScimPersonPhonesPatch> getPhoneNumbers() {
		return phoneNumbers;
	}

	public void setPhoneNumbers(List<ScimPersonPhonesPatch> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}

	@XmlElementWrapper(name = "ims")
	@XmlElement(name = "im")
	public List<ScimPersonImsPatch> getIms() {
		return this.ims;
	}

	public void setIms(List<ScimPersonImsPatch> ims) {

		this.ims = ims;

	}

	@XmlElementWrapper(name = "photos")
	@XmlElement(name = "photo")
	public List<ScimPersonPhotosPatch> getPhotos() {
		return this.photos;
	}

	public void setPhotos(List<ScimPersonPhotosPatch> photos) {
		this.photos = photos;
	}

	@XmlElementWrapper(name = "x509Certificates")
	@XmlElement(name = "x509Certificate")
	public List<Scimx509CertificatesPatch> getX509Certificates() {
		return this.x509Certificates;
	}

	public void setX509Certificates(List<Scimx509CertificatesPatch> x509Certificates) {
		this.x509Certificates = x509Certificates;
	}

	@XmlElementWrapper(name = "groups")
	@XmlElement(name = "group")
	public List<ScimPersonGroupsPatch> getGroups() {
		return groups;
	}

	public void setGroups(List<ScimPersonGroupsPatch> groups) {
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
	public PersonMetaPatch getMeta() {
		return this.meta;
	}

	public void setMeta(PersonMetaPatch meta) {
		this.meta = meta;
	}

	@XmlElementWrapper(name = "roles")
	@XmlElement(name = "role")
	public List<ScimRolesPatch> getRoles() {
		return this.roles;
	}

	public void setRoles(List<ScimRolesPatch> roles) {
		this.roles = roles;
	}

	@XmlElementWrapper(name = "entitlements")
	@XmlElement(name = "entitlement")
	public List<ScimEntitlementsPatch> getEntitlements() {
		return this.entitlements;

	}

	public void setEntitlements(List<ScimEntitlementsPatch> entitlements) {
		this.entitlements = entitlements;
	}

	public ScimPersonPatch() {
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
		emails = new ArrayList<ScimPersonEmailsPatch>();
		phoneNumbers = new ArrayList<ScimPersonPhonesPatch>();
		ims = new ArrayList<ScimPersonImsPatch>();
		photos = new ArrayList<ScimPersonPhotosPatch>();
		x509Certificates = new ArrayList<Scimx509CertificatesPatch>();
		addresses = new ArrayList<ScimPersonAddressesPatch>();
		groups = new ArrayList<ScimPersonGroupsPatch>();
		meta = new PersonMetaPatch();
		entitlements = new ArrayList<ScimEntitlementsPatch>();
		roles = new ArrayList<ScimRolesPatch>();
	}
}
