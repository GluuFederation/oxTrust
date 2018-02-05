/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.gluu.persist.model.base.Entry;
import org.gluu.persist.model.base.GluuBoolean;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;
import org.xdi.oxauth.model.common.ScopeType;

/**
 * oxAuth scope
 * 
 * @author Reda Zerrad Date: 06/18/2012
 * @author Yuriy Movchan Date: 03/21/2014
 */
@LdapEntry(sortBy = { "displayName" })
@LdapObjectClass(values = { "top", "oxAuthCustomScope" })
public class OxAuthScope extends Entry implements Serializable {

	private static final long serialVersionUID = 4308826784917052508L;

	private transient boolean selected;

	@LdapAttribute(ignoreDuringUpdate = true)
	private String inum;

	@NotNull
	@Size(min = 0, max = 60, message = "Length of the Display Name should not exceed 60")
	@LdapAttribute
	private String displayName;

	@Size(min = 0, max = 4000, message = "Length of the Description should not exceed 4000")
	@LdapAttribute
	private String description;

    @LdapAttribute(name = "oxScopeType")
    private ScopeType scopeType;

	@LdapAttribute(name = "oxAuthClaim")
	private List<String> oxAuthClaims;

	@LdapAttribute(name = "defaultScope")
	private GluuBoolean defaultScope;

	@LdapAttribute(name = "oxScriptDn")
    private List<String> dynamicScopeScripts;

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

	public ScopeType getScopeType() {
		return scopeType;
	}

	public void setScopeType(ScopeType scopeType) {
		this.scopeType = scopeType;
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

	public GluuBoolean getDefaultScope() {
		return this.defaultScope;
	}

	public void setDefaultScope(GluuBoolean defaultScope) {
		this.defaultScope = defaultScope;
	}

    public List<String> getDynamicScopeScripts() {
		return dynamicScopeScripts;
	}

	public void setDynamicScopeScripts(List<String> dynamicScopeScripts) {
        this.dynamicScopeScripts = dynamicScopeScripts;
    }
	
	@Override
	public String toString() {
		return "OxAuthScope [inum=" + inum + ", displayName=" + displayName + ", description=" + description + ", oxAuthClaims="
				+ oxAuthClaims + ", defaultScope=" + defaultScope + "]";
	}
}
