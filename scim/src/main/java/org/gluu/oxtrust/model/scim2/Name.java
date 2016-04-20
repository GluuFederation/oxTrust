/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2;

import org.gluu.site.ldap.persistence.annotation.LdapAttribute;

/**
 * This class represents the User's real name.
 *
 * <p>
 * For more detailed information please look at the <a
 * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema 2.0, section 6</a>
 * </p>
 */
// @XmlAccessorType(XmlAccessType.FIELD)
public class Name {

    private String formatted;

    @LdapAttribute(name = "sn")
    private String familyName;

    @LdapAttribute(name = "givenName")
    private String givenName;

    @LdapAttribute(name = "middleName")
    private String middleName;

    @LdapAttribute(name = "oxTrusthonorificPrefix")
    private String honorificPrefix;

    @LdapAttribute(name = "oxTrusthonorificSuffix")
    private String honorificSuffix;

    /**
     * Default constructor for Jackson
     */
    public Name() {
    }


    /**
     * Gets the full name, including all middle names, titles, and suffixes as appropriate, formatted for display.
     *
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema 2.0, section 6</a>
     * </p>
     *
     * @return the formatted name
     */
    public String getFormatted() {
        return formatted;
    }

    /**
     * Gets the family name of the User.
     *
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema 2.0, section 6</a>
     * </p>
     *
     * @return the family name
     */
    public String getFamilyName() {
        return familyName;
    }

    /**
     * Gets the given (first) name of the User.
     *
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema 2.0, section 6</a>
     * </p>
     *
     * @return the given name
     */
    public String getGivenName() {
        return givenName;
    }

    /**
     * Gets the middle name(s) of the User.
     *
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema 2.0, section 6</a>
     * </p>
     *
     * @return the middle name
     */
    public String getMiddleName() {
        return middleName;
    }

    /**
     * Gets the honorific prefix(es) of the User.
     *
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema 2.0, section 6</a>
     * </p>
     *
     * @return the honorific prefix
     */
    public String getHonorificPrefix() {
        return honorificPrefix;
    }

    /**
     * Gets the honorific suffix(es) of the User.
     *
     * <p>
     * For more detailed information please look at the <a
     * href="http://tools.ietf.org/html/draft-ietf-scim-core-schema-02#section-6">SCIM core schema 2.0, section 6</a>
     * </p>
     *
     * @return the honorific sufix
     */
    public String getHonorificSuffix() {
        return honorificSuffix;
    }

    public void setFormatted(String formatted) {
		this.formatted = formatted;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public void setHonorificPrefix(String honorificPrefix) {
		this.honorificPrefix = honorificPrefix;
	}

	public void setHonorificSuffix(String honorificSuffix) {
		this.honorificSuffix = honorificSuffix;
	}

	/**
     * <p>Checks if this {@link Name} is empty, i.e. all properties are empty or null.</p>
     *
     * @return true if all properties are null or empty, else false
     */
 /*   @JsonIgnore
    @XmlTransient
    public boolean isEmpty() {
        if(!Strings.isNullOrEmpty(formatted)) {
            return false;
        }

        if(!Strings.isNullOrEmpty(familyName)) {
            return false;
        }

        if(!Strings.isNullOrEmpty(givenName)) {
            return false;
        }

        if(!Strings.isNullOrEmpty(middleName)) {
            return false;
        }

        if(!Strings.isNullOrEmpty(honorificPrefix)) {
            return false;
        }

        if(!Strings.isNullOrEmpty(honorificSuffix)) {
            return false;
        }

        return true;
    }*/

    @Override
    public String toString() {
        return "Name [formatted=" + formatted + ", familyName=" + familyName + ", givenName=" + givenName
                + ", middleName=" + middleName + ", honorificPrefix=" + honorificPrefix + ", honorificSuffix="
                + honorificSuffix + "]";
    }
}
