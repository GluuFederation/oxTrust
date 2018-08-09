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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.gluu.persist.model.base.InumEntry;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;
import org.gluu.site.ldap.persistence.annotation.LdapEntry;
import org.gluu.site.ldap.persistence.annotation.LdapObjectClass;
import org.xdi.model.GluuStatus;

import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonIgnore;

@LdapEntry
@LdapObjectClass(values = { "top", "gluuSAMLconfig" })

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({GluuEntityType.class, GluuMetadataSourceType.class, GluuStatus.class, GluuValidationStatus.class, 
    GluuCustomAttribute.class, MetadataFilter.class, ProfileConfiguration.class, DeconstructedTrustRelationship.class})
public class GluuSAMLTrustRelationship extends InumEntry implements Serializable {

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

	@Pattern(regexp="^(https?|http)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", message = "Please enter a valid SP url, including protocol (http/https)")
	@LdapAttribute(name = "url")
	private String url;
	
	@Pattern(regexp="^$|(^(https?|http)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])", message = "Please enter a valid url, including protocol (http/https)")
	@LdapAttribute(name = "oxAuthPostLogoutRedirectURI")
	private String spLogoutURL;

	@LdapAttribute(name = "gluuValidationLog")
	private List<String> validationLog;

	@LdapAttribute(name = "researchAndScholarshipEnabled")
	private String researchBundleEnabled;
	
	@LdapAttribute(name = "gluuEntityType")
	private GluuEntityType entityType;

	

	public void setFederation(boolean isFederation) {
		this.gluuIsFederation = Boolean.toString(isFederation);
	}

	public boolean isFederation() {
		return Boolean.parseBoolean(gluuIsFederation);
	}

