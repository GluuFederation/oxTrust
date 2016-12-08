package org.gluu.oxtrust.model.passport;

/**
 * @author Shekhar L.
 * @Date 07/17/2016
 */

public class PassportStrategy {

	private String clientId;
	private String clientSecret;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
}
