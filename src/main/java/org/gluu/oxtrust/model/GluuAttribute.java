package org.gluu.oxtrust.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;
import org.xdi.ldap.model.Entry;
import org.xdi.ldap.model.GluuStatus;

/**
 * Attribute Metadata
 * 
 * @author Yuriy Movchan Date: 10.07.2010
 */
@LdapEntry(sortBy = { "displayName" })
@LdapObjectClass(values = { "top", "gluuAttribute" })
public class GluuAttribute extends Entry implements Serializable {

	private static final long serialVersionUID = 4817004894646725606L;

	private transient boolean selected;

	@LdapAttribute(ignoreDuringUpdate = true)
	private String inum;

	@NotNull
	@Pattern(regexp = "^[a-zA-Z_]+$", message = "Name should contain only letters and underscores")
	@Size(min = 1, max = 30, message = "Length of the Name should be between 1 and 30")
	@LdapAttribute(name = "gluuAttributeName")
	private String name;

	@NotNull
	@Size(min = 0, max = 60, message = "Length of the Display Name should not exceed 60")
	@LdapAttribute
	private String displayName;

	@NotNull
	@Size(min = 0, max = 4000, message = "Length of the Description should not exceed 4000")
	@LdapAttribute
	private String description;

	@LdapAttribute(name = "gluuAttributeOrigin")
	private String origin;

	@NotNull
	@LdapAttribute(name = "gluuAttributeType")
	private GluuAttributeDataType dataType;

	@NotNull
	@LdapAttribute(name = "gluuAttributeEditType")
	private GluuUserRole[] editType;

	@NotNull
	@LdapAttribute(name = "gluuAttributeViewType")
	private GluuUserRole[] viewType;

	@NotNull
	@LdapAttribute(name = "gluuAttributePrivacyLevel")
	private GluuAttributePrivacyLevel privacyLevel;

	@LdapAttribute(name = "gluuAttributeUsageType")
	private GluuAttributeUsageType[] usageType;

	@LdapAttribute(name = "seeAlso")
	private String seeAlso;

	@LdapAttribute(name = "gluuStatus")
	private GluuStatus status;

	@LdapAttribute(name = "gluuSAML1URI")
	private String saml1Uri;

	@LdapAttribute(name = "gluuSAML2URI")
	private String saml2Uri;

	@LdapAttribute(ignoreDuringUpdate = true)
	private String urn;

	@LdapAttribute(name = "oxSCIMCustomAttribute")
	private ScimCustomAtribute oxSCIMCustomAttribute;

	@LdapAttribute(name = "oxMultivaluedAttribute")
	private OxMultivalued oxMultivaluedAttribute;

	@Transient
	private boolean custom;

	@Transient
	private boolean requred;

	public String getInum() {
		return inum;
	}

	public void setInum(String inum) {
		this.inum = inum;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public GluuAttributeDataType getDataType() {
		return dataType;
	}

	public void setDataType(GluuAttributeDataType dataType) {
		this.dataType = dataType;
	}

	public GluuUserRole[] getEditType() {
		return editType;
	}

	public void setEditType(GluuUserRole[] editType) {
		this.editType = editType;
	}

	public GluuUserRole[] getViewType() {
		return viewType;
	}

	public void setViewType(GluuUserRole[] viewType) {
		this.viewType = viewType;
	}

	public GluuAttributePrivacyLevel getPrivacyLevel() {
		return privacyLevel;
	}

	public void setPrivacyLevel(GluuAttributePrivacyLevel privacyLevel) {
		this.privacyLevel = privacyLevel;
	}

	public GluuAttributeUsageType[] getUsageType() {
		return usageType;
	}

	public void setUsageType(GluuAttributeUsageType[] usageType) {
		this.usageType = usageType;
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

	public boolean isCustom() {
		return custom;
	}

	public void setCustom(boolean custom) {
		this.custom = custom;
	}

	public boolean allowEditBy(GluuUserRole role) {
		return GluuUserRole.containsRole(editType, role);
	}

	public boolean allowViewBy(GluuUserRole role) {
		return GluuUserRole.containsRole(viewType, role);
	}

	public boolean isAdminCanAccess() {
		return isAdminCanView() | isAdminCanEdit();
	}

	public boolean isAdminCanView() {
		return allowViewBy(GluuUserRole.ADMIN);
	}

	public boolean isAdminCanEdit() {
		return allowEditBy(GluuUserRole.ADMIN);
	}

	public boolean isUserCanAccess() {
		return isUserCanView() | isUserCanEdit();
	}

	public boolean isUserCanView() {
		return allowViewBy(GluuUserRole.USER);
	}

	public boolean isWhitePagesCanView() {
		return allowViewBy(GluuUserRole.WHITEPAGES);
	}

	public boolean isUserCanEdit() {
		return allowEditBy(GluuUserRole.USER);
	}

	public String getUrn() {
		return urn;
	}

	public void setUrn(String urn) {
		this.urn = urn;
	}

	public ScimCustomAtribute getOxSCIMCustomAttribute() {
		return this.oxSCIMCustomAttribute;
	}

	public void setOxSCIMCustomAttribute(ScimCustomAtribute oxSCIMCustomAttribute) {
		this.oxSCIMCustomAttribute = oxSCIMCustomAttribute;
	}

	public OxMultivalued getOxMultivaluedAttribute() {
		return this.oxMultivaluedAttribute;
	}

	public void setOxMultivaluedAttribute(OxMultivalued oxMultivaluedAttribute) {
		this.oxMultivaluedAttribute = oxMultivaluedAttribute;
	}

	public void setSaml1Uri(String saml1Uri) {
		this.saml1Uri = saml1Uri;
	}

	public String getSaml1Uri() {
		return saml1Uri;
	}

	public void setSaml2Uri(String saml2Uri) {
		this.saml2Uri = saml2Uri;
	}

	public String getSaml2Uri() {
		return saml2Uri;
	}

	public boolean isRequred() {
		return requred;
	}

	public void setRequred(boolean requred) {
		this.requred = requred;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GluuAttribute [inum=").append(inum).append(", name=").append(name).append(", displayName=").append(displayName)
				.append(", description=").append(description).append(", origin=").append(origin).append(", dataType=").append(dataType)
				.append(", editType=").append(Arrays.toString(editType)).append(", viewType=").append(Arrays.toString(viewType))
				.append(", privacyLevel=").append(privacyLevel).append(", usageType=").append(usageType).append(", seeAlso=")
				.append(seeAlso).append(", status=").append(status).append(", saml1Uri=").append(saml1Uri).append(", saml2Uri=")
				.append(saml2Uri).append(", urn=").append(urn).append(", oxSCIMCustomAttribute=").append(oxSCIMCustomAttribute)
				.append(", oxMultivaluedAttribute=").append(oxMultivaluedAttribute).append(", custom=").append(custom).append(", requred=")
				.append(requred).append(", toString()=").append(super.toString()).append("]");
		return builder.toString();
	}

}