	public void setContainerFederation(GluuSAMLTrustRelationship containerFederation) {
		this.gluuContainerFederation = containerFederation.getDn();
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

	public List<String> getGluuEntityId() {
		return gluuEntityId;
	}
        
        //@com.fasterxml.jackson.annotation.JsonIgnore
        @JsonIgnore
        @XmlTransient
	public void setGluuEntityId(Set<String> gluuEntityId) {
		this.gluuEntityId = new ArrayList<String>(gluuEntityId);
	}
        
	/*
	 * This method is for ldap persistance only. For purposes of crud - plea00se use setGluuEntityId(Set<String> gluuEntityId)
	 */
	@Deprecated
	public void setGluuEntityId(List<String> gluuEntityId) {
		this.gluuEntityId = gluuEntityId;
	}


	/*
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

    public List<DeconstructedTrustRelationship> getDeconstructedTrustRelationships() {
        return deconstructedTrustRelationships;
    }

    public void setDeconstructedTrustRelationships(List<DeconstructedTrustRelationship> deconstructedTrustRelationships) {
        this.deconstructedTrustRelationships = deconstructedTrustRelationships;
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

    public String getGluuContainerFederation() {
        return gluuContainerFederation;
    }

    public void setGluuContainerFederation(String gluuContainerFederation) {
        this.gluuContainerFederation = gluuContainerFederation;
    }

    public String getGluuIsFederation() {
        return gluuIsFederation;
    }

    public void setGluuIsFederation(String gluuIsFederation) {
        this.gluuIsFederation = gluuIsFederation;
    }

    public List<String> getGluuProfileConfiguration() {
        return gluuProfileConfiguration;
    }

    public void setGluuProfileConfiguration(List<String> gluuProfileConfiguration) {
        this.gluuProfileConfiguration = gluuProfileConfiguration;
    }

    public List<String> getGluuSAMLMetaDataFilter() {
        return gluuSAMLMetaDataFilter;
    }

    public void setGluuSAMLMetaDataFilter(List<String> gluuSAMLMetaDataFilter) {
        this.gluuSAMLMetaDataFilter = gluuSAMLMetaDataFilter;
    }

    public String getGluuSpecificRelyingPartyConfig() {
        return gluuSpecificRelyingPartyConfig;
    }

    public void setGluuSpecificRelyingPartyConfig(String gluuSpecificRelyingPartyConfig) {
        this.gluuSpecificRelyingPartyConfig = gluuSpecificRelyingPartyConfig;
    }

    public List<String> getGluuTrustContact() {
        return gluuTrustContact;
    }

    public void setGluuTrustContact(List<String> gluuTrustContact) {
        this.gluuTrustContact = gluuTrustContact;
    }

    public List<String> getGluuTrustDeconstruction() {
        return gluuTrustDeconstruction;
    }

    public void setGluuTrustDeconstruction(List<String> gluuTrustDeconstruction) {
        this.gluuTrustDeconstruction = gluuTrustDeconstruction;
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

    public String getMaxRefreshDelay() {
        return maxRefreshDelay;
    }

    public void setMaxRefreshDelay(String maxRefreshDelay) {
        this.maxRefreshDelay = maxRefreshDelay;
    }

    public Map<String, MetadataFilter> getMetadataFilters() {
        return metadataFilters;
    }

    public void setMetadataFilters(Map<String, MetadataFilter> metadataFilters) {
        this.metadataFilters = metadataFilters;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Map<String, ProfileConfiguration> getProfileConfigurations() {
        return profileConfigurations;
    }

    public void setProfileConfigurations(Map<String, ProfileConfiguration> profileConfigurations) {
        this.profileConfigurations = profileConfigurations;
    }

    public List<String> getReleasedAttributes() {
        return releasedAttributes;
    }

    public void setReleasedAttributes(List<String> releasedAttributes) {
        this.releasedAttributes = releasedAttributes;
    }

    public List<GluuCustomAttribute> getReleasedCustomAttributes() {
        return releasedCustomAttributes;
    }

    public void setReleasedCustomAttributes(List<GluuCustomAttribute> releasedCustomAttributes) {
        this.releasedCustomAttributes = releasedCustomAttributes;
    }

    public String getSpLogoutURL() {
        return spLogoutURL;
    }

    public void setSpLogoutURL(String spLogoutURL) {
        this.spLogoutURL = spLogoutURL;
    }

    public String getSpMetaDataFN() {
        return spMetaDataFN;
    }

    public void setSpMetaDataFN(String spMetaDataFN) {
        this.spMetaDataFN = spMetaDataFN;
    }

    public GluuMetadataSourceType getSpMetaDataSourceType() {
        return spMetaDataSourceType;
    }

    public void setSpMetaDataSourceType(GluuMetadataSourceType spMetaDataSourceType) {
        this.spMetaDataSourceType = spMetaDataSourceType;
    }

    public String getSpMetaDataURL() {
        return spMetaDataURL;
    }

    public void setSpMetaDataURL(String spMetaDataURL) {
        this.spMetaDataURL = spMetaDataURL;
    }

    public GluuStatus getStatus() {
        return status;
    }

    public void setStatus(GluuStatus status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getValidationLog() {
        return validationLog;
    }

    public void setValidationLog(List<String> validationLog) {
        this.validationLog = validationLog;
    }

    public GluuValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(GluuValidationStatus validationStatus) {
        this.validationStatus = validationStatus;
    }

    public String getResearchBundleEnabled() {
        return researchBundleEnabled;
    }

    public void setResearchBundleEnabled(String researchBundleEnabled) {
        this.researchBundleEnabled = researchBundleEnabled;
    }

    
    public boolean isResearchBundle() {
        return Boolean.parseBoolean(researchBundleEnabled);
    }
    
    @JsonIgnore
    public boolean getResearchBundle() {
        return Boolean.parseBoolean(researchBundleEnabled);
    }
    
    
    public void setResearchBundle(boolean researchBundle) {
        this.researchBundleEnabled = Boolean.toString(researchBundle);
    }

	public GluuEntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(GluuEntityType entityType) {
		this.entityType = entityType;
	}
}
