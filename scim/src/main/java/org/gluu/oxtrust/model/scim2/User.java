/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;

import java.util.*;

/**
 * User model are meant to enable expression of common User informations. With the core attributes it should be
 * possible to express most user data. If more information need to be saved in a user object the user extension can be
 * used to store all customized data.
 * 
 * <p>
 * For more detailed information please look at the <a
 * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema 2.0, section 6</a>
 * </p>
 *
 * ===== IMPORTANT! =====
 * There might be JSON serializers/deserializers dependent on this class via reflection, most notably
 * org.gluu.oxtrust.service.scim2.schema.strategy.serializers.SchemaTypeUserSerializer and
 * org.gluu.oxtrust.service.antlr.scimFilter.util.ListResponseUserSerializer. You should consult these files first
 * before changing anything here.
 */
// @XmlRootElement(name = "User")
// @XmlAccessorType(XmlAccessType.PROPERTY)
public class User extends Resource {

    @LdapAttribute(name = "uid")
    private String userName;

    // See @LdapAttribute annotations in class
    private Name name;

    @LdapAttribute(name = "displayName")
    private String displayName;

    @LdapAttribute(name = "nickName")
    private String nickName;

    @LdapAttribute(name = "oxTrustProfileURL")
    private String profileUrl;

    @LdapAttribute(name = "oxTrustTitle")
    private String title;

    @LdapAttribute(name = "oxTrustUserType")
    private String userType;

    @LdapAttribute(name = "preferredLanguage")
    private String preferredLanguage;

    @LdapAttribute(name = "locale")
    private String locale;

    @LdapAttribute(name = "timezone")
    private String timezone;

    @LdapAttribute(name = "oxTrustActive")
    private Boolean active;

    @LdapAttribute(name = "password")
    private String password = "";

    @LdapAttribute(name = "oxTrustEmail")
    private List<Email> emails = new ArrayList<Email>();

    @LdapAttribute(name = "oxTrustPhoneValue")
    private List<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();

    @LdapAttribute(name = "oxTrustImsValue")
    private List<Im> ims = new ArrayList<Im>();

    @LdapAttribute(name = "oxTrustPhotos")
    private List<Photo> photos = new ArrayList<Photo>();

    @LdapAttribute(name = "oxTrustAddresses")
    private List<Address> addresses = new ArrayList<Address>();

    @LdapAttribute(name = "memberOf")
    private List<GroupRef> groups = new ArrayList<GroupRef>();

    @LdapAttribute(name = "oxTrustEntitlements")
    private List<Entitlement> entitlements = new ArrayList<Entitlement>();

    @LdapAttribute(name = "oxTrustRole")
    private List<Role> roles = new ArrayList<Role>();
    
    @LdapAttribute(name = "oxPPID")
    private List<String> pairwiseIdentitifers = new ArrayList<String>();

	public List<String> getPairwiseIdentitifers() {
		return pairwiseIdentitifers;
	}

	public void setPairwiseIdentitifers(List<String> pairwiseIdentitifers) {
		this.pairwiseIdentitifers = pairwiseIdentitifers;
	}

	@LdapAttribute(name = "oxTrustx509Certificate")
    private List<X509Certificate> x509Certificates = new ArrayList<X509Certificate>();

    private Map<String, Extension> extensions = new HashMap<String, Extension>();
	// private List<CustomAttributes> customAttributes = new ArrayList<CustomAttributes>();

    /**
     * Default constructor for Jackson
     */
    public User() {
    	Meta userMeta = new Meta();
    	userMeta.setResourceType("User");
    	setMeta(userMeta);
    	Set<String> userSchemas = new HashSet<String>();
    	userSchemas.add(Constants.USER_CORE_SCHEMA_ID);
		setSchemas(userSchemas);
    }

    /**
     * Gets the unique identifier for the User.
     * 
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema 2.0, section 6</a>
     * </p>
     * 
     * @return the user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Gets the components of the User's real name.
     * 
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema 2.0, section 6</a>
     * </p>
     * 
     * @return the real {@link Name} of the {@link User}
     */
    public Name getName() {
        return name;
    }

