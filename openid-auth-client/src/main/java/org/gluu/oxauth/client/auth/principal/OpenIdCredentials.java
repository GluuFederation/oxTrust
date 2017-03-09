/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxauth.client.auth.principal;

import java.io.Serializable;

import org.gluu.oxauth.client.auth.user.UserProfile;

/**
 * This class represents client credentials and user profile
 * 
 * @author Yuriy Movchan 11/14/2014
 */
public final class OpenIdCredentials implements Serializable  {

	private static final long serialVersionUID = -7368677422769694487L;

	private String clientName;

	private final String authorizationCode;

	private UserProfile userProfile;

	public OpenIdCredentials(final String authorizationCode) {
		this.authorizationCode = authorizationCode;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(final String clientName) {
		this.clientName = clientName;
	}

	public String getAuthorizationCode() {
		return authorizationCode;
	}

	public UserProfile getUserProfile() {
		return userProfile;
	}

	public void setUserProfile(final UserProfile theUserProfile) {
		this.userProfile = theUserProfile;
	}

	public String getId() {
		if (this.userProfile != null) {
			return this.userProfile.getId();
		}

		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OpenIdCredentials [clientName=").append(clientName).append(", authorizationCode=").append(authorizationCode).append(", userProfile=")
				.append(userProfile).append(", getId()=").append(getId()).append("]");
		return builder.toString();
	}

}
