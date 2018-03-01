package org.gluu.oxtrust.model.passport;

/**
 * @author Shekhar L.
 * @version 07/17/2016
 */
public class PassportStrategy {

	private String clientID;
	private String clientSecret;

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
}