    /**
     * Gets the name of the User, suitable for display to end-users.
     * 
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema 2.0, section 6</a>
     * </p>
     * 
     * @return the display name of the {@link User}
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the casual way to address the user in real life,
     * 
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema 2.0, section 6</a>
     * </p>
     * 
     * @return the nickname of the {@link User}
     */
    public String getNickName() {
        return nickName;
    }

    /**
     * Gets a fully qualified URL to a page representing the User's online profile.
     * 
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema 2.0, section 6</a>
     * </p>
     * 
     * @return the progile URL of the {@link User}
     */
    public String getProfileUrl() {
        return profileUrl;
    }

    /**
     * The user's title, such as "Vice President."
     * 
     * @return the title of the {@link User}
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the type of the {@link User}
     * 
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema 2.0, section 6</a>
     * </p>
     * 
     * @return the type of the {@link User}
     */
    public String getUserType() {
        return userType;
    }

    /**
     * Gets the preferred written or spoken language of the User in ISO 3166-1 alpha 2 format, e.g. "DE" or "US".
     * 
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema 2.0, section 6</a>
     * </p>
     * 
     * @return the preferred language of the {@link User}
     */
    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    /**
     * Gets the default location of the User in ISO 639-1 two letter language code, e.g. 'de_DE' or 'en_US'
     * 
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema 2.0, section 6</a>
     * </p>
     * 
     * @return the default location of the {@link User}
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Gets the User's time zone in the "Olson" timezone database format
     * 
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema 2.0, section 6</a>
     * </p>
     * 
     * @return the time zone of the {@link User}
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * Gets a Boolean that indicates the User's administrative status.
     * 
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema 2.0, section 6</a>
     * </p>
     * 
     * @return the active status of the {@link User}
     */
    public Boolean isActive() {
        return active;
    }

    /**
     * Gets the password from the User.
     * 
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema 2.0, section 6</a>
     * </p>
     *
     * @return the password of the {@link User}
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets all E-mail addresses for the User.
     * 
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6.2">SCIM core schema 2.0, section
     * 6.2</a>
     * </p>
     * 
     * @return the email addresses of the {@link User}
     */
    public List<Email> getEmails() {
        return emails;
    }

    /**
     * Gets the phone numbers for the user.
     * 
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6.2">SCIM core schema 2.0, section
     * 6.2</a>
     * </p>
     * 
     * @return the phone numbers of the {@link User}
     */
    public List<PhoneNumber> getPhoneNumbers() {
        return phoneNumbers;
    }

    /**
     * Gets the instant messaging address for the user.
     * 
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6.2">SCIM core schema 2.0, section
     * 6.2</a>
     * </p>
     * 
     * @return the ims of the {@link User}
     */
    public List<Im> getIms() {
        return ims;
    }

    /**
     * Gets the URL's of the photos of the user.
     * 
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6.2">SCIM core schema 2.0, section
     * 6.2</a>
     * </p>
     * 
     * @return the photo URL's of the {@link User}
     */
    public List<Photo> getPhotos() {
        return photos;
    }

    /**
     * Gets the physical mailing addresses for this user.
     * 
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6.2">SCIM core schema 2.0, section
     * 6.2</a>
     * </p>
     * 
     * @return the addresses of the {@link User}
     */
    public List<Address> getAddresses() {
        return addresses;
    }

    /**
     * Gets a list of groups that the user belongs to.
     * 
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6.2">SCIM core schema 2.0, section
     * 6.2</a>
     * </p>
     * 
     * @return a list of all {@link Group}s where the {@link User} is a member of
     */
    public List<GroupRef> getGroups() {
        return groups;
    }

    /**
     * Gets a list of entitlements for the user that represent a thing the User has.
     * 
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6.2">SCIM core schema 2.0, section
     * 6.2</a>
     * </p>
     * 
     * @return a list of all entitlements of the {@link User}
     */
    public List<Entitlement> getEntitlements() {
        return entitlements;
    }

    /**
     * Gets a list of roles for the user that collectively represent who the User is e.g., 'Student', "Faculty"
     * 
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6.2">SCIM core schema 2.0, section
     * 6.2</a>
     * </p>
     * 
     * @return a list of the roles of the {@link User}
     */
    public List<Role> getRoles() {
        return roles;
    }

