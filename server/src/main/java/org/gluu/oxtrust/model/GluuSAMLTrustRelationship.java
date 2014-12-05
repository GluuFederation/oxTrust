/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Data;

import org.gluu.oxtrust.ldap.service.TrustService;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;
import org.xdi.ldap.model.GluuStatus;
import org.xdi.ldap.model.InumEntry;

@LdapEntry
@LdapObjectClass(values = { "top", "gluuSAMLconfig" })
public @Data class GluuSAMLTrustRelationship extends InumEntry implements Serializable {

	private static final long serialVersionUID = 5907443836820485369L;

	@LdapAttribute(ignoreDuringUpdate = true)
	private String inum;

	@LdapAttribute(ignoreDuringUpdate = true)
	private String iname;

	@NotNull
	@Size(min = 0, max = 60, message = "Length of the Display Name should not exceed 60")
	@LdapAttribute
	private String displayName;

	@NotNull
	@Size(min = 0, max = 4000, message = "Length of the Description should not exceed 4000")
	@LdapAttribute
	private String description;

	@LdapAttribute(name = "gluuStatus")
	private GluuStatus status;

	@LdapAttribute(name = "gluuValidationStatus")
	private GluuValidationStatus validationStatus;

	@LdapAttribute(name = "gluuReleasedAttribute")
	private List<String> releasedAttributes;

	@NotNull
	@LdapAttribute(name = "gluuSAMLspMetaDataSourceType")
	private GluuMetadataSourceType spMetaDataSourceType;

	@LdapAttribute(name = "gluuSAMLspMetaDataFN")
	private String spMetaDataFN;

	@LdapAttribute(name = "gluuSAMLspMetaDataURL")
	private String spMetaDataURL;

	@LdapAttribute(name = "o")
	private String owner;

	@LdapAttribute(name = "gluuSAMLmaxRefreshDelay")
	private String maxRefreshDelay;

	@Transient
	private transient List<GluuCustomAttribute> releasedCustomAttributes = new ArrayList<GluuCustomAttribute>();

	private Map<String, MetadataFilter> metadataFilters = new HashMap<String, MetadataFilter>();

	private Map<String, ProfileConfiguration> profileConfigurations = new HashMap<String, ProfileConfiguration>();

	@LdapAttribute(name = "gluuSAMLMetaDataFilter")
	private List<String> gluuSAMLMetaDataFilter;

	@LdapAttribute(name = "gluuTrustContact")
	private List<String> gluuTrustContact;

	private List<DeconstructedTrustRelationship> deconstructedTrustRelationships = new ArrayList<DeconstructedTrustRelationship>();

	@LdapAttribute(name = "gluuTrustDeconstruction")
	private List<String> gluuTrustDeconstruction;

	@LdapAttribute(name = "gluuContainerFederation")
	protected String gluuContainerFederation;

	@LdapAttribute(name = "gluuIsFederation")
	private String gluuIsFederation;

	@LdapAttribute(name = "gluuEntityId")
	private List<String> gluuEntityId;

	@LdapAttribute(name = "gluuProfileConfiguration")
	private List<String> gluuProfileConfiguration;

	@LdapAttribute(name = "gluuSpecificRelyingPartyConfig")
	private String gluuSpecificRelyingPartyConfig;

	@Pattern(regexp="^(http|https)\\://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,4}(:[a-zA-Z0-9]*)?/?$", message = "Please enter a valid SP url, including protocol (http/https)")
	@LdapAttribute(name = "url")
	private String url;
	
	@Pattern(regexp="(^$)|(^(http|https)\\://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,4}(:[a-zA-Z0-9]*)?/?$)", message = "Please enter a valid url, including protocol (http/https)")
	@LdapAttribute(name = "oxAuthPostLogoutRedirectURI")
	private String spLogoutURL;

	@LdapAttribute(name = "gluuValidationLog")
	private List<String> validationLog;

	

	public void setFederation(boolean isFederation) {
		this.gluuIsFederation = Boolean.toString(isFederation);
	}

	public boolean isFederation() {
		return Boolean.parseBoolean(gluuIsFederation);
	}

	public void setContainerFederation(GluuSAMLTrustRelationship containerFederation) {
		this.gluuContainerFederation = containerFederation.getDn();
	}

	public GluuSAMLTrustRelationship getContainerFederation() {
		return TrustService.instance().getRelationshipByDn(this.gluuContainerFederation);
	}


	@Override
	public boolean equals(Object o) {
		if (!(o instanceof GluuSAMLTrustRelationship)) {
			return false;
		}

		if (getInum() == null) {
			return getInum() == ((GluuSAMLTrustRelationship) o).getInum();
		}

		return getInum().equals(((GluuSAMLTrustRelationship) o).getInum());
	}

	public void setGluuEntityId(Set<String> gluuEntityId) {
		this.gluuEntityId = new ArrayList<String>(gluuEntityId);
	}
	/**
	 * This method is for ldap persistance only. For purposes of crud - please use setGluuEntityId(Set<String> gluuEntityId)
	 */
	@Deprecated
	public void setGluuEntityId(List<String> gluuEntityId) {
		this.gluuEntityId = gluuEntityId;
	}


	/**
	 * This method returns entityId for site TRs only.
	 */
	public String getEntityId() {
		if ((gluuEntityId != null) && (gluuEntityId.size() == 1)) {
			return gluuEntityId.get(0);
		}
		return null;
	}

	public void setEntityId(String entityId) {
		Set<String> entityIds = new TreeSet<String>();
		if (entityId != null) {
			entityIds.add(entityId);
		}
		setGluuEntityId(entityIds);
	}


	public void setSpecificRelyingPartyConfig(boolean specificRelyingPartyConfig) {
		this.gluuSpecificRelyingPartyConfig = Boolean.toString(specificRelyingPartyConfig);
	}

	public boolean getSpecificRelyingPartyConfig() {
		return Boolean.parseBoolean(gluuSpecificRelyingPartyConfig);
	}

}
