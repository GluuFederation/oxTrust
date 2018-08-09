/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.persist.model.base.Entry;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapJsonObject;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;
import org.xdi.model.GluuStatus;

/**
 * Group
 * 
 * @author Yuriy Movchan Date: 11.02.2010
 */
@LdapEntry(sortBy = { "displayName" })
@LdapObjectClass(values = { "top", "gluuOrganization" })
public class GluuOrganization extends Entry implements Serializable {

	private static final long serialVersionUID = -8284018077740582699L;

	@LdapAttribute(ignoreDuringUpdate = true)
	private String inum;

	@LdapAttribute(ignoreDuringUpdate = true)
	private String iname;

	@NotNull
	@Size(min = 0, max = 60, message = "Length of the Display Name should not exceed 60")
	@LdapAttribute
	private String displayName;

	@NotNull
	@Size(min = 0, max = 60, message = "Length of the Description should not exceed 60")
	@LdapAttribute
	private String description;

	@LdapAttribute(name = "memberOf")
	private String member;

	@LdapAttribute(name = "c")
	private String countryName;

	@LdapAttribute(name = "o")
	private String organization;

	@LdapAttribute
	private String seeAlso;

	@LdapAttribute(name = "gluuStatus")
	private GluuStatus status;

	@LdapAttribute(name = "gluuCommunityAttribute")
	private String communityAttribute;

	@LdapAttribute(name = "gluuManagerGroup")
	private String managerGroup;

	@LdapAttribute(name = "gluuLogoImage")
	private String logoImage;

	@LdapAttribute(name = "gluuThemeColor")
	private String themeColor;

	@LdapAttribute(name = "gluuOrgShortName")
	private String shortName;

	@LdapAttribute(name = "gluuCustomMessage")
	private String[] customMessages;

	@LdapAttribute(name = "gluuFaviconImage")
	private String faviconImage;

	@LdapAttribute(name = "gluuTempFaviconImage")
	private String tempFaviconImage;

	@LdapAttribute(name = "oxInumConfig")
	private String oxInumConfig;

	@LdapAttribute(name = "title")
	private String title;
	
	@LdapAttribute(name = "oxLinktrackEnabled")
	private Boolean linktrackEnabled;

	@LdapAttribute(name = "oxLinktrackLogin")
	private String linktrackLogin;

	@LdapAttribute(name = "oxLinktrackPassword")
	private String linktrackPassword;
	
	@LdapAttribute(name = "oxRegistrationConfiguration")
	@LdapJsonObject
	private RegistrationConfiguration oxRegistrationConfiguration;

	public String getOrganizationTitle() {
		if (title == null || title.trim().equals("")) {
			return "Gluu";
		}
		return title;
	}

    public String getCommunityAttribute() {
        return communityAttribute;
    }

    public void setCommunityAttribute(String communityAttribute) {
        this.communityAttribute = communityAttribute;
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

    public String getIname() {
        return iname;
    }

    public void setIname(String iname) {
        this.iname = iname;
    }

    public String getInum() {
        return inum;
    }

    public void setInum(String inum) {
        this.inum = inum;
    }

    public Boolean getLinktrackEnabled() {
        return linktrackEnabled;
    }

    public void setLinktrackEnabled(Boolean linktrackEnabled) {
        this.linktrackEnabled = linktrackEnabled;
    }

    public String getLinktrackLogin() {
        return linktrackLogin;
    }

    public void setLinktrackLogin(String linktrackLogin) {
        this.linktrackLogin = linktrackLogin;
    }

    public String getLinktrackPassword() {
        return linktrackPassword;
    }

    public void setLinktrackPassword(String linktrackPassword) {
        this.linktrackPassword = linktrackPassword;
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

    public String getTempFaviconImage() {
        return tempFaviconImage;
    }

    public void setTempFaviconImage(String tempFaviconImage) {
        this.tempFaviconImage = tempFaviconImage;
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