    /**
     * Gets a list of certificates issued to the user. Values are Binary and DER encoded x509.
     * 
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6.2">SCIM core schema 2.0, section
     * 6.2</a>
     * </p>
     * 
     * @return a list of the certificates of the {@link User}
     */
    public List<X509Certificate> getX509Certificates() {
        return x509Certificates;
    }

    /**
     * Provides an unmodifiable view of all additional {@link Extension} fields of the user
     * 
     * @return an unmodifiable view of the extensions
     */
    @JsonAnyGetter
    public Map<String, Extension> getExtensions() {
        return Collections.unmodifiableMap(extensions);
    }

    /**
     * Provides the {@link Extension} with the given URN
     * 
     * @param urn
     *        The URN of the extension
     * @return The extension for the given URN
     * @throws IllegalArgumentException
     *         If urn is null or empty
     * @throws NoSuchElementException
     *         If extension with given urn is not available
     */
    public Extension getExtension(String urn) {
        if (urn == null || urn.isEmpty()) {
            throw new IllegalArgumentException("urn must be neither null nor empty");
        }

        if (!extensions.containsKey(urn)) {
            throw new NoSuchElementException("extension " + urn + " is not available");
        }

        return extensions.get(urn);
    }

    /**
     * Checks if an extension with the given urn is present because an extension is not returned if no field is set
     * 
     * @param urn
     *        urn of the extension
     * @return true if the given extension is present, else false
     */
    public boolean isExtensionPresent(String urn) {
        return extensions.containsKey(urn);
    }

    public void setUserName(String userName) {
		this.userName = userName;
	}

    public void setName(Name name) {
		this.name = name;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public void setProfileUrl(String profileUrl) {
		this.profileUrl = profileUrl;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public void setPreferredLanguage(String preferredLanguage) {
		this.preferredLanguage = preferredLanguage;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setEmails(List<Email> emails) {
		this.emails = emails;
	}

	public void setPhoneNumbers(List<PhoneNumber> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}

	public void setIms(List<Im> ims) {
		this.ims = ims;
	}

	public void setPhotos(List<Photo> photos) {
		this.photos = photos;
	}

	public void setAddresses(List<Address> addresses) {
		this.addresses = addresses;
	}

	public void setGroups(List<GroupRef> groups) {
		this.groups = groups;
	}

	public void setEntitlements(List<Entitlement> entitlements) {
		this.entitlements = entitlements;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	public void setX509Certificates(List<X509Certificate> x509Certificates) {
		this.x509Certificates = x509Certificates;
	}

    @JsonAnySetter
	public void setExtensions(Map<String, Extension> extensions) {
		this.extensions = extensions;
	}

    public void setSchemas(Set<String> schemas) {
        super.setSchemas(schemas);
    }

	public void setActive(Boolean active) {
		this.active = active;
	}

    /*
    public List<CustomAttributes> getCustomAttributes() {
		return customAttributes;
	}

	public void setCustomAttributes(List<CustomAttributes> customAttributes) {
		this.customAttributes = customAttributes;
	}
	*/

	public void addExtension(Extension extension) {
        if (extension != null) {
            extensions.put(extension.getUrn(), extension);
            addSchema(extension.getUrn());
        }
    }

	@Override
    public String toString() {
        return "User [userName=" + userName + ", name=" + name + ", displayName=" + displayName + ", nickName="
                + nickName + ", profileUrl=" + profileUrl + ", title=" + title + ", userType=" + userType
                + ", preferredLanguage=" + preferredLanguage + ", locale=" + locale + ", timezone=" + timezone
                + ", active=" + active + ", password=" + password + ", emails=" + emails + ", phoneNumbers="
                + phoneNumbers + ", ims=" + ims + ", photos=" + photos + ", addresses=" + addresses + ", groups="
                + groups + ", entitlements=" + entitlements + ", roles=" + roles + ", x509Certificates="
                + x509Certificates + ", extensions=" + extensions + ", getId()=" + getId() + ", getExternalId()="
                + getExternalId() + ", getMeta()=" + getMeta() + ", getSchemas()=" + getSchemas() + "]";
    }

}
