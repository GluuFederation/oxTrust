/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2.provider;

/**
 * This class represents the AuthenticationSchemes complex attribute in the
 * Service Provider Config.
 */
public class AuthenticationScheme {

	private String name;
	private String description;
	private String specUrl;
	private String documentationUrl;
	private String type;
	private boolean primary;

	/**
	 * Create a value of the SCIM AuthenticationSchemes attribute.
	 *
	 * @param name
	 *            The name of the Authentication Scheme.
	 * @param description
	 *            The description of the Authentication Scheme.
	 * @param specUrl
	 *            A HTTP addressable URL pointing to the Authentication Scheme's
	 *            specification.
	 * @param documentationUrl
	 *            A HTTP addressable URL pointing to the Authentication Scheme's
	 *            usage documentation.
	 * @param type
	 *            The type of Authentication Scheme.
	 * @param primary
	 *            Specifies whether this value is the primary value.
	 */
	public AuthenticationScheme(final String name, final String description,
			final String specUrl, final String documentationUrl,
			final String type, final boolean primary) {
		this.name = name;
		this.description = description;
		this.specUrl = specUrl;
		this.documentationUrl = documentationUrl;
		this.primary = primary;
		this.type = type;
	}

	/**
	 * Retrieves the name of the Authentication Scheme.
	 *
	 * @return The name of the Authentication Scheme.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the Authentication Scheme.
	 *
	 * @param name
	 *            The name of the Authentication Scheme.
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Retrieves the description of the Authentication Scheme.
	 *
	 * @return The description of the Authentication Scheme.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of the Authentication Scheme.
	 *
	 * @param description
	 *            The description of the Authentication Scheme.
	 */
	public void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * Retrieves the HTTP addressable URL pointing to the Authentication
	 * Scheme's specification.
	 *
	 * @return The the HTTP addressable URL pointing to the Authentication
	 *         Scheme's specification, or {@code null} if there is none.
	 */
	public String getSpecUrl() {
		return specUrl;
	}

	/**
	 * Sets the HTTP addressable URL pointing to the Authentication Scheme's
	 * specification.
	 * 
	 * @param specUrl
	 *            The HTTP addressable URL pointing to the Authentication
	 *            Scheme's specification.
	 */
	public void setSpecUrl(final String specUrl) {
		this.specUrl = specUrl;
	}

	/**
	 * Retrieves the HTTP addressable URL pointing to the Authentication
	 * Scheme's usage documentation.
	 * 
	 * @return The HTTP addressable URL pointing to the Authentication Scheme's
	 *         usage documentation.
	 */
	public String getDocumentationUrl() {
		return documentationUrl;
	}

	/**
	 * Sets the HTTP addressable URL pointing to the Authentication Scheme's
	 * usage documentation.
	 * 
	 * @param documentationUrl
	 *            The HTTP addressable URL pointing to the Authentication
	 *            Scheme's usage documentation.
	 */
	public void setDocumentationUrl(final String documentationUrl) {
		this.documentationUrl = documentationUrl;
	}

	/**
	 * Indicates whether this value is the primary value.
	 *
	 * @return <code>true</code> if this value is the primary value or
	 *         <code>false</code> otherwise.
	 */
	public boolean isPrimary() {
		return primary;
	}

	/**
	 * Specifies whether this value is the primary value.
	 *
	 * @param primary
	 *            Whether this value is the primary value.
	 */
	public void setPrimary(final boolean primary) {
		this.primary = primary;
	}

	/**
	 * Retrieves the type of Authentication Scheme.
	 *
	 * @return The type of Authentication Scheme.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type of Authentication Scheme.
	 *
	 * @param type
	 *            The type of Authentication Scheme.
	 */
	public void setType(final String type) {
		this.type = type;
	}
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("AuthenticationScheme");
		sb.append("{name='").append(name).append('\'');
		sb.append(", description='").append(description).append('\'');
		sb.append(", specUrl='").append(specUrl).append('\'');
		sb.append(", documentationUrl='").append(documentationUrl).append('\'');
		sb.append(", type='").append(type).append('\'');
		sb.append(", primary=").append(primary);
		sb.append('}');
		return sb.toString();
	}

	/**
	 * Convenience method that creates a new AuthenticationScheme instances for
	 * HTTP BASIC.
	 *
	 * @param primary
	 *            Whether this authentication scheme is primary
	 *
	 * @return A new AuthenticationScheme instances for HTTP BASIC.
	 */
	public static AuthenticationScheme createBasic(final boolean primary) {
		return new AuthenticationScheme(
				"Http Basic",
				"The HTTP Basic Access Authentication scheme. This scheme is not "
						+ "considered to be a secure method of user authentication (unless "
						+ "used in conjunction with some external secure system such as "
						+ "SSL), as the user name and password are passed over the network "
						+ "as cleartext.",
				"http://www.ietf.org/rfc/rfc2617.txt",
				"http://en.wikipedia.org/wiki/Basic_access_authentication",
				"httpbasic", primary);
	}

	/**
	 * Convenience method that creates a new AuthenticationScheme instances for
	 * OAuth 2.
	 *
	 * @param primary
	 *            Whether this authentication scheme is primary
	 *
	 * @return A new AuthenticationScheme instances for OAuth 2.
	 */
	public static AuthenticationScheme createOAuth2(final boolean primary) {
		return new AuthenticationScheme(
				"OAuth 2.0", "OAuth2 Access Token Authentication Scheme. Enabled only on 'SCIM Test Mode'.",
				"http://tools.ietf.org/html/rfc6749", "https://gluu.org/docs/integrate/oauth2grants/",
				"oauth2", primary);
	}
	/*
	public static AuthenticationScheme createOAuth2(final boolean primary) {
		return new AuthenticationScheme(
				"OAuth 2.0",
				"The OAuth 2.0 Bearer Token Authentication scheme. OAuth enables "
						+ "clients to access protected resources by obtaining an access "
						+ "token, which is defined in RFC 6750 as \"a string "
						+ "representing an access authorization issued to the client\", "
						+ "rather than using the resource owner's credentials directly.",
				"http://tools.ietf.org/html/rfc6750", "http://oauth.net/2/",
				"oauth2", primary);
	}
	*/

	public static AuthenticationScheme createUma(final boolean primary) {
		return new AuthenticationScheme(
				"UMA 1.0.1", "UMA Authentication Scheme",
				"https://kantarainitiative.org/confluence/display/uma/UMA+Protocol", "https://gluu.org/docs/integrate/uma/",
				"uma", primary);
	}
}
