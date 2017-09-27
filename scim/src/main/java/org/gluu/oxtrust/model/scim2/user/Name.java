package org.gluu.oxtrust.model.scim2.user;

import org.apache.commons.lang.StringUtils;
import org.gluu.oxtrust.model.scim2.annotations.Attribute;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;

/**
 * Created by jgomer on 2017-09-04.
 *
 * The components of the user's full name.
 *
 * Do not remove LdapAttribute annotations. These are used by FilterVisitor classes to convert SCIM filter queries into LDAP queries
 */
public class Name {

    @Attribute(description = "The family name of the User, or Last  Name in most Western languages (for example, Jensen " +
            "given the full name Ms. Barbara J Jensen, III.).")
    @LdapAttribute(name = "sn")
    private String familyName;

    @Attribute(description = "The given name of the User, or First Name  in most Western languages (for example, Barbara " +
            "given the full name Ms. Barbara J Jensen, III.).")
    @LdapAttribute(name = "givenName")
    private String givenName;

    @Attribute(description = "The middle name(s) of the User (for example,  Robert given the full name Ms. Barbara J " +
            "Jensen, III.).")
    @LdapAttribute(name = "middleName")
    private String middleName;

    @Attribute(description = "The honorific prefix(es) of the User, or  Title in most Western languages (for example, Ms. " +
            "given the full name Ms. Barbara J Jensen, III.).")
    @LdapAttribute(name = "oxTrusthonorificPrefix")
    private String honorificPrefix;

    @Attribute(description = "The honorific suffix(es) of the User, or  Suffix in most Western languages (for example, " +
            "III. given the full name Ms. Barbara J Jensen, III.)")
    @LdapAttribute(name = "oxTrusthonorificSuffix")
    private String honorificSuffix;

    @Attribute(description = "The full name, including all middle names, titles, and suffixes as appropriate, formatted " +
            "for display (for example, Ms. Barbara J Jensen, III.).")
    //This field is computed iff a value is not passed (ie is null)
    private String formatted;

    public String getFormatted() {

        if (StringUtils.isEmpty(formatted)) {
            StringBuilder formattedName = new StringBuilder("");

            formattedName.append(StringUtils.isEmpty(honorificPrefix) ? "" : honorificPrefix + " ");
            formattedName.append(StringUtils.isEmpty(givenName) ? "" : givenName + " ");
            formattedName.append(StringUtils.isEmpty(middleName) ? "" : middleName + " ");
            formattedName.append(StringUtils.isEmpty(familyName) ? "" : familyName + " ");
            formattedName.append(StringUtils.isEmpty(honorificSuffix) ? "" : honorificSuffix);

            formatted=formattedName.toString().trim();
        }
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
