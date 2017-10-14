/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.model.scim2.provider;


import org.gluu.oxtrust.model.scim2.annotations.Attribute;
import org.gluu.oxtrust.model.scim2.AttributeDefinition;

/**
 * This class represents the AuthenticationSchemes complex attribute in the
 * Service Provider Config.
 */
public class AuthenticationScheme {

    @Attribute(description = "The authentication scheme.",
            isRequired = true,
            canonicalValues = {"oauth", "oauth2", "oauthbearertoken", "httpbasic", "httpdigest"},
            mutability = AttributeDefinition.Mutability.READ_ONLY)
    private String type;

    @Attribute(description = "The common authentication scheme name, e.g., HTTP Basic.",
            isRequired = true,
            mutability = AttributeDefinition.Mutability.READ_ONLY)
	private String name;

    @Attribute(description = "A description of the authentication scheme.",
            isRequired = true,
            mutability = AttributeDefinition.Mutability.READ_ONLY)
	private String description;

    @Attribute(description = "An HTTP-addressable URL pointing to the authentication scheme's specification.",
            mutability = AttributeDefinition.Mutability.READ_ONLY)
	private String specUri;

    @Attribute(description = "An HTTP-addressable URL pointing to the authentication scheme's usage documentation.",
            mutability = AttributeDefinition.Mutability.READ_ONLY)
	private String documentationUri;

    @Attribute(description = "Whether it's the preferred authentication scheme for service usage",
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            type = AttributeDefinition.Type.BOOLEAN)
    private boolean primary;

	/**
	 * Create a value of the SCIM AuthenticationSchemes attribute.
	 *
	 * @param name
	 *            The name of the Authentication Scheme.
	 * @param description
	 *            The description of the Authentication Scheme.
	 * @param specUri
	 *            A HTTP addressable URL pointing to the Authentication Scheme's
	 *            specification.
	 * @param documentationUri
	 *            A HTTP addressable URL pointing to the Authentication Scheme's
	 *            usage documentation.
	 * @param type
	 *            The type of Authentication Scheme.
     * @param primary
	 */
	public AuthenticationScheme(final String name, final String description, final String specUri,
                                final String documentationUri, final String type, final boolean primary) {
		this.name = name;
		this.description = description;
		this.specUri = specUri;
		this.documentationUri = documentationUri;
		this.type = type;
		this.primary=primary;
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
	 * Retrieves the description of the Authentication Scheme.
	 *
	 * @return The description of the Authentication Scheme.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Retrieves the HTTP addressable URL pointing to the Authentication
	 * Scheme's specification.
	 *
	 * @return The the HTTP addressable URL pointing to the Authentication
	 *         Scheme's specification, or {@code null} if there is none.
	 */
	public String getSpecUri() {
		return specUri;
	}

	/**
	 * Retrieves the HTTP addressable URL pointing to the Authentication
	 * Scheme's usage documentation.
	 * 
	 * @return The HTTP addressable URL pointing to the Authentication Scheme's
	 *         usage documentation.
	 */
	public String getDocumentationUri() {
		return documentationUri;
	}

	/**
	 * Retrieves the type of Authentication Scheme.
	 *
	 * @return The type of Authentication Scheme.
	 */
	public String getType() {
		return type;
	}

    public boolean isPrimary() {
        return primary;
    }

    /**
	 * Convenience method that creates a new AuthenticationScheme instances for
	 * HTTP BASIC.
	 * @return A new AuthenticationScheme instances for HTTP BASIC.
	 */
	public static AuthenticationScheme createBasic(boolean primary) {
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
	 * @return A new AuthenticationScheme instances for OAuth 2.
	 */
	public static AuthenticationScheme createOAuth2(boolean primary) {
		return new AuthenticationScheme(
				"OAuth 2.0", "OAuth2 Access Token Authentication Scheme. Enabled only on 'SCIM Test Mode'.",
				"http://tools.ietf.org/html/rfc6749", "https://www.gluu.org/docs/ce/admin-guide/user-scim/",
				"oauth2", primary);
	}

	/*
	public static AuthenticationScheme createOAuth2(boolean primary) {
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

	public static AuthenticationScheme createUma(boolean primary) {
		return new AuthenticationScheme(
				"UMA 2.0", "UMA Authentication Scheme",
				"https://docs.kantarainitiative.org/uma/ed/oauth-uma-grant-2.0-06.html", "https://www.gluu.org/docs/ce/admin-guide/uma/",
				"uma", primary);
	}

}
