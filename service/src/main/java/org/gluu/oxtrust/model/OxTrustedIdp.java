/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.gluu.persist.annotation.AttributeName;
import org.gluu.persist.annotation.DataEntry;
import org.gluu.persist.annotation.ObjectClass;
import org.gluu.persist.model.base.Entry;
import java.io.Serializable;

/**
 * oxTrustedIdp
 *
 * @author Shekhar L.
 * 
 */
@DataEntry(sortBy = { "remoteIdpName" })
@ObjectClass(value = "oxTrustedIdp")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OxTrustedIdp extends Entry implements Serializable {

	private static final long serialVersionUID = -2310140703735705346L;

	private transient boolean selected;

	@AttributeName(ignoreDuringUpdate = true)
	private String inum;

	@AttributeName
	private String remoteIdpName;

	@AttributeName
	private String remoteIdpHost;
	
	@AttributeName(name = "selectedSingleSignOnService")
	private String selectedSingleSignOnService;
	
	@AttributeName(name = "supportedSingleSignOnServices")
	private String supportedSingleSignOnServices;
	
	@AttributeName(name = "signingCertificates")
	private String signingCertificates;

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getInum() {
		return inum;
	}

	public void setInum(String inum) {
		this.inum = inum;
	}

	public String getRemoteIdpName() {
		return remoteIdpName;
	}

	public void setRemoteIdpName(String remoteIdpName) {
		this.remoteIdpName = remoteIdpName;
	}

	public String getRemoteIdpHost() {
		return remoteIdpHost;
	}

	public void setRemoteIdpHost(String remoteIdpHost) {
		this.remoteIdpHost = remoteIdpHost;
	}

	public String getSelectedSingleSignOnService() {
		return selectedSingleSignOnService;
	}

	public void setSelectedSingleSignOnService(String selectedSingleSignOnService) {
		this.selectedSingleSignOnService = selectedSingleSignOnService;
	}

	public String getSupportedSingleSignOnServices() {
		return supportedSingleSignOnServices;
	}

	public void setSupportedSingleSignOnServices(String supportedSingleSignOnServices) {
		this.supportedSingleSignOnServices = supportedSingleSignOnServices;
	}

	public String getSigningCertificates() {
		return signingCertificates;
	}

	public void setSigningCertificates(String signingCertificates) {
		this.signingCertificates = signingCertificates;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	
}
