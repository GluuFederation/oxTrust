/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

import org.gluu.model.GluuStatus;
import org.gluu.persist.annotation.AttributeName;
import org.gluu.persist.annotation.DataEntry;
import org.gluu.persist.annotation.ObjectClass;
import org.gluu.persist.model.base.InumEntry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import com.fasterxml.jackson.annotation.JsonInclude.Include;

@DataEntry
@ObjectClass(value = "gluuSAMLconfig")
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({GluuEntityType.class, GluuMetadataSourceType.class, GluuStatus.class, GluuValidationStatus.class, 
    GluuCustomAttribute.class, MetadataFilter.class, ProfileConfiguration.class, DeconstructedTrustRelationship.class})
public class GluuSAMLTrustRelationship extends InumEntry implements Serializable {

	private static final long serialVersionUID = 5907443836820485369L;

	@AttributeName(ignoreDuringUpdate = true)
	private String inum;

	@NotNull
	@Size(min = 0, max = 60, message = "Length of the Display Name should not exceed 60")
	@AttributeName
	private String displayName;

	@NotNull
	@Size(min = 0, max = 4000, message = "Length of the Description should not exceed 4000")
	@AttributeName
	private String description;

	@AttributeName(name = "gluuStatus")
	private GluuStatus status;

	@AttributeName(name = "gluuValidationStatus")
	private GluuValidationStatus validationStatus;

	@AttributeName(name = "gluuReleasedAttribute")
	private List<String> releasedAttributes;

	@NotNull
	@AttributeName(name = "gluuSAMLspMetaDataSourceType")
	private GluuMetadataSourceType spMetaDataSourceType;

	@AttributeName(name = "gluuSAMLspMetaDataFN")
	private String spMetaDataFN;

	@AttributeName(name = "gluuSAMLspMetaDataURL")
	private String spMetaDataURL;

	@AttributeName(name = "o")
	private String owner;

	@AttributeName(name = "gluuSAMLmaxRefreshDelay")
	private String maxRefreshDelay;

	@Transient
	private transient List<GluuCustomAttribute> releasedCustomAttributes = new ArrayList<GluuCustomAttribute>();

	private Map<String, MetadataFilter> metadataFilters = new HashMap<String, MetadataFilter>();

	private Map<String, ProfileConfiguration> profileConfigurations = new HashMap<String, ProfileConfiguration>();

	@AttributeName(name = "gluuSAMLMetaDataFilter")
	private List<String> gluuSAMLMetaDataFilter;

	@AttributeName(name = "gluuTrustContact")
	private List<String> gluuTrustContact;

	//private List<DeconstructedTrustRelationship> deconstructedTrustRelationships = new ArrayList<DeconstructedTrustRelationship>();

	@AttributeName(name = "gluuTrustDeconstruction")
	private List<String> gluuTrustDeconstruction;

	@AttributeName(name = "gluuContainerFederation")
	protected String gluuContainerFederation;

	@AttributeName(name = "gluuIsFederation")
	private String gluuIsFederation;

	@AttributeName(name = "gluuEntityId")
	private List<String> gluuEntityId;

	@AttributeName(name = "gluuProfileConfiguration")
	private List<String> gluuProfileConfiguration;

	@AttributeName(name = "gluuSpecificRelyingPartyConfig")
	private String gluuSpecificRelyingPartyConfig;

	@Pattern(regexp = "^(https?|http)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", message = "Please enter a valid SP url, including protocol (http/https)")
	@AttributeName(name = "url")
	private String url;

	@Pattern(regexp = "^$|(^(https?|http)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])", message = "Please enter a valid url, including protocol (http/https)")
	@AttributeName(name = "oxAuthPostLogoutRedirectURI")
	private String spLogoutURL;

	@Pattern(regexp = "^$|(^(https?|http)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])", message = "Please enter a valid url, including protocol (http/https)")
	@AttributeName(name="spLogoutRedirectUrl")
	private String spLogoutRedirectUrl;

	@AttributeName(name = "gluuValidationLog")
	private List<String> validationLog;

	@AttributeName(name = "researchAndScholarshipEnabled")
	private String researchBundleEnabled;

	@AttributeName(name = "gluuEntityType")
	private GluuEntityType entityType;
	
	private String metadataStr;
	
	private String certificate;


	public String getCertificate() {
		return certificate;
	}

	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

	public String getMetadataStr() {
		return metadataStr;
	}

	public void setMetadataStr(String metadataStr) {
		this.metadataStr = metadataStr;
	}

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

	/*public void setGluuEntityId(Set<String> gluuEntityId) {
		this.gluuEntityId = new ArrayList<String>(gluuEntityId);
	}*/
	public void setUniqueGluuEntityId(Set<String> gluuEntityId) {
		this.gluuEntityId = new ArrayList<String>(gluuEntityId);
	}

	
	@Deprecated
	public void setGluuEntityId(List<String> gluuEntityId) {
		this.gluuEntityId = gluuEntityId;
	}

	
	public String getEntityId() {
		if (this.gluuEntityId != null && !this.gluuEntityId.isEmpty()) {
			return this.gluuEntityId.get(0);
		}
		return "";
	}

	public void setEntityId(String entityId) {
		Set<String> entityIds = new TreeSet<String>();
		if (entityId != null) {
			entityIds.add(entityId);
		}
		setUniqueGluuEntityId(entityIds);
	}

	public void setSpecificRelyingPartyConfig(boolean specificRelyingPartyConfig) {
		this.gluuSpecificRelyingPartyConfig = Boolean.toString(specificRelyingPartyConfig);
	}

	public boolean getSpecificRelyingPartyConfig() {
		return Boolean.parseBoolean(gluuSpecificRelyingPartyConfig);
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
		return this.gluuContainerFederation;
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

	public String getSpLogoutRedirectUrl() {
		
		
		return spLogoutRedirectUrl;
	}

	public void setSpLogoutRedirectUrl(String spLogoutRedirectUrl) {

		this.spLogoutRedirectUrl = spLogoutRedirectUrl;
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

	public boolean entityTypeIsFederation() {

		return (this.entityType == GluuEntityType.FederationAggregate);
	}

	public boolean entityTypeIsSingleSp() {

		return (this.entityType == GluuEntityType.SingleSP);
	}

	public boolean isFileMetadataSourceType() {

		return (this.spMetaDataSourceType == GluuMetadataSourceType.FILE);
	}

	public boolean isUriMetadataSourceType() {

		return (this.spMetaDataSourceType == GluuMetadataSourceType.URI);
	}

	public boolean isMdqMetadataSourceType() {

		return (this.spMetaDataSourceType == GluuMetadataSourceType.MDQ);
	}

	public boolean isMdqFederation() {

		return (this.entityType == GluuEntityType.FederationAggregate) && (this.spMetaDataSourceType == GluuMetadataSourceType.MDQ);
	}

	private static class SortByDatasourceTypeComparator implements Comparator<GluuSAMLTrustRelationship> {

		public int compare(GluuSAMLTrustRelationship first, GluuSAMLTrustRelationship second) {

			return first.getSpMetaDataSourceType().getRank() - second.getSpMetaDataSourceType().getRank();
		}
	}

	public static void sortByDataSourceType(List<GluuSAMLTrustRelationship> trustRelationships) {
		Collections.sort(trustRelationships,new SortByDatasourceTypeComparator());
	}
}