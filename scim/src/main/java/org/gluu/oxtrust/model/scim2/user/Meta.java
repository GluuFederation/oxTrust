package org.gluu.oxtrust.model.scim2.user;

import org.gluu.oxtrust.model.scim2.AttributeDefinition;
import org.gluu.oxtrust.model.scim2.annotations.Attribute;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;

/**
 * Created by jgomer on 2017-09-04.
 *
 * Do not remove LdapAttribute annotations. These are used by FilterVisitor classes to convert SCIM filter queries into LDAP queries
 */
public class Meta {

    @Attribute(description = "The resource Type",
            isCaseExact = true,
            mutability = AttributeDefinition.Mutability.READ_ONLY)
    private String resourceType;

    @Attribute(description = "Date and time the resource was created",
            mutability = AttributeDefinition.Mutability.READ_ONLY)
    @LdapAttribute(name = "oxTrustMetaCreated")
    private String created;

    @Attribute(description = "Date and time the resource was last modified",
            mutability = AttributeDefinition.Mutability.READ_ONLY)
    @LdapAttribute(name = "oxTrustMetaLastModified")
    private String lastModified;

    @Attribute(description = "The location (URI) of the resource",
            mutability = AttributeDefinition.Mutability.READ_ONLY)
    @LdapAttribute(name = "oxTrustMetaLocation")
    private String location;

    @Attribute(description = "The version of the resource",
            isCaseExact = true,
            mutability = AttributeDefinition.Mutability.READ_ONLY)
    @LdapAttribute(name = "oxTrustMetaVersion")
    private String version;

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
