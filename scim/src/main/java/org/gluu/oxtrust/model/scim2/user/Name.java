/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
package org.gluu.oxtrust.model.scim2.user;

import org.apache.commons.lang.StringUtils;
import org.gluu.oxtrust.model.scim2.annotations.Attribute;
import org.gluu.oxtrust.model.scim2.annotations.StoreReference;

/**
 * A class that represents the components of a user's name. See section 4.1.1 of RFC 7643.
 */
/*
 * Created by jgomer on 2017-09-04.
 */
public class Name {

    @Attribute(description = "The family name of the User, or Last  Name in most Western languages (for example, Jensen " +
            "given the full name Ms. Barbara J Jensen, III.).")
    @StoreReference(ref = "sn")
    private String familyName;

    @Attribute(description = "The given name of the User, or First Name  in most Western languages (for example, Barbara " +
            "given the full name Ms. Barbara J Jensen, III.).")
    @StoreReference(ref = "givenName")
    private String givenName;

    @Attribute(description = "The middle name(s) of the User (for example,  Robert given the full name Ms. Barbara J " +
            "Jensen, III.).")
    @StoreReference(ref = "middleName")
    private String middleName;

    @Attribute(description = "The honorific prefix(es) of the User, or  Title in most Western languages (for example, Ms. " +
            "given the full name Ms. Barbara J Jensen, III.).")
    @StoreReference(ref = "oxTrusthonorificPrefix")
    private String honorificPrefix;

    @Attribute(description = "The honorific suffix(es) of the User, or  Suffix in most Western languages (for example, " +
            "III. given the full name Ms. Barbara J Jensen, III.)")
    @StoreReference(ref = "oxTrusthonorificSuffix")
    private String honorificSuffix;

    @Attribute(description = "The full name, including all middle names, titles, and suffixes as appropriate, formatted " +
            "for display (for example, Ms. Barbara J Jensen, III.).")
    //This field is computed iff a value is not passed (ie is null)
    @StoreReference(ref = "oxTrustNameFormatted")
    private String formatted;

    /**
     * From a Name instance, it builds a string depicting a full name including all middle names, titles, and suffixes
     * as appropriate for display if the {@link #getFormatted() formatted} field of the object passed is null or empty
     * @param name A <code>Name</code> instance
     * @return A string representing a full name
     */
    public static String computeFormattedName(Name name){

        if (StringUtils.isEmpty(name.formatted)) {
            String formattedName = "";

            formattedName+=StringUtils.isEmpty(name.honorificPrefix) ? "" : name.honorificPrefix + " ";
            formattedName+=StringUtils.isEmpty(name.givenName) ? "" : name.givenName + " ";
            formattedName+=StringUtils.isEmpty(name.middleName) ? "" : name.middleName + " ";
            formattedName+=StringUtils.isEmpty(name.familyName) ? "" : name.familyName + " ";
            formattedName+=StringUtils.isEmpty(name.honorificSuffix) ? "" : name.honorificSuffix;
            formattedName=formattedName.trim();

            name.formatted=formattedName.length()==0 ? null : formattedName;
        }
        return name.formatted;

    }

    public String getFormatted() {
        return formatted;
    }

    public void setFormatted(String formatted) {
        this.formatted = formatted;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getHonorificPrefix() {
        return honorificPrefix;
    }

    public void setHonorificPrefix(String honorificPrefix) {
        this.honorificPrefix = honorificPrefix;
    }

    public String getHonorificSuffix() {
        return honorificSuffix;
    }

    public void setHonorificSuffix(String honorificSuffix) {
        this.honorificSuffix = honorificSuffix;
    }

}
