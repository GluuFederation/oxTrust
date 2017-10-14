package org.gluu.oxtrust.model.scim2.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.gluu.oxtrust.model.scim2.BaseScimResource;
import org.gluu.oxtrust.model.scim2.annotations.Attribute;
import org.gluu.oxtrust.model.scim2.extensions.Extension;
import org.gluu.oxtrust.model.scim2.extensions.ExtensionField;

import javax.management.InvalidAttributeValueException;
import java.lang.reflect.Field;
import java.util.*;

import static org.gluu.oxtrust.model.scim2.AttributeDefinition.Mutability.*;

/**
 * Created by jgomer on 2017-09-25.
 *
 * Helper class to traverse a SCIM resource object recursively
 */
class traversalClass {

    String errorAt;
    private Class base;

    public traversalClass(Class baseClass){
        base=baseClass;
    }

    private String getNewPrefix(String prefix, String key){
        return prefix + (prefix.length()==0 ? "" : ".") + key;
    }

    private Map<String, Object> smallerMap(String prefix, Map<String, Object> source, Object destination){
        Map<String, Object> smallMap = (destination==null) ? new HashMap<String, Object>() : (Map<String, Object>) destination;
        traverse(prefix, source, smallMap);
        return smallMap.size()==0 ? null : smallMap;
    }

    void traverse(String prefix, Map<String, Object> source, Map<String, Object> destination){

        for (String key : source.keySet()) {
            Object value = source.get(key);
            Object destValue= destination.get(key);

            if (value != null && errorAt==null) {
                Field f = IntrospectUtil.findFieldFromPath(base, getNewPrefix(prefix, key));

                if (f!=null) {  //Atrributes related to extensions evaluate null here
                    Attribute attrAnnot = f.getAnnotation(Attribute.class);
                    if (attrAnnot != null && !attrAnnot.mutability().equals(READ_ONLY)) {
                        if (value instanceof Map)
                            value = smallerMap(getNewPrefix(prefix, key), (Map<String, Object>) value, destValue);
                        else
                        if (attrAnnot.mutability().equals(IMMUTABLE) && destValue != null && !value.equals(destValue)) {
                            //provokes no more traversals
                            errorAt = key;
                            value = null;
                        }

                        if (value != null) {
                            if (IntrospectUtil.isCollection(value.getClass()) && ((Collection) value).size() == 0)
                                value = null;
                            destination.put(key, value);
                        }
                    }
                }
            }
        }
    }

    public void attachExtensionInfo(Map<String, Object> source, Map<String, Object> destination, List<Extension> extensions){

        for (Extension extension : extensions){
            String urn=extension.getUrn();
            Object extendedAttrsObj=source.get(urn);

            if (extendedAttrsObj!=null){

                Map<String, Object> extendedAttrs=(Map<String, Object>) extendedAttrsObj;
                Map<String, ExtensionField> fields=extension.getFields();

                Map<String, Object> destMap = destination.get(urn)==null ? new HashMap<String, Object>() : (Map<String, Object>) destination.get(urn);

                for (String attr : fields.keySet()){
                    Object value=extendedAttrs.get(attr);

                    if (value!=null)
                        destMap.put(attr, value);
                }
                destination.put(urn, destMap);
            }
        }
    }

}

/**
 * Created by jgomer on 2017-09-25.
 *
 * This class contains methods to facilitate transformation, and manipulation of data inside SCIM resource objects
 */
public class ScimResourceUtil {

    private static Logger log = LogManager.getLogger(ScimResourceUtil.class);

    private static ObjectMapper mapper=new ObjectMapper();

    /**
     * Incorporates the information from an origin into a destination object doing replacement (suitable for the replace
     * operation via PUT, for instance), following these rules:
     * - Ignores changes in readonly attrs
     * - Ignores null values (for single-valued attrs)
     * - Nullifies multi-valued attribute when empty array is passed
     * - Immutable attrs must match in origin and destination or else exception is thrown
     * @param origin Object with the information to be copied/replaced into the destination object
     * @param destination Object that receives the new information (only non-null attributes in the origin object) end up
     *                    being changed in this object
     * @param extensions
     * @return A new object that contains the result: the final state of destination object
     * @throws Exception When recursive traversal fails or if the rule of immutable attribute was not fulfilled
     */
    public static BaseScimResource transferToResource(BaseScimResource origin, final BaseScimResource destination,
                                                      List<Extension> extensions) throws InvalidAttributeValueException{

        Map<String, Object> fromMap = mapper.convertValue(origin, Map.class);
        Map<String, Object> toMap = mapper.convertValue(destination, Map.class);

        traversalClass tclass=new traversalClass(origin.getClass());
        tclass.traverse("", fromMap, toMap);
        tclass.attachExtensionInfo(fromMap, toMap, extensions);

        if (tclass.errorAt==null)
            return mapper.convertValue(toMap, origin.getClass());
        else
            throw new InvalidAttributeValueException("Invalid value passed for immutable attribute " + tclass.errorAt);

    }

}
