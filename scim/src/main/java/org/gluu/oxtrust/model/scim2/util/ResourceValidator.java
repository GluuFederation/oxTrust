package org.gluu.oxtrust.model.scim2.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gluu.oxtrust.model.exception.SCIMException;
import org.gluu.oxtrust.model.scim2.BaseScimResource;
import org.gluu.oxtrust.model.scim2.Validations;
import org.gluu.oxtrust.model.scim2.annotations.Attribute;
import org.gluu.oxtrust.model.scim2.annotations.Schema;
import org.gluu.oxtrust.model.scim2.annotations.Validator;
import org.gluu.oxtrust.model.scim2.extensions.Extension;
import org.gluu.oxtrust.model.scim2.extensions.ExtensionField;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by jgomer on 2017-08-17.
 *
 * This class contains methods to validate whether a resource instance fulfills certain characteristics - regarded to
 * formatting, mutability, uniqueness, etc. - in order to adhere more closely to SCIM spec
 */
public class ResourceValidator {

    private Logger log = LogManager.getLogger(getClass());

    private static final String REQUIRED_ATTR_NOTFOUND="Required attribute %s not found";
    private static final String WRONG_SCHEMAS_ATTR ="Wrong value of schemas attribute";
    private static final String UNKNOWN_EXTENSION="Extension %s not recognized";
    private static final String ATTR_NOT_RECOGNIZED="Attribute %s not part of schema %s";
    private static final String ERROR_PARSING_EXTENDED="Error parsing extended attributes";
    private static final String ATTR_VALIDATION_FAILED ="Unexpected value for attribute %s";

    private BaseScimResource resource;
    private Class<? extends BaseScimResource> resourceClass;
    private List<Extension> extensions;

    /**
     * Construct a instance of this class base on a SCIM resource passed
     * @param resource SCIM resource object
     */
    public ResourceValidator(BaseScimResource resource){
        new ResourceValidator(resource, new ArrayList<Extension>());
    }

    public ResourceValidator(BaseScimResource resource, List<Extension> extensions){
        this.resource=resource;
        resourceClass=resource.getClass();
        this.extensions=extensions;
    }

    /**
     * Traverses the contents of a SCIM resource and applies a set of getter methods to collect a list of values.
     * For example, if passing a UserResource object and list of getters such as [getAdresses(), getStreetAddress()],
     * it will return a list with all street address that can be found in the associated addresses the user has
     * @param bean A SCIM resource object
     * @param getters A list of getters methods
     * @return List of values. They are collected by scanning the getter list from beginning to end. If no values could
     * be collected at all, an empty list is returned
     */
    public List<Object> getAttributeValues(BaseScimResource bean, final List<Method> getters){

        final List<Object> results=new ArrayList<Object>();

        class traversalClass{

            void traverse(Object value, int index){

                try {
                    if (value!=null && index < getters.size()) {
                        if (IntrospectUtil.isCollection(value.getClass())) {

                            Collection collection=(Collection)value;
                            if (collection.isEmpty())
                                traverse(null, index);    //stops branching...
                            else {
                                for (Object val : collection)
                                    traverse(val, index);
                            }
                        }
                        else {
                            Object val=getters.get(index).invoke(value);
                            traverse(val, index+1);
                        }
                    }
                    //Add result only if we are at the deepest level (tree tip)
                    if (index==getters.size())
                        results.add(value);
                }
                catch (Exception e){
                    log.error(e.getMessage(), e);
                }
            }

        }

        new traversalClass().traverse(bean, 0);
        return results;

    }

    public void validateRequiredAttributes() throws SCIMException {

        Map<String, List<Method>> map=IntrospectUtil.requiredCoreAttrs.get(resourceClass);

        for (String attributePath : map.keySet()){
            log.debug("Validating existence of required attribute '{}'", attributePath);

            for (Object val : getAttributeValues(resource, map.get(attributePath)))
                if (val == null) {
                    log.error("Error getting value of required attribute '{}'", attributePath);
                    throw new SCIMException(String.format(REQUIRED_ATTR_NOTFOUND, attributePath));
                }
        }

    }

    //This validation should be called after a successful call to validateRequiredAttributes
    public void validateValidableAttributes() throws SCIMException{

        Map<String, List<Method>> map=IntrospectUtil.validableCoreAttrs.get(resourceClass);

        for (String attributePath : map.keySet()) {

            Field f=IntrospectUtil.findFieldFromPath(resourceClass, attributePath);
            Validations valToApply=f.getAnnotation(Validator.class).value();
            log.debug("Validating value(s) of attribute '{}'", attributePath);

            for (Object val : getAttributeValues(resource, map.get(attributePath))) {
                if (!Validations.apply(valToApply, val)) {
                    log.error("Error validating attribute '{}', wrong value supplied: '{}'", attributePath, val.toString());
                    throw new SCIMException(String.format(ATTR_VALIDATION_FAILED, attributePath));
                }
            }
        }

    }

