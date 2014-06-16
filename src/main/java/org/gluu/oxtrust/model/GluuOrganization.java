package org.gluu.oxtrust.model;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapJsonObject;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;
import org.xdi.ldap.model.Entry;
import org.xdi.ldap.model.GluuStatus;

/**
 * Group
 * 
 * @author Yuriy Movchan Date: 11.02.2010
 */
@LdapEntry(sortBy = { "displayName" })
@LdapObjectClass(values = { "top", "gluuGroup", "oxEntry" })
public @Data class GluuOrganization extends Entry implements Serializable {

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

	@LdapAttribute(name = "gluuOwnerGroup")
	private String ownerGroup;

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

	@LdapAttribute(name = "scimStatus")
	private GluuStatus scimStatus;

	@LdapAttribute(name = "scimAuthMode")
	private String scimAuthMode;

	@LdapAttribute(name = "scimGroup")
	private String scimGroup;

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

}
