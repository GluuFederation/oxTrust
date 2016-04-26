/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model;

import java.io.Serializable;

public class ProfileConfiguration implements Serializable {

	private static final long serialVersionUID = 7083971450893323016L;
        
	private String name;
	private boolean includeAttributeStatement;
	private String signResponses;
	private String signAssertions;
	private String signRequests;
	private int assertionLifetime;
	private int assertionProxyCount;
	private String encryptNameIds;
	private String encryptAssertions;
	private String profileConfigurationCertFileName;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isIncludeAttributeStatement() {
		return includeAttributeStatement;
	}

	public void setIncludeAttributeStatement(boolean includeAttributeStatement) {
		this.includeAttributeStatement = includeAttributeStatement;
	}

	public String getSignResponses() {
		return signResponses;
	}

	public void setSignResponses(String signResponses) {
		this.signResponses = signResponses;
	}

	public String getSignAssertions() {
		return signAssertions;
	}

	public void setSignAssertions(String signAssertions) {
		this.signAssertions = signAssertions;
	}

	public String getSignRequests() {
		return signRequests;
	}

	public void setSignRequests(String signRequests) {
		this.signRequests = signRequests;
	}

	public int getAssertionLifetime() {
		return assertionLifetime;
	}

	public void setAssertionLifetime(int assertionLifetime) {
		this.assertionLifetime = assertionLifetime;
	}

	public int getAssertionProxyCount() {
		return assertionProxyCount;
	}

	public void setAssertionProxyCount(int assertionProxyCount) {
		this.assertionProxyCount = assertionProxyCount;
	}

	public String getEncryptNameIds() {
		return encryptNameIds;
	}

	public void setEncryptNameIds(String encryptNameIds) {
		this.encryptNameIds = encryptNameIds;
	}

	public String getEncryptAssertions() {
		return encryptAssertions;
	}

	public void setEncryptAssertions(String encryptAssertions) {
		this.encryptAssertions = encryptAssertions;
	}

	public String getProfileConfigurationCertFileName() {
		return profileConfigurationCertFileName;
	}

	public void setProfileConfigurationCertFileName(
			String profileConfigurationCertFileName) {
		this.profileConfigurationCertFileName = profileConfigurationCertFileName;
	}

	@Override
	public String toString() {
		return String.format("ProfileConfiguration [name=%s, includeAttributeStatement=%s, signResponses=%s, signAssertions=%s, signRequests=%s, assertionLifetime=%s, assertionProxyCount=%s, encryptNameIds=%s, encryptAssertions=%s, profileConfigurationCertFileName=%s]", getName(), isIncludeAttributeStatement(), getSignResponses(), getSignAssertions(), getSignRequests(), getAssertionLifetime(), getAssertionProxyCount(), getEncryptNameIds(), getEncryptAssertions(), getProfileConfigurationCertFileName());
	}

}
