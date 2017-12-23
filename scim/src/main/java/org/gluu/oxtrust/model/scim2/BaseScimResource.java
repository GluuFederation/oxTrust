package org.gluu.oxtrust.model.scim2;

import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.gluu.oxtrust.model.scim2.annotations.*;
import org.gluu.oxtrust.model.scim2.fido.FidoDeviceResource;
import org.gluu.oxtrust.model.scim2.group.GroupResource;
import org.gluu.oxtrust.model.scim2.user.UserResource;
import org.gluu.oxtrust.model.scim2.util.IntrospectUtil;
import org.gluu.oxtrust.model.scim2.util.ScimResourceUtil;

import java.util.*;

/**
 * Created by jgomer on 2017-09-04.
 *
 * This class represents the root hierarchy of SCIM resources. All of them: user, group, etc. are subclasses of this class
 * Notes: Property names (member names) MUST match exactly as in the spec, so do not change!. Add a new item to the list
 * found in the static block of code at org.gluu.oxtrust.model.scim2.util.IntrospectUtil when a new subclass (SCIM resource)
 * is added. StoreReference annotations are used by FilterVisitor classes to convert SCIM filter queries into LDAP queries
 *
 * Adapted from https://github.com/pingidentity/scim2/blob/master/scim2-sdk-common/src/main/java/com/unboundid/scim2/common/BaseScimResource.java
 */
public class BaseScimResource {

    @Attribute(description = "The schemas attribute is a REQUIRED attribute and is an array of Strings containing URIs " +
            "that are used to indicate the namespaces of the SCIM schemas that define the attributes present in the " +
            "current JSON structure",
            isRequired = true,
            //mutability = AttributeDefinition.Mutability.READ_ONLY,
            /* This should not be READ_ONLY as the spec says, ie. if upon creation only the default schema is provided and
               then via an update a custom attribute is specified, the schemas attributes needs to be updated! */
            returned = AttributeDefinition.Returned.ALWAYS)
    private List<String> schemas;

    @Attribute(description = "A unique identifier for a SCIM resource as defined by the service provider",
            isRequired = false,     //Notice that clients don't need to pass it really
            isCaseExact = true,
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            returned = AttributeDefinition.Returned.ALWAYS,
            uniqueness = AttributeDefinition.Uniqueness.SERVER)
    @StoreReference(resourceType = {UserResource.class, FidoDeviceResource.class, GroupResource.class},
            refs = {"inum", "oxId", "inum"})
    private String id;

    @Attribute(description = "A String that is an identifier for the resource as defined by the provisioning client",
            isCaseExact = true)
    @StoreReference(ref = "oxTrustExternalId")
    private String externalId;

    @Attribute(description = "A complex attribute containing resource metadata",
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            type = AttributeDefinition.Type.COMPLEX)
    private Meta meta;

    private Map<String, Object> extendedAttrs=new HashMap<String, Object>();   //Never must be null

    @JsonAnySetter
    public void addExtendedAttributes(String extensionUrn, Map<String, Object> extension){
        extendedAttrs.put(extensionUrn, extension);
    }

    @JsonAnyGetter
    public Map<String, Object> getExtendedAttributes(){
        return extendedAttrs;
    }

    public BaseScimResource(){
        schemas=new ArrayList<String>();
        String defSchema= ScimResourceUtil.getDefaultSchemaUrn(getClass());
        if (defSchema!=null)
            schemas.add(defSchema);
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

    //Useful method for client-side
    public Map<String, Object> getExtendedAttributes(String urn){
        Object custAttrs=extendedAttrs.get(urn);
        return (custAttrs==null) ? null : IntrospectUtil.strObjMap(custAttrs);
    }

}
