/*
 * SCIM-Client is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
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
 * This class represents the root hierarchy of SCIM resources. All of them: user, group, etc. are subclasses of this class.
 */
/*
 * Created by jgomer on 2017-09-04.
 *
 * Notes: Property names (member names) MUST match exactly as in the spec, so do not change!. Add a new item to the list
 * found in the static block of code at org.gluu.oxtrust.model.scim2.util.IntrospectUtil when a new subclass (SCIM resource)
 * is added. StoreReference annotations are used by LdapFilterListener to convert SCIM filter queries into LDAP queries
 * Adapted from https://github.com/pingidentity/scim2/blob/master/scim2-sdk-common/src/main/java/com/unboundid/scim2/common/BaseScimResource.java
 * Based on former org.gluu.oxtrust.model.scim2.Resource class
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
    private Set<String> schemas;

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

    private Map<String, Object> extendedAttrs=new HashMap<String, Object>();   //must never be null

    /**
     * Replaces the custom attributes belonging to the extension identified by the <code>urn</code> passed as parameter
     * with the attribute/value pairs supplied in the <code>Map</code>.
     * <p>Note that this method does not apply any sort of validation. Whether the <code>urn</code> and attributes are
     * recognized or the values are consistent with data types, is something that is performed only when the resource is
     * passed in a service method invocation.</p>
     * @param extensionUrn Urn that identifies an extension
     * @param extension A Map holding attribute names (Strings) and values (Objects).
     */
    @JsonAnySetter
    public void addExtendedAttributes(String extensionUrn, Map<String, Object> extension){
        extendedAttrs.put(extensionUrn, extension);
    }

    /**
     * Retrieves all custom attributes found in this resource object. The attributes are structured hierarchically in a
     * <code>Map</code> where they can be looked up using the <code>urn</code> to which the attributes belong to.
     * @return A Map with all custom attributes
     */
    @JsonAnyGetter
    public Map<String, Object> getExtendedAttributes(){
        return extendedAttrs;
    }

    /**
     * Constructs a basic SCIM resource with all its attributes unassigned
     */
    public BaseScimResource(){
        schemas=new HashSet<String>();
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

    public Set<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(Set<String> schemas) {
        this.schemas = schemas;
    }

    /**
     * Returns the custom attributes of this resource that belong to the extension identified by the <code>urn</code>
     * passed as parameter.
     * @param urn Identifier of the extension
     * @return A Map that holds name/value pairs of the attributes
     */
    public Map<String, Object> getExtendedAttributes(String urn){
        //This method is useful for client-side, do not remove
        Object custAttrs=extendedAttrs.get(urn);
        return (custAttrs==null) ? null : IntrospectUtil.strObjMap(custAttrs);
    }

}
