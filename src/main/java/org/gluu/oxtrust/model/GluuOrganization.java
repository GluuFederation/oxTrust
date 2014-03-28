package org.gluu.oxtrust.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
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

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMember() {
		return member;
	}

	public void setMember(String member) {
		this.member = member;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getSeeAlso() {
		return seeAlso;
	}

	public void setSeeAlso(String seeAlso) {
		this.seeAlso = seeAlso;
	}

	public GluuStatus getStatus() {
		return status;
	}

	public void setStatus(GluuStatus status) {
		this.status = status;
	}

	public String getCommunityAttribute() {
		return communityAttribute;
	}

	public void setCommunityAttribute(String communityAttribute) {
		this.communityAttribute = communityAttribute;
	}

	public String getManagerGroup() {
		return managerGroup;
	}

	public void setManagerGroup(String managerGroup) {
		this.managerGroup = managerGroup;
	}

	public String getOwnerGroup() {
		return ownerGroup;
	}

	public void setOwnerGroup(String ownerGroup) {
		this.ownerGroup = ownerGroup;
	}

	public String getLogoImage() {
		return logoImage;
	}

	public void setLogoImage(String logoImage) {
		this.logoImage = logoImage;
	}

	public String getThemeColor() {
		return themeColor;
	}

	public void setThemeColor(String themeColor) {
		this.themeColor = themeColor;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String[] getCustomMessages() {
		return customMessages;
	}

	public void setCustomMessages(String[] customMessages) {
		this.customMessages = customMessages;
	}

	public String getFaviconImage() {
		return faviconImage;
	}

	public void setFaviconImage(String faviconImage) {
		this.faviconImage = faviconImage;
	}

	public void setTempFaviconImage(String tempFaviconImage) {
		this.tempFaviconImage = tempFaviconImage;
	}

	public String getTempFaviconImage() {
		return this.tempFaviconImage;
	}

	public void setScimStatus(GluuStatus scimStatus) {
		this.scimStatus = scimStatus;
	}

	public GluuStatus getScimStatus() {
		return this.scimStatus;
	}

	public void setScimAuthMode(String scimAuthMode) {
		this.scimAuthMode = scimAuthMode;
	}

	public String getScimAuthMode() {
		return this.scimAuthMode;
	}

	public void setScimGroup(String scimGroup) {
		this.scimGroup = scimGroup;
	}

	public String getScimGroup() {
		return this.scimGroup;
	}

	public String getOxInumConfig() {
		return this.oxInumConfig;
	}

	public void setOxInumConfig(String oxInumConfig) {
		this.oxInumConfig = oxInumConfig;
	}

	public String getOrganizationTitle() {
		if (title == null || title.trim().equals("")) {
			return "Gluu";
		}
		return title;
	}

	@Override
	public String toString() {
		return "GluuOrganization [inum=" + inum + ", iname=" + iname + ", displayName=" + displayName + ", description=" + description
				+ ", member=" + member + ", countryName=" + countryName + ", organization=" + organization + ", seeAlso=" + seeAlso
				+ ", status=" + status + ", communityAttribute=" + communityAttribute + ", managerGroup=" + managerGroup + ", ownerGroup="
				+ ownerGroup + ", logoImage=" + logoImage + ", themeColor=" + themeColor + ", shortName=" + shortName + ", customMessages="
				+ Arrays.toString(customMessages) + ", faviconImage=" + faviconImage + ", tempFaviconImage=" + tempFaviconImage
				+ ", scimStatus=" + scimStatus + ", scimAuthMode=" + scimAuthMode + ", scimGroup=" + scimGroup + ", oxInumConfig="
				+ oxInumConfig + ", toString()=" + super.toString() + "]";
	}

}
