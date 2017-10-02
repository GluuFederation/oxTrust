package org.gluu.oxtrust.model.scim2;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.gluu.oxtrust.model.scim2.annotations.Attribute;
import org.gluu.oxtrust.model.scim2.annotations.Schema;
import org.gluu.oxtrust.model.scim2.user.Meta;
import org.gluu.site.ldap.persistence.annotation.LdapAttribute;

import java.util.*;

/**
 * Created by jgomer on 2017-09-04.
 *
 * This class represents the root hierarchy of SCIM resources. All of them: user, group, etc. are subclasses of this class
 * Adapted from https://github.com/pingidentity/scim2/blob/master/scim2-sdk-common/src/main/java/com/unboundid/scim2/common/BaseScimResource.java
 *
 * Do not remove LdapAttribute annotations. These are used by FilterVisitor classes to convert SCIM filter queries into LDAP queries
 */
public class BaseScimResource {

    @Attribute(description = "The schemas attribute is a REQUIRED attribute and is an array of Strings containing URIs " +
            "that are used to indicate the namespaces of the SCIM schemas that define the attributes present in the " +
            "current JSON structure",
            isRequired = true,
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            returned = AttributeDefinition.Returned.ALWAYS)
    private List<String> schemas;

    @Attribute(description = "A unique identifier for a SCIM resource as defined by the service provider",
            isRequired = false,     //e.g. clients don't need to pass it
            isCaseExact = true,
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            returned = AttributeDefinition.Returned.ALWAYS,
            uniqueness = AttributeDefinition.Uniqueness.SERVER) //?
    @LdapAttribute(name = "inum")
    private String id;

    @Attribute(description = "A String that is an identifier for the resource as defined by the provisioning client",
            isCaseExact = true)
    @LdapAttribute(name = "oxTrustExternalId")
    private String externalId;

    @Attribute(description = "A complex attribute containing resource metadata",
            mutability = AttributeDefinition.Mutability.READ_ONLY)
    private Meta meta;

    public BaseScimResource(){
        schemas=new ArrayList<String>();
        schemas.add(getClass().getAnnotation(Schema.class).id());
    }

    private Map<String, Object> extendedAttrs=new HashMap<String, Object>();   //Never must be null

    @JsonAnySetter
    public void addExtendedAttributes(String extensionUrn, Map<String, Object> extension){
        extendedAttrs.put(extensionUrn, extension);
    }

    @JsonAnyGetter
    public Map<String, Object> getExtendedAttributes(){
        return extendedAttrs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }


    public static String getType(Class<? extends BaseScimResource> cls){
        Schema annot=cls.getAnnotation(Schema.class);
        return annot==null ? null : annot.name();
    }

    /*
    public static Class <? extends BaseScimResource> getResourceClass(BaseScimResource bean){

        Class<? extends BaseScimResource> origClass=bean.getClass();
        List<Class<? extends BaseScimResource>> scimResouceSubClasses= Arrays.asList(UserResource.class, GroupResource.class);
        for (Class subClass : scimResouceSubClasses)
            try{
                origClass.asSubclass(subClass);
                return subClass;
            }
            catch (Exception e){
                //left empty intentionally
            }
        return null;
    }
    */
}