    //This validation should be called after a successful call to validateRequiredAttributes
    public void validateCanonicalizedAttributes() throws SCIMException{

        Map<String, List<Method>> map=IntrospectUtil.canonicalCoreAttrs.get(resourceClass);

        for (String attributePath : map.keySet()) {

            Field f=IntrospectUtil.findFieldFromPath(resourceClass, attributePath);
            List<String> canonicalVals=Arrays.asList(f.getAnnotation(Attribute.class).canonicalValues());
            log.debug("Validating values of canonical attribute '{}'", attributePath);

            for (Object val : getAttributeValues(resource, map.get(attributePath))) {
                if (!canonicalVals.contains(val.toString())) {
                    log.error("Error validating canonical attribute '{}', wrong value supplied: '{}'", attributePath, val.toString());
                    throw new SCIMException(String.format(ATTR_VALIDATION_FAILED, attributePath));
                }
            }
        }
    }

    //This validation should be called after a successful call to validateRequiredAttributes
    public void validateSchemasAttribute() throws SCIMException {

        Set<String> allSchemas=new HashSet<String>();
        allSchemas.add(resourceClass.getAnnotation(Schema.class).id());
        for (Extension ext : extensions)
            allSchemas.add(ext.getUrn());

        Set<String> schemaList = new HashSet<String>(resource.getSchemas());
        schemaList.removeAll(allSchemas);

        if (schemaList.size()>0)    //means that some wrong extension urn is there
            throw new SCIMException(WRONG_SCHEMAS_ATTR);

    }

    /**
     * Validates if an attribute part of an extension is consistent with an arbitrary value passed
     * @param extension Extension where the attribute exists
     * @param attribute The name of the attribute inside the extensin passed
     * @param value The value to be checked (never null)
     * @throws SCIMException When the value is inconsistent, or the attribute is not part of the extension
     */
    private void validateDataTypeExtendedAttr(Extension extension, String attribute, Object value) throws SCIMException{

        ExtensionField field=extension.getFields().get(attribute);
        if (field==null)
            throw new SCIMException(String.format(ATTR_NOT_RECOGNIZED, attribute, extension.getUrn()));
        else{
            log.debug("validateDataTypeExtendedAttr. Checking attribute '{}' for type '{}' with value '{}'", attribute, field.toString(), value.toString());

            //look up if the field in this extension is consistent with the value passed
            if (ExtensionField.valueOf(field, value)==null)
                throw new SCIMException(String.format(ATTR_VALIDATION_FAILED, attribute));
        }

    }

    //This validation should be called after a successful call to validateSchemasAttribute
    public void validateExtendedAttributes() throws SCIMException{

        //Note: throughout this method, we always ignore presence of nulls

        //Gets all extended attributes (see the @JsonAnySetter annotation in BaseScimResource)
        Map<String, Object> extendedAttributes=resource.getExtendedAttributes();

        //Iterate over every extension of the resource object (in practice it will be just one at most)
        for (String schema : extendedAttributes.keySet()) {
            //Validate if the schema referenced in the extended attributes is contained in the valid set of extension

            Extension extension=null;
            for (Extension ext : extensions)
                if (ext.getUrn().equals(schema)) {
                    extension = ext;
                    break;
                }

            if (extension!=null) {
                log.debug("validateExtendedAttributes. Revising attributes under schema {}", schema);

                try {
                    //Obtains a generic map consisting of all name/value(s) pairs associated to this schema
                    Map<String, Object> attrsMap = (Map<String, Object>) extendedAttributes.get(schema);

                    for (String attr : attrsMap.keySet()) {
                        Object value = attrsMap.get(attr);
                        if (value != null) {
                            /*
                             Gets the class associated to the value of current attribute. Since the value is coming
                             from Json content, it can only be: String, numeric (Integer or Double), boolean, Collection
                             (ArrayList), or Map (LinkedHashMap). For extended attributes, we should only see coming:
                             String, Integer, and Collection. Different things will be rejected
                             */
                            Class cls = value.getClass();
                            boolean isCollection=IntrospectUtil.isCollection(cls);

                            log.debug("validateExtendedAttributes. Got value(s) for attribute '{}'", attr);
                            //Check if the multivalued custom attribute is consistent with the nature of the value itself
                            if (isCollection == extension.getFields().get(attr).isMultiValued()){
                                if (isCollection) {
                                    for (Object elem : (Collection) value)
                                        if (elem!=null)
                                            validateDataTypeExtendedAttr(extension, attr, elem);
                                }
                                else
                                    validateDataTypeExtendedAttr(extension, attr, value);
                            }
                            else
                                throw new SCIMException(ERROR_PARSING_EXTENDED);
                        }
                    }
                }
                catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new SCIMException(ERROR_PARSING_EXTENDED);
                }
            }
            else
                throw new SCIMException(String.format(UNKNOWN_EXTENSION, schema));
        }

    }

}
