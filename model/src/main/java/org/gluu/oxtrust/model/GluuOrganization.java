/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.model.GluuStatus;
import org.gluu.persist.model.base.Entry;
import org.gluu.persist.annotation.AttributeName;
import org.gluu.persist.annotation.DataEntry;
import org.gluu.persist.annotation.JsonObject;
import org.gluu.persist.annotation.ObjectClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Group
 * 
 * @author Yuriy Movchan Date: 11.02.2010
 */
@DataEntry(sortBy = { "displayName" })
@ObjectClass(values = { "top", "gluuOrganization" })
@JsonInclude(Include.NON_NULL)
public class GluuOrganization extends Entry implements Serializable {

	private static final long serialVersionUID = -8284018077740582699L;

	@NotNull
	@Size(min = 0, max = 60, message = "Length of the Display Name should not exceed 60")
	@AttributeName
	private String displayName;

	@NotNull
	@Size(min = 0, max = 60, message = "Length of the Description should not exceed 60")
	@AttributeName
	private String description;

	@AttributeName(name = "memberOf")
	private String member;

	@AttributeName(name = "c")
	private String countryName;

	@AttributeName(name = "o")
	private String organization;

	@AttributeName
	private String seeAlso;

	@AttributeName(name = "gluuStatus")
	private GluuStatus status;

	@AttributeName(name = "gluuManagerGroup")
	private String managerGroup;

	@AttributeName(name = "gluuLogoImage")
	private String logoImage;

	@AttributeName(name = "gluuThemeColor")
	private String themeColor;

	@AttributeName(name = "gluuOrgShortName")
	private String shortName;

	@AttributeName(name = "gluuCustomMessage")
	private String[] customMessages;

	@AttributeName(name = "gluuFaviconImage")
	private String faviconImage;

	@AttributeName(name = "oxInumConfig")
	private String oxInumConfig;

	@AttributeName(name = "title")
	private String title;

	@AttributeName(name = "oxRegistrationConfiguration")
	@JsonObject
	private RegistrationConfiguration oxRegistrationConfiguration;

	public String getOrganizationTitle() {
		if (title == null || title.trim().equals("")) {
			return "Gluu";
		}
		return title;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public String[] getCustomMessages() {
		return customMessages;
	}

	public void setCustomMessages(String[] customMessages) {
		this.customMessages = customMessages;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getFaviconImage() {
		return faviconImage;
	}

	public void setFaviconImage(String faviconImage) {
		this.faviconImage = faviconImage;
	}

	public String getLogoImage() {
		return logoImage;
	}

	public void setLogoImage(String logoImage) {
		this.logoImage = logoImage;
	}

	public String getManagerGroup() {
		return managerGroup;
	}

	public void setManagerGroup(String managerGroup) {
		this.managerGroup = managerGroup;
	}

	public String getMember() {
		return member;
	}

	public void setMember(String member) {
		this.member = member;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getOxInumConfig() {
		return oxInumConfig;
	}

	public void setOxInumConfig(String oxInumConfig) {
		this.oxInumConfig = oxInumConfig;
	}

	public RegistrationConfiguration getOxRegistrationConfiguration() {
		return oxRegistrationConfiguration;
	}

	public void setOxRegistrationConfiguration(RegistrationConfiguration oxRegistrationConfiguration) {
		this.oxRegistrationConfiguration = oxRegistrationConfiguration;
	}

	public String getSeeAlso() {
		return seeAlso;
	}

	public void setSeeAlso(String seeAlso) {
		this.seeAlso = seeAlso;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public GluuStatus getStatus() {
		return status;
	}

	public void setStatus(GluuStatus status) {
		this.status = status;
	}

	public String getThemeColor() {
		return themeColor;
	}

	public void setThemeColor(String themeColor) {
		this.themeColor = themeColor;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
