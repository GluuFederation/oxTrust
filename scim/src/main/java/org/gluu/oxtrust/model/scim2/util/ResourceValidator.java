package org.gluu.oxtrust.model.scim2.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gluu.oxtrust.model.exception.SCIMException;
import org.gluu.oxtrust.model.scim2.BaseScimResource;
import org.gluu.oxtrust.model.scim2.Validations;
import org.gluu.oxtrust.model.scim2.annotations.Attribute;
import org.gluu.oxtrust.model.scim2.annotations.Schema;
import org.gluu.oxtrust.model.scim2.annotations.Validator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

//TODO: remainder - changes for issues: I, II,
/**
 * Created by jgomer on 2017-08-17.
 *
 * This class contains methods to validate whether a resource instance fulfills certain characteristics - regarded to
 * formatting, mutability, uniqueness, etc. - in order to adhere more closely to SCIM spec
 */
public class ResourceValidator {

    private Logger log = LogManager.getLogger(getClass());

    private static final String REQUIRED_ATTR_NOTFOUND="Required attribute %s not found";
    private static final String SCHEMAS_ATTR_NOTFOUND ="Wrong value of schemas attribute";
    private static final String ATTR_VALIDATION_FAILED ="Unexpected value for attribute %s";

    private BaseScimResource resource;
    private Class<? extends BaseScimResource> resourceClass;

    /**
     * Construct a instance of this class base on a SCIM resource passed
     * @param resource SCIM resource object
     */
    public ResourceValidator(BaseScimResource resource){
        this.resource=resource;
        resourceClass=resource.getClass();
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
            log.debug("Validating existence of required attribute {}", attributePath);

            for (Object val : getAttributeValues(resource, map.get(attributePath)))
                if (val == null) {
                    log.error("Error getting value of required attribute {}", attributePath);
                    throw new SCIMException(String.format(REQUIRED_ATTR_NOTFOUND, attributePath));
                }
        }

    }

    //This validation should be called after a successful call to validateRequiredAttributes
    public void validateSchemaAttribute() throws SCIMException {

        Set<String> schemaList = new HashSet<String>(resource.getSchemas());
        String coreSchema=resourceClass.getAnnotation(Schema.class).id();

        if (!schemaList.contains(coreSchema))
            throw new SCIMException(SCHEMAS_ATTR_NOTFOUND);

    }

    //This validation should be called after a successful call to validateRequiredAttributes
    public void validateValidableAttributes() throws SCIMException{

        Map<String, List<Method>> map=IntrospectUtil.validableCoreAttrs.get(resourceClass);

        for (String attributePath : map.keySet()) {

            Field f=IntrospectUtil.findFieldFromPath(resourceClass, attributePath);
            Validations valToApply=f.getAnnotation(Validator.class).value();
            log.debug("Validating values of attribute {}", attributePath);

            for (Object val : getAttributeValues(resource, map.get(attributePath))) {
                if (!Validations.apply(valToApply, val)) {
                    log.error("Error validating attribute {}, wrong value supplied: {}", attributePath, val.toString());
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
            log.debug("Validating values of canonical attribute {}", attributePath);

            for (Object val : getAttributeValues(resource, map.get(attributePath))) {
                if (!canonicalVals.contains(val.toString())) {
                    log.error("Error validating canonical attribute {}, wrong value supplied: {}", attributePath, val.toString());
                    throw new SCIMException(String.format(ATTR_VALIDATION_FAILED, attributePath));
                }
            }
        }
    }

}
