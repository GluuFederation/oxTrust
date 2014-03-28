package org.gluu.oxtrust.model;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;
import org.xdi.ldap.model.Entry;

/**
 * oxAuth client
 * 
 * @author Reda Zerrad Date: 06.18.2012
 */
@LdapEntry(sortBy = { "displayName" })
@LdapObjectClass(values = { "top", "oxAuthCustomScope" })
public class OxAuthScope extends Entry implements Serializable {

	/**
     *
     */
	private static final long serialVersionUID = 4308826784917052508L;

	private transient boolean selected;

	private transient boolean isDefault;

	@LdapAttribute(ignoreDuringUpdate = true)
	private String inum;

	@NotNull
	@Size(min = 0, max = 60, message = "Length of the Display Name should not exceed 60")
	@LdapAttribute
	private String displayName;

	@Size(min = 0, max = 4000, message = "Length of the Description should not exceed 4000")
	@LdapAttribute
	private String description;

	@LdapAttribute(name = "oxAuthClaim")
	private List<String> oxAuthClaims;

	@LdapAttribute(name = "defaultScope")
	private String defaultScope;

	public String getInum() {
		return this.inum;
	}

	public void setInum(String inum) {
		this.inum = inum;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getOxAuthClaims() {
		return this.oxAuthClaims;
	}

	public void setOxAuthClaims(List<String> oxAuthClaims) {
		this.oxAuthClaims = oxAuthClaims;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getDefaultScope() {
		return this.defaultScope;
	}

	public void setDefaultScope(String defaultScope) {
		this.defaultScope = defaultScope;
	}

	public boolean getIsDefault() {
		if (this.defaultScope == null) {
			return false;
		}
		if (this.defaultScope.equalsIgnoreCase("true")) {
			this.isDefault = true;
			return this.isDefault;
		}
		this.isDefault = false;
		return this.isDefault;
	}

	@Override
	public String toString() {
		return String.format("oxAuthScope [description=%s, displayName=%s, inum=%s, oxAuthClaims=%s, defaultScope=%s, toString()=%s]",
				description, displayName, inum, oxAuthClaims, defaultScope, super.toString());
	}
}
