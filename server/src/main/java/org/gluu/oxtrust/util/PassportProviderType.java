package org.gluu.oxtrust.util;

public enum PassportProviderType {
	OPENID_OXD("openidconnect-oxd"), SAML("saml"), OAUTH("oauth"), OPENID("openidconnect");

	private String properValue;

	private PassportProviderType(final String value) {
		this.properValue = value;
	}

	public String getProperValue() {
		return this.properValue;
	}
}
